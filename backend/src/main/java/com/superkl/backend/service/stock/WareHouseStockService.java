package com.superkl.backend.service.stock;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.stock.WareHouseStockConverter;
import com.superkl.backend.dto.stock.WarehouseStockUpdateDto;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.stock.WareHouseStockInfo;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WareHouseStockService {
    private final WareHouseStockRepository wareHouseStockRepository;

    // 修改仓库某一件个SKU的库存
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void batchUpdate(List<WarehouseStockUpdateDto> dtos) {
        for (WarehouseStockUpdateDto dto : dtos) {
            WareHouseStock ws = wareHouseStockRepository.findById(dto.getStockId())
                    .orElseThrow(() -> new BusinessException(403, "库存记录不存在"));
            ws.setStock(dto.getStock());
            wareHouseStockRepository.save(ws);
            log.info("管理员手动修改库存：stockId={}，新库存={}", dto.getStockId(), dto.getStock());
            RequestUser.log();
        }
    }

    // 查询某一个商品的某一个仓库的库存记录
    public List<WareHouseStockInfo> findByWareHouseIdAndProductId(Long warehouseId, Long productId) {
        if (!RequestUser.isCurrentWareHouse(warehouseId) && !RequestUser.isAdmin()) {
            throw new BusinessException(403, "您没有权限查询该仓库的库存记录");
        }
        return WareHouseStockConverter.toInfoList(wareHouseStockRepository.findByProductIdAndWareHouseId(productId, warehouseId));
    }

    // 减少库存
    @Transactional
    public void decreaseStock(Long wareHouseId, Long skuId, Integer stock) {
        // 检查库存数量是否大于0
        if (stock == null || stock <= 0) {
            throw new BusinessException("库存数量必须大于0");
        }

        // 查询库存记录是否存在
        WareHouseStock ws = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, wareHouseId)
                .orElseThrow(() -> new BusinessException(403, "库存记录不存在"));

        // 检查库存是否足够
        if (ws.getStock() - stock < 0) {
            throw new BusinessException("库存不足");
        }
        // 减少库存
        ws.setStock(ws.getStock() - stock);
        wareHouseStockRepository.save(ws);
        log.info("扣减库存：仓库{}，SKU{}，数量{}，剩余{}", wareHouseId, skuId, stock, ws.getStock());
        RequestUser.log();
    }

    // 增加库存
    @Transactional
    public void increaseStock(Long wareHouseId, Long skuId, Integer stock) {
        // 检查库存数量是否大于0
        if (stock == null || stock <= 0) {
            throw new BusinessException("库存数量必须大于0");
        }

        // 查询库存记录是否存在
        WareHouseStock ws = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, wareHouseId)
                .orElseThrow(() -> new BusinessException(403, "库存记录不存在"));

        // 增加库存
        ws.setStock(ws.getStock() + stock);
        wareHouseStockRepository.save(ws);
        log.info("增加库存：仓库{}，SKU{}，数量{}，剩余{}", wareHouseId, skuId, stock, ws.getStock());
        RequestUser.log();
    }
}
