package com.superkl.backend.info;

import com.superkl.backend.enums.ImportOrderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ImportOrderInfo extends BaseInfo {
    private String importOrderNo;// 进货订单编号;
    private String supplierName;// 快照供应商名字
    private String remark;// 备注
    private BigDecimal totalAmount;// 总金额
    private ImportOrderEnum status;// 进货订单状态
    private String wareHouseName;// 仓库名称
}
