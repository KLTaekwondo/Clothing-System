package com.superkl.backend.converter;

import com.superkl.backend.dto.WareHouseCreateDto;
import com.superkl.backend.dto.WareHouseUpdateDto;
import com.superkl.backend.entity.Admin;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.info.WareHouseInfo;

import java.util.List;

public class WareHouseConverter {
    private WareHouseConverter(){} // 私有化，防止外部实例化

    // 实体转Info
    public static WareHouseInfo toInfo(WareHouse wareHouse) {
        return WareHouseInfo.builder()
                .id(wareHouse.getWareHouseId())
                .code(wareHouse.getWareHouseCode())
                .name(wareHouse.getWareHouseName())
                .status(wareHouse.getStatus())
                .createTime(wareHouse.getCreateTime())
                .updateTime(wareHouse.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<WareHouseInfo> toInfoList(List<WareHouse> wareHouseList) {
        return wareHouseList.stream()
                .map(WareHouseConverter::toInfo)
                .toList();
    }

    // Dto转实体
    public static WareHouse toEntity(WareHouseCreateDto wareHouseCreateDto , Admin admin) {
        return WareHouse.builder()
                .wareHouseCode(wareHouseCreateDto.getCode())
                .wareHouseName(wareHouseCreateDto.getName())
                .admin(admin)
                .build();
    }

    // Dto更新实体
    public static void updateEntity(WareHouse wareHouse, WareHouseUpdateDto wareHouseUpdateDto) {
        wareHouse.setWareHouseCode(wareHouseUpdateDto.getCode());
        wareHouse.setWareHouseName(wareHouseUpdateDto.getName());
        wareHouse.setStatus(wareHouseUpdateDto.getStatus());
    }
}
