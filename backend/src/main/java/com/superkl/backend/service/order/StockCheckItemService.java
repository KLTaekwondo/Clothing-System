package com.superkl.backend.service.order;

import com.superkl.backend.converter.order.StockCheckItemConverter;
import com.superkl.backend.dto.order.StockCheckItemCreateDto;
import com.superkl.backend.entity.order.StockCheck;
import com.superkl.backend.entity.order.StockCheckItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.StockCheckItemInfo;
import com.superkl.backend.repository.order.StockCheckItemRepository;
import com.superkl.backend.repository.product.ProductSkuRepository;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockCheckItemService {
    private final StockCheckItemRepository stockCheckItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final WareHouseStockRepository wareHouseStockRepository;

    // 创建盘点项
    @Transactional
    public StockCheckItem create(StockCheckItemCreateDto dto , StockCheck stockCheck) {
        Long targetWareHouseId = stockCheck.getTargetWarehouse().getWareHouseId();
        // 先查找需要盘点的商品sku
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode())
                .orElseThrow(() -> new BusinessException("商品编码不存在"));
        // 查出系统中该商铺记录的商品数量
        WareHouseStock ws = wareHouseStockRepository.findBySkuIdAndWarehouseId(productSku.getSkuId(), targetWareHouseId)
                .orElseThrow(() -> new BusinessException("商品库存记录不存在"));

        // 检查商品状态是否正常
        if (!productSku.getProduct().isEnabled()) {
            throw new BusinessException(403, "商品并未启用，无法进行盘点");
        }
        if(!productSku.isEnabled()) {
            throw new BusinessException(403, "改商品编码未启用，无法进行盘点");
        }

        // 记录数据
        StockCheckItem item = StockCheckItemConverter.toEntity(dto, productSku, ws);
        item.setStockCheck(stockCheck);
        return item;
    }

    // 创建盘点项列表
    @Transactional
    public List<StockCheckItem> createList(List<StockCheckItemCreateDto> dtos, StockCheck stockCheck) {
        List<StockCheckItem> stockCheckItems = new ArrayList<>();
        for(StockCheckItemCreateDto dto : dtos) {
            stockCheckItems.add(create(dto, stockCheck));
        }
        return stockCheckItems;
    }

    // 根据盘点单Id查询盘点项列表
    public List<StockCheckItemInfo> findByStockCheckId(Long stockCheckId) {
        List<StockCheckItem> stockCheckItemList = stockCheckItemRepository.findByStockCheckId(stockCheckId);
        return StockCheckItemConverter.toInfoList(stockCheckItemList);
    }

    // 更新盘点项
    public void updateDelete(Long stockCheckId) {
        List<StockCheckItem> stockCheckItemList = stockCheckItemRepository.findByStockCheckId(stockCheckId);
        stockCheckItemRepository.deleteAll(stockCheckItemList);
    }
}
