package com.superkl.backend.info.basic;

import com.superkl.backend.enums.CheckEnum;
import com.superkl.backend.enums.StatusEnum;
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
public class WareHouseInfo extends BaseInfo {
    private String code;// 仓库编码
    private String name;// 仓库名称
    private StatusEnum status;// 仓库状态
    private CheckEnum checkStatus;// 仓库盘点状态
}
