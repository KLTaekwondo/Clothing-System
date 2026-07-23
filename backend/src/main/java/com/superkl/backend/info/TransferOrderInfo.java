package com.superkl.backend.info;

import com.superkl.backend.enums.AuditStatusEnum;
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
public class TransferOrderInfo extends BaseInfo {
    private String transferOrderNo; // 转移订单编号
    private String sourceWareHouseName; // 源仓库名称(快照，用于记录当时的仓库名称)
    private String targetWareHouseName; // 目标仓库名称(快照，用于记录当时的仓库名称)
    private AuditStatusEnum status; // 转移订单状态
    private String remark; // 备注
    private BigDecimal totalPrice; // 商品总价
}
