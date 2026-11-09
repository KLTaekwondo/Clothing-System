package com.superkl.backend.service.stock;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.dto.stock.StockContext;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.entity.product.Product;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.stock.StockRecord;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.enums.CheckEnum;
import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.repository.stock.StockRecordRepository;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WareHouseStockServiceTest {

    @Mock
    private WareHouseStockRepository wareHouseStockRepository;

    @Mock
    private StockRecordRepository stockRecordRepository;

    private WareHouseStockService wareHouseStockService;

    private static final Long WAREHOUSE_ID = 1L;
    private static final Long SKU_ID = 100L;
    private static final Long STOCK_ID = 1000L;

    @BeforeEach
    void setUp() {
        wareHouseStockService = new WareHouseStockService(
                wareHouseStockRepository,
                stockRecordRepository
        );

        // saveRecord 里要取操作人，所以得塞一个身份
        RequestUser currentUser = RequestUser.builder()
                .requestId(WAREHOUSE_ID)
                .requestRole("ROLE_ADMIN")
                .requestName("测试管理员")
                .requestCode("ADMIN001")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========== 扣减：正常 ==========

    @Test
    void decreaseStock_whenNormal_shouldDeductAndSaveRecord() {
        // 锁出来是 10，实体也是 10
        when(wareHouseStockRepository.lockStockBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(10));
        WareHouseStock ws = buildStock(10);
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));

        wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 3, buildContext());

        assertEquals(7, ws.getStock());
        verify(wareHouseStockRepository).save(ws);

        ArgumentCaptor<StockRecord> captor = ArgumentCaptor.forClass(StockRecord.class);
        verify(stockRecordRepository).save(captor.capture());
        StockRecord record = captor.getValue();
        assertEquals(10, record.getBeforeQuantity());
        assertEquals(7, record.getAfterQuantity());
        assertEquals(-3, record.getChangeQuantity());
        assertEquals("OR20260721TEST", record.getSourceNo());
    }

    // ========== 扣减：数量非法 ==========

    @Test
    void decreaseStock_whenQuantityNonPositive_shouldThrow() {
        assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 0, buildContext()));
        assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, -5, buildContext()));

        // 数量都不合法，一条查询都不该发
        verifyNoInteractions(wareHouseStockRepository);
        verify(stockRecordRepository, never()).save(any());
    }

    // ========== 扣减：锁查询为空（记录不存在） ==========

    @Test
    void decreaseStock_whenLockReturnsEmpty_shouldThrow() {
        when(wareHouseStockRepository.lockStockBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 3, buildContext()));
        assertTrue(ex.getMessage().contains("库存记录不存在"));

        // 锁都没拿到，不该再发第二条查询
        verify(wareHouseStockRepository, never()).findBySkuIdAndWarehouseId(any(), any());
        verify(stockRecordRepository, never()).save(any());
    }

    // ========== 扣减：锁拿到了但实体没了 ==========

    @Test
    void decreaseStock_whenEntityMissingAfterLock_shouldThrow() {
        when(wareHouseStockRepository.lockStockBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(10));
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 3, buildContext()));
        assertTrue(ex.getMessage().contains("库存记录不存在"));

        verify(wareHouseStockRepository, never()).save(any());
    }

    // ========== 扣减：库存不足（防超卖核心） ==========

    @Test
    void decreaseStock_whenInsufficientStock_shouldThrowAndNotSave() {
        when(wareHouseStockRepository.lockStockBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(2));
        WareHouseStock ws = buildStock(2);
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 5, buildContext()));
        assertTrue(ex.getMessage().contains("库存不足"));

        assertEquals(2, ws.getStock());
        verify(wareHouseStockRepository, never()).save(any());
        verify(stockRecordRepository, never()).save(any());
    }

    // ========== 扣减：仓库盘点中 ==========

    @Test
    void decreaseStock_whenWareHouseUnderCheck_shouldThrow() {
        when(wareHouseStockRepository.lockStockBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(10));
        WareHouseStock ws = buildStock(10, CheckEnum.UNDER_CHECK);
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 3, buildContext()));
        assertTrue(ex.getMessage().contains("盘点"));

        assertEquals(10, ws.getStock());
        verify(wareHouseStockRepository, never()).save(any());
        verify(stockRecordRepository, never()).save(any());
    }

    // ========== 增加：正常 ==========

    @Test
    void increaseStock_whenNormal_shouldAddAndSaveRecord() {
        when(wareHouseStockRepository.lockStockBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(10));
        WareHouseStock ws = buildStock(10);
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));

        wareHouseStockService.increaseStock(WAREHOUSE_ID, SKU_ID, 5, buildContext());

        assertEquals(15, ws.getStock());

        ArgumentCaptor<StockRecord> captor = ArgumentCaptor.forClass(StockRecord.class);
        verify(stockRecordRepository).save(captor.capture());
        assertEquals(15, captor.getValue().getAfterQuantity());
        assertEquals(5, captor.getValue().getChangeQuantity());
    }

    // ========== 增加：仓库盘点中 ==========

    @Test
    void increaseStock_whenWareHouseUnderCheck_shouldThrow() {
        when(wareHouseStockRepository.lockStockBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(10));
        WareHouseStock ws = buildStock(10, CheckEnum.UNDER_CHECK);
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));

        assertThrows(BusinessException.class,
                () -> wareHouseStockService.increaseStock(WAREHOUSE_ID, SKU_ID, 5, buildContext()));

        assertEquals(10, ws.getStock());
        verify(stockRecordRepository, never()).save(any());
    }

    // ==================================================================
    // 造数据辅助方法
    // ==================================================================

    private WareHouseStock buildStock(int quantity) {
        return buildStock(quantity, CheckEnum.NO_CHECK);
    }

    private WareHouseStock buildStock(int quantity, CheckEnum checkStatus) {
        Product product = Product.builder()
                .productId(1L)
                .productCode("P001")
                .productName("测试商品")
                .season(SeasonEnum.SPRING)
                .type("男")
                .category("T恤")
                .unit("件")
                .composition("棉")
                .year("2026")
                .importPrice(BigDecimal.TEN)
                .salePrice(BigDecimal.valueOf(100))
                .build();

        ProductSku sku = ProductSku.builder()
                .skuId(SKU_ID)
                .skuCode("SKU001")
                .skuName("测试SKU")
                .product(product)
                .build();

        WareHouse wareHouse = WareHouse.builder()
                .wareHouseId(WAREHOUSE_ID)
                .wareHouseCode("WH001")
                .wareHouseName("测试仓库")
                .checkStatus(checkStatus)
                .build();

        return WareHouseStock.builder()
                .stockId(STOCK_ID)
                .productSku(sku)
                .wareHouse(wareHouse)
                .stock(quantity)
                .build();
    }

    private StockContext buildContext() {
        return StockContext.builder()
                .sourceNo("OR20260721TEST")
                .changeType(StockChangeTypeEnum.SALE_OUT)
                .sourceType(StockSourceTypeEnum.ORDER)
                .build();
    }
}
