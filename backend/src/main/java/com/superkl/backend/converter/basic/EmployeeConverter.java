package com.superkl.backend.converter.basic;

import com.superkl.backend.dto.basic.EmployeeCreateDto;
import com.superkl.backend.dto.basic.EmployeeUpdateDto;
import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.info.basic.EmployeeInfo;

import java.util.List;

public class EmployeeConverter {
    private EmployeeConverter() {} // 私有化，防止外部创建对象

    // 实体转Info
    public static EmployeeInfo toInfo(Employee employee) {
        return EmployeeInfo.builder()
                .id(employee.getEmployeeId())
                .name(employee.getEmployeeName())
                .code(employee.getEmployeeCode())
                .wareHouseCode(employee.getWareHouse().getWareHouseCode())
                .wareHouseName(employee.getWareHouse().getWareHouseName())
                .status(employee.getStatus())
                .createTime(employee.getCreateTime())
                .updateTime(employee.getUpdateTime())
                .build();
    }

    // 实体列表转Info列表
    public static List<EmployeeInfo> toInfoList(List<Employee> employees) {
        return employees.stream()
                .map(EmployeeConverter::toInfo)
                .toList();
    }

    // DTO转实体
    public static Employee toEntity(EmployeeCreateDto employeeCreateDto , WareHouse wareHouse) {
        return Employee.builder()
                .employeeCode(employeeCreateDto.getCode())
                .employeeName(employeeCreateDto.getName())
                .wareHouse(wareHouse)
                .build();
    }

    // Dto更新实体
    public static void updateEntity(Employee employee, EmployeeUpdateDto employeeUpdateDto) {
        employee.setEmployeeCode(employeeUpdateDto.getCode());
        employee.setEmployeeName(employeeUpdateDto.getName());
        employee.setStatus(employeeUpdateDto.getStatus());
    }
}
