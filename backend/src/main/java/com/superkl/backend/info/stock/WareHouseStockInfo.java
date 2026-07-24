package com.superkl.backend.info.stock;

import com.superkl.backend.info.BaseInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WareHouseStockInfo extends BaseInfo {
    private Map<String , String> spec;// 规格参数
    private Integer stock;// 库存数量
}
