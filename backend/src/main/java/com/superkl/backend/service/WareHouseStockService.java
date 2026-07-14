package com.superkl.backend.service;

import com.superkl.backend.converter.WareHouseStockConverter;
import com.superkl.backend.dto.StockTransferDto;
import com.superkl.backend.dto.WarehouseStockUpdateDto;
import com.superkl.backend.entity.WareHouseStock;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.WareHouseStockInfo;
import com.superkl.backend.repository.WareHouseStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                    .orElseThrow(() -> new BusinessException("库存记录不存在"));
            ws.setStock(dto.getStock());
            wareHouseStockRepository.save(ws);
        }
    }

    // 查询某一个商品的某一个仓库的库存记录
    public List<WareHouseStockInfo> findByWareHouseIdAndProductId(Long warehouseId, Long productId) {
        return WareHouseStockConverter.toInfoList(wareHouseStockRepository.findByProductIdAndWareHouseId(productId, warehouseId));
    }

    // 调货操作
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void transfer(StockTransferDto dto) {
        // 1. 校验参数
        Long skuId = dto.getSkuId();
        Long targetWarehouseId = dto.getTargetWarehouseId();
        Long sourceWarehouseId = dto.getSourceWarehouseId();
        Integer stock = dto.getStock();

        if(sourceWarehouseId.equals(targetWarehouseId)) {
            throw new BusinessException("源仓库和目标仓库不能相同");
        }

        if(stock <= 0) {
            throw new BusinessException("调货库存数量不得小于等于0");
        }

        WareHouseStock sourceWs = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, sourceWarehouseId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));
        WareHouseStock targetWs = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, targetWarehouseId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));

        if (sourceWs.getStock() < stock) {
            throw new BusinessException("库存不足");
        }
        sourceWs.setStock(sourceWs.getStock() - stock);
        targetWs.setStock(targetWs.getStock() + stock);
        wareHouseStockRepository.save(sourceWs);
        wareHouseStockRepository.save(targetWs);
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
                .orElseThrow(() -> new BusinessException("库存记录不存在"));

        // 检查库存是否足够
        if (ws.getStock() - stock < 0) {
            throw new BusinessException("库存不足");
        }
        // 减少库存
        ws.setStock(ws.getStock() - stock);
        wareHouseStockRepository.save(ws);
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
                .orElseThrow(() -> new BusinessException("库存记录不存在"));

        // 增加库存
        ws.setStock(ws.getStock() + stock);
        wareHouseStockRepository.save(ws);
    }
}
