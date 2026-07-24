package com.superkl.backend.info.basic;

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
public class EmployeeInfo extends BaseInfo {
    private String name; // 员工姓名
    private String code; // 员工编码
    private StatusEnum status; // 员工状态
}
