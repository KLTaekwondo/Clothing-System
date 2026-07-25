package com.superkl.backend.info.stock;

import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import com.superkl.backend.info.BaseInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StockRecordInfo extends BaseInfo {
    private String sourceNo;// 来源订单号
    private StockSourceTypeEnum sourceType;// 来源类型
    private StockChangeTypeEnum changeType;// 变动类型
    private String productCode;// 商品编码
    private String productName;// 商品名称
    private String skuCode;// sku编码
    private String skuName;// sku名称
    private String wareHouseCode;// 仓库编码
    private String wareHouseName;// 仓库名称
    private Integer changeQuantity;// 变动数量
    private Integer beforeQuantity; // 变动前库存
    private Integer afterQuantity;// 变动后库存
    private String operationCode;// 操作人编码
    private String operationName;// 操作人名称
    private String operationRole;// 操作人角色
}
