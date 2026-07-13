package com.superkl.backend.service;

import com.superkl.backend.converter.WareHouseStockConverter;
import com.superkl.backend.dto.StockTransferDto;
import com.superkl.backend.dto.WarehouseStockUpdateDto;
import com.superkl.backend.entity.WareHouseStock;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.WareHouseStockInfo;
import com.superkl.backend.repository.WareHouseStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WareHouseStockService {
    private final WareHouseStockRepository wareHouseStockRepository;

    // 修改仓库某一件个SKU的库存
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
    @Transactional
    public void transfer(StockTransferDto dto) {
        // 1. 校验参数
        Long skuId = dto.getSkuId();
        Long targetWarehouseId = dto.getTargetWarehouseId();
        Long sourceWarehouseId = dto.getSourceWarehouseId();
        Integer stock = dto.getStock();

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

    @Transactional
    public void decreaseStock(Long wareHouseId, Long skuId, Integer stock) {
        WareHouseStock ws = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, wareHouseId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));

        if (ws.getStock() - stock < 0) {
            throw new BusinessException("库存不足");
        }
        ws.setStock(ws.getStock() - stock);
        wareHouseStockRepository.save(ws);
    }

    @Transactional
    public void increaseStock(Long wareHouseId, Long skuId, Integer stock) {
        WareHouseStock ws = wareHouseStockRepository.findBySkuIdAndWarehouseId(skuId, wareHouseId)
                .orElseThrow(() -> new BusinessException("库存记录不存在"));
        ws.setStock(ws.getStock() + stock);
        wareHouseStockRepository.save(ws);
    }
}
