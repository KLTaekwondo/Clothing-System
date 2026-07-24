package com.superkl.backend.converter.basic;


import com.superkl.backend.entity.basic.Admin;
import com.superkl.backend.info.basic.AdminInfo;

public class AdminConverter {
    private AdminConverter(){}  // 私有化，防止外部new对象

    // 转化单个管理员实体为管理员信息
    public static AdminInfo toInfo(Admin admin){
        return AdminInfo.builder()
                .id(admin.getAdminId())
                .name(admin.getUsername())
                .status(admin.getStatus())
                .code(admin.getAdminCode())
                .createTime(admin.getCreateTime())
                .updateTime(admin.getUpdateTime())
                .build();
    }
}
