package com.superkl.backend.service.order;

import com.superkl.backend.converter.order.StockCheckItemConverter;
import com.superkl.backend.dto.order.StockCheckItemCreateDto;
import com.superkl.backend.entity.order.StockCheck;
import com.superkl.backend.entity.order.StockCheckItem;
import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.StockCheckItemInfo;
import com.superkl.backend.repository.order.StockCheckItemRepository;
import com.superkl.backend.repository.product.ProductSkuRepository;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockCheckItemService {
    private final StockCheckItemRepository stockCheckItemRepository;
    private final ProductSkuRepository productSkuRepository;
    private final WareHouseStockRepository wareHouseStockRepository;

    // 创建盘点项
    public StockCheckItem create(StockCheckItemCreateDto dto , StockCheck stockCheck , List<String> errors) {
        Long targetWareHouseId = stockCheck.getTargetWarehouse().getWareHouseId();
        // 先查找需要盘点的商品sku
        ProductSku productSku = productSkuRepository.findBySkuCode(dto.getSkuCode()).orElse(null);
        if(productSku == null) {
            errors.add("商品编码["+dto.getSkuCode()+"]不存在");
            return null;
        }
        // 查出系统中该商铺记录的商品数量
        WareHouseStock ws = wareHouseStockRepository.findBySkuAndWareHouse(productSku.getSkuId(), targetWareHouseId).orElse(null);
        if(ws == null) {
            errors.add("商品编码["+dto.getSkuCode()+"]的库存记录在仓库["+stockCheck.getTargetWarehouse().getWareHouseName()+"]中不存在");
            return null;
        }

        String skuCode = productSku.getSkuCode();
        String productName = productSku.getProduct().getProductName();
        String productCode = productSku.getProduct().getProductCode();
        // 检查商品状态是否正常
        if (!productSku.getProduct().isEnabled()) {
            errors.add(productName+"商品["+productCode+"]并未启用，无法进行盘点");
        }

        if(!productSku.isEnabled()) {
            errors.add(productName+"商品["+productCode+"]的SKU["+skuCode+"]未启用，无法进行盘点");
        }
        // 存在错误直接返回null
        if(!errors.isEmpty()){
            return null;
        }

        // 记录数据
        StockCheckItem item = StockCheckItemConverter.toEntity(dto, productSku, ws);
        item.setStockCheck(stockCheck);
        return item;
    }

    // 创建盘点项列表
    public List<StockCheckItem> createList(List<StockCheckItemCreateDto> dtos, StockCheck stockCheck) {
        List<StockCheckItem> stockCheckItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        // 校验商品编码是否重复
        Set<String> checkedCodes = new HashSet<>();
        for(StockCheckItemCreateDto dto : dtos) {
            // 如果重复直接跳过，不要直接暴力甩出异常
            if(!checkedCodes.add(dto.getSkuCode())) {
                continue;
            }
            // 创建盘点项并添加到列表中
            StockCheckItem item = create(dto, stockCheck, errors);

            if(item!=null) {
                stockCheckItems.add(item);
            }
        }

        if(!errors.isEmpty()) {
            throw new BusinessException(ErrorCodeEnum.RULE_ITEM_FORBIDDEN, String.join("\n", errors));
        }

        return stockCheckItems;
    }

    // 根据盘点单Id查询盘点项列表
    @Transactional(readOnly = true)
    public List<StockCheckItemInfo> findByStockCheckId(Long stockCheckId) {
        List<StockCheckItem> stockCheckItemList = stockCheckItemRepository.listByStockCheck(stockCheckId);
        return StockCheckItemConverter.toInfoList(stockCheckItemList);
    }

    // 更新盘点项
    @Transactional
    public void updateDelete(Long stockCheckId) {
        List<StockCheckItem> stockCheckItemList = stockCheckItemRepository.listByStockCheck(stockCheckId);
        stockCheckItemRepository.deleteAll(stockCheckItemList);
    }
}
