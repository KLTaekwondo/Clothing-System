package com.superkl.backend.service.stock;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.dto.stock.StockContext;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.entity.product.Product;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.stock.StockRecord;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.repository.stock.StockRecordRepository;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
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

/**
 * 教学版测试：WareHouseStockService 的库存扣减规则
 *
 * 核心套路和 OrderServiceTest 一样：mock 依赖 → 造数据 → 调用 → 断言
 *
 * 这个类的重点是"防超卖"：
 * - decreaseStock() 是悲观锁方案：findByIdForUpdate() 是 SELECT ... FOR UPDATE
 * - 测试要验证：库存不足时抛异常，且绝不落库存流水（save 不被调用）
 */
@ExtendWith(MockitoExtension.class)
class WareHouseStockServiceTest {

    // ========== 第一步：声明依赖 ==========

    @Mock
    private WareHouseStockRepository wareHouseStockRepository;

    @Mock
    private StockRecordRepository stockRecordRepository;

    private WareHouseStockService wareHouseStockService;

    // 测试用常量
    private static final Long WAREHOUSE_ID = 1L;
    private static final Long SKU_ID = 100L;
    private static final Long STOCK_ID = 1000L;

    @BeforeEach
    void setUp() {
        wareHouseStockService = new WareHouseStockService(
                wareHouseStockRepository,
                stockRecordRepository
        );

        // 塞一个管理员身份（RequestUser.notNull() 需要）
        RequestUser currentUser = RequestUser.builder()
                .requestId(WAREHOUSE_ID)
                .requestRole("ROLE_ADMIN")
                .requestName("测试管理员")
                .requestCode("ADMIN001")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                // 三参构造器：带权限列表的 token 才是已认证状态，否则 RequestUser.notNull() 会抛"未获取到当前用户信息"
                new UsernamePasswordAuthenticationToken(currentUser, null, List.of())
        );
    }

    // ========== 用例 9：正常扣减 → 库存减少 + 流水落库 ==========

    @Test
    void decreaseStock_whenNormal_shouldDeductAndSaveRecord() {
        // given：库存 10 件
        WareHouseStock ws = buildStock(10);
        // findBySkuIdAndWarehouseId 第一次查库存（不加锁）
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));
        // findByIdForUpdate 加锁后再查一次（悲观锁）
        when(wareHouseStockRepository.findByIdForUpdate(STOCK_ID))
                .thenReturn(Optional.of(ws));

        // when：扣减 3 件
        wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 3, buildContext());

        // then：库存变成 7
        assertEquals(7, ws.getStock());

        // 关键断言：库存记录必须保存（save 被调用 1 次）
        verify(wareHouseStockRepository).save(ws);

        // 关键断言：流水也必须保存，且 before/after/change 正确
        ArgumentCaptor<StockRecord> captor = ArgumentCaptor.forClass(StockRecord.class);
        verify(stockRecordRepository).save(captor.capture());
        StockRecord record = captor.getValue();
        assertEquals(10, record.getBeforeQuantity());   // 扣减前 10
        assertEquals(7, record.getAfterQuantity());     // 扣减后 7
        assertEquals(-3, record.getChangeQuantity());   // 变化 -3
        assertEquals("OR20260721TEST", record.getSourceNo());
    }

    // ========== 用例 10：扣减数量 <= 0 → 抛异常 ==========

    @Test
    void decreaseStock_whenQuantityNonPositive_shouldThrow() {
        // given：什么都不用 mock（第一行就校验数量）
        // when + then：0 和负数都必须抛
        assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 0, buildContext()));
        assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, -5, buildContext()));

        // 关键：没查到库存，也没保存流水
        verify(wareHouseStockRepository, never()).findBySkuIdAndWarehouseId(any(), any());
        verify(stockRecordRepository, never()).save(any());
    }

    // ========== 用例 11：库存记录不存在 → 抛异常 ==========

    @Test
    void decreaseStock_whenStockNotFound_shouldThrow() {
        // given：查不到库存记录
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 3, buildContext()));
        assertTrue(ex.getMessage().contains("库存记录不存在"));
        // 绝不能走加锁查询（说明提前返回了）
        verify(wareHouseStockRepository, never()).findByIdForUpdate(any());
    }

    // ========== 用例 12：库存不足 → 抛异常，不落流水（防超卖核心） ==========

    @Test
    void decreaseStock_whenInsufficientStock_shouldThrowAndNotSave() {
        // given：库存只有 2 件，却要扣 5 件
        WareHouseStock ws = buildStock(2);
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));
        when(wareHouseStockRepository.findByIdForUpdate(STOCK_ID))
                .thenReturn(Optional.of(ws));

        // when + then：抛"库存不足"
        BusinessException ex = assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 5, buildContext()));
        assertTrue(ex.getMessage().contains("库存不足"));

        // 防超卖核心断言：库存没被改，流水也没落库
        assertEquals(2, ws.getStock());
        verify(wareHouseStockRepository, never()).save(any());
        verify(stockRecordRepository, never()).save(any());
    }

    // ========== 用例 13：加锁后记录消失 → 抛异常（防并发删库存） ==========

    @Test
    void decreaseStock_whenLockedRecordMissing_shouldThrow() {
        // given：第一次查到库存（乐观），加锁后却没了（被并发删除）
        WareHouseStock ws = buildStock(10);
        when(wareHouseStockRepository.findBySkuIdAndWarehouseId(SKU_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(ws));
        when(wareHouseStockRepository.findByIdForUpdate(STOCK_ID))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> wareHouseStockService.decreaseStock(WAREHOUSE_ID, SKU_ID, 3, buildContext()));
        assertTrue(ex.getMessage().contains("已锁定"));
    }

    // ==================================================================
    // 造数据辅助方法
    // ==================================================================

    // 造一条库存记录（关联好 SKU 和仓库，saveRecord 里要取 SKU/仓库信息）
    private WareHouseStock buildStock(int quantity) {
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
                .build();

        return WareHouseStock.builder()
                .stockId(STOCK_ID)
                .productSku(sku)
                .wareHouse(wareHouse)
                .stock(quantity)
                .build();
    }

    // 造一个库存上下文（decreaseStock 的参数）
    private StockContext buildContext() {
        return StockContext.builder()
                .sourceNo("OR20260721TEST")
                .changeType(StockChangeTypeEnum.SALE_OUT)
                .sourceType(StockSourceTypeEnum.ORDER)
                .build();
    }
}
