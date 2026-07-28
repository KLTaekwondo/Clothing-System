package com.superkl.backend.converter.basic;

import com.superkl.backend.dto.basic.SupplierCreateDto;
import com.superkl.backend.dto.basic.SupplierUpdateDto;
import com.superkl.backend.entity.basic.Admin;
import com.superkl.backend.entity.basic.Supplier;
import com.superkl.backend.info.basic.SupplierInfo;

import java.util.List;

public class SupplierConverter {
    private SupplierConverter(){} //私有化构造函数，防止外部实例化

    // 实体转Info
    public static SupplierInfo toInfo(Supplier supplier){
        return SupplierInfo.builder()
                .id(supplier.getSupplierId())
                .supplierName(supplier.getSupplierName())
                .supplierCode(supplier.getSupplierCode())
                .contactPhone(supplier.getContactPhone())
                .status(supplier.getStatus())
                .remark(supplier.getRemark())
                .createTime(supplier.getCreateTime())
                .updateTime(supplier.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<SupplierInfo> toInfoList(List<Supplier> suppliers){
        return suppliers.stream()
                .map(SupplierConverter::toInfo)
                .toList();
    }

    // dto转实体
    public static Supplier toEntity(SupplierCreateDto dto , Admin admin){
        return Supplier.builder()
                .supplierName(dto.getSupplierName())
                .supplierCode(dto.getSupplierCode())
                .contactPhone(dto.getContactPhone())
                .remark(dto.getRemark())
                .admin(admin)
                .build();
    }

    // dto更新实体
    public static void updateEntity(Supplier supplier , SupplierUpdateDto dto ){
        supplier.setSupplierName(dto.getSupplierName());
        supplier.setSupplierCode(dto.getSupplierCode());
        supplier.setContactPhone(dto.getContactPhone());
        supplier.setStatus(dto.getStatus());
        supplier.setRemark(dto.getRemark());
        supplier.setStatus(dto.getStatus());
    }
}
