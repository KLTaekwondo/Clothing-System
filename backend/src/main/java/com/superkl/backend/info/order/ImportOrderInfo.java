package com.superkl.backend.info.order;

import com.superkl.backend.enums.AuditStatusEnum;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.info.BaseInfo;
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
    private AuditStatusEnum status;// 进货订单状态
    private String wareHouseName;// 仓库名称
    private DirectionEnum direction;// 业务方向
}
