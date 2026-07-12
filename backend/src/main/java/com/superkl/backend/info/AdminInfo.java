package com.superkl.backend.info;

import com.superkl.backend.enums.StatusEnum;
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
public class AdminInfo extends BaseInfo{
    private String name;// 管理员姓名
    private String code;// 管理员账号
    private StatusEnum status;// 管理员状态
}
