package com.superkl.backend.info.order;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.enums.AuditStatusEnum;
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
public class StockCheckInfo extends BaseInfo {
    private String stockCheckNo;// 库存盘点订单编号
    private String wareHouseName;// 仓库快照名称
    private String wareHouseCode;// 仓库快照编码
    private AuditStatusEnum status;// 审核状态
    private String remark;// 备注
}
