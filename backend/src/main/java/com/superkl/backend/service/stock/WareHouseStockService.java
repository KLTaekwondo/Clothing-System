package com.superkl.backend.service.stock;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.stock.StockRecordConverter;
import com.superkl.backend.converter.stock.WareHouseStockConverter;
import com.superkl.backend.dto.stock.StockContext;
import com.superkl.backend.dto.stock.WarehouseStockUpdateDto;
import com.superkl.backend.entity.order.StockCheckItem;
import com.superkl.backend.entity.stock.StockRecord;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.stock.WareHouseStockInfo;
import com.superkl.backend.repository.stock.StockRecordRepository;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WareHouseStockService {
    private final WareHouseStockRepository wareHouseStockRepository;
    private final StockRecordRepository stockRecordRepository;

    // 修改仓库某一件个SKU的库存
    @Transactional
    public void batchUpdate(List<WarehouseStockUpdateDto> dtos) {
        for (WarehouseStockUpdateDto dto : dtos) {
            WareHouseStock ws = wareHouseStockRepository.findByStockId(dto.getStockId())
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存记录不存在"));
            Integer beforeQuantity = ws.getStock();
            Integer afterQuantity = dto.getStock();
            Integer changeQuantity = afterQuantity - beforeQuantity;

            // 检查库存数量是否小于0
            if(afterQuantity < 0) {
                throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "库存数量不得少于0");
            }

            // 如果库存数量没有变化，不保存库存记录
            if(changeQuantity.compareTo(0) == 0) {
                continue;
            }
            StockContext stockContext = StockContext.builder()
                    .sourceNo(generateManualSourceNo(dto.getStockId()))
                    .changeType(StockChangeTypeEnum.MANUAL_ADJUST)
                    .sourceType(StockSourceTypeEnum.MANUAL_ADJUST)
                    .build();

            ws.setStock(afterQuantity);
            // 保存库存记录
            saveRecord(ws, beforeQuantity, afterQuantity, changeQuantity, stockContext);
            wareHouseStockRepository.save(ws);
            log.info("管理员手动修改库存：stockId={}，新库存={}", dto.getStockId(), dto.getStock());
            RequestUser.log();
        }
    }

    // 查询某一个商品的某一个仓库的库存记录
    @Transactional(readOnly = true)
    public List<WareHouseStockInfo> findByWareHouseIdAndProductId(Long warehouseId, Long productId) {
        if (!RequestUser.isCurrentWareHouse(warehouseId) && !RequestUser.isAdmin()) {
            throw new BusinessException(ErrorCodeEnum.FORBIDDEN, "您没有权限查询该仓库的库存记录");
        }
        return WareHouseStockConverter.toInfoList(wareHouseStockRepository.findByProductIdAndWareHouseId(productId, warehouseId));
    }

    // 分页查询某一个仓库的商品库存记录
    @Transactional(readOnly = true)
    public PageResult<WareHouseStockInfo> findByWareHouseId(Long warehouseId, Pageable pageable) {
        if (!RequestUser.isCurrentWareHouse(warehouseId) && !RequestUser.isAdmin()) {
            throw new BusinessException(ErrorCodeEnum.FORBIDDEN, "您没有权限查询该仓库的库存记录");
        }
        Page<WareHouseStock> wareHouseStockPage = wareHouseStockRepository.findPageByWareHouseId(warehouseId, pageable);
        return WareHouseStockConverter.toInfoPage(wareHouseStockPage);
    }

    // 减少库存
    @Transactional
    public void decreaseStock(Long wareHouseId, Long skuId, Integer stock , StockContext stockContext) {
        // 检查库存数量是否大于0
        if (stock == null || stock <= 0) {
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "库存数量必须大于0");
        }

        // 查询库存记录是否存在
        WareHouseStock ws = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, wareHouseId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存记录不存在"));

        // 并发锁定
        ws = wareHouseStockRepository.findByIdForUpdate(ws.getStockId())
                .orElseThrow(()-> new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "该库存记录已锁定"));

        // 检查库存是否足够
        Integer beforeQuantity = ws.getStock();
        Integer afterQuantity = beforeQuantity - stock;
        if (afterQuantity < 0) {
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, ws.getProductSku().getSkuName()+"库存不足");
        }
        // 减少库存
        ws.setStock(afterQuantity);
        // 保存库存记录
        saveRecord(ws, beforeQuantity, afterQuantity, -stock, stockContext);
        wareHouseStockRepository.save(ws);
        log.info("扣减库存：仓库{}，SKU{}，数量{}，剩余{}", wareHouseId, skuId, stock, afterQuantity);
        RequestUser.log();
    }

    // 增加库存
    @Transactional
    public void increaseStock(Long wareHouseId, Long skuId, Integer stock, StockContext stockContext) {
        // 检查库存数量是否大于0
        if (stock == null || stock <= 0) {
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "库存数量必须大于0");
        }

        // 查询库存记录是否存在
        WareHouseStock ws = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, wareHouseId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存记录不存在"));

        // 锁定库存
        ws = wareHouseStockRepository.findByIdForUpdate(ws.getStockId())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "该库存记录已锁定！"));
        // 增加库存
        Integer beforeQuantity = ws.getStock();
        Integer afterQuantity = beforeQuantity + stock;
        ws.setStock(afterQuantity);
        // 保存库存记录
        saveRecord(ws, beforeQuantity, afterQuantity, stock, stockContext);
        wareHouseStockRepository.save(ws);
        log.info("增加库存：仓库{}，SKU{}，数量{}，剩余{}", wareHouseId, skuId, stock, afterQuantity);
        RequestUser.log();
    }


    // 盘点更新库存数量，区别于手动调整
    @Transactional
    public void checkUpdateStock(Long stockId, StockCheckItem item , StockContext stockContext) {
        // 查询库存记录是否存在(这里直接使用stockId查找，精确，不像上面需要两个定位独立的字段)
        WareHouseStock ws = wareHouseStockRepository.findByStockId(stockId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存记录不存在"));

        // 检查actualQuantity是否小于0
        if(item.getActualQuantity() < 0) {
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "实际数量不得少于0");
        }

        // 检查systemQuantity是否小于0
        if(item.getSystemQuantity() < 0) {
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "系统数量不得少于0");
        }

        // 更新库存
        ws.setStock(item.getActualQuantity());
        // 保存库存记录
        saveRecord(ws, item.getSystemQuantity(), item.getActualQuantity(), item.getDiffQuantity(), stockContext);
        wareHouseStockRepository.save(ws);
        log.info("盘点更新库存：stockId={}，新库存={}", stockId, item.getActualQuantity());
        RequestUser.log();
    }

    // 保存库存记录
    private void saveRecord(WareHouseStock ws , Integer beforeQuantity,
                            Integer afterQuantity ,Integer changeQuantity, StockContext stockContext) {
        // 获取当前操作人信息
        RequestUser user = RequestUser.notNull();

        if(stockContext == null){
            throw new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "未获取到库存上下文");
        }

        // 保存库存记录
        StockRecord stockRecord = StockRecordConverter.toEntity(stockContext);
        stockRecord.setBeforeQuantity(beforeQuantity);
        stockRecord.setAfterQuantity(afterQuantity);
        stockRecord.setChangeQuantity(changeQuantity);
        stockRecord.setProductCode(ws.getProductSku().getProduct().getProductCode());
        stockRecord.setProductName(ws.getProductSku().getProduct().getProductName());
        stockRecord.setSkuCode(ws.getProductSku().getSkuCode());
        stockRecord.setSkuName(ws.getProductSku().getSkuName());
        stockRecord.setWareHouseCode(ws.getWareHouse().getWareHouseCode());
        stockRecord.setWareHouseName(ws.getWareHouse().getWareHouseName());
        stockRecord.setOperationCode(user.getRequestCode());
        stockRecord.setOperationName(user.getRequestName());
        stockRecord.setOperationRole(user.getRequestRole());
        stockRecordRepository.save(stockRecord);
    }

    // 手动修改库存，生成订单号
    private String generateManualSourceNo(Long stockId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid =  UUID.randomUUID().toString().replace("-", "");
        return "MANUAL"+ date + stockId + uuid;
    }
}
