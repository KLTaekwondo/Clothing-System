package com.superkl.backend.converter;

import com.superkl.backend.dto.EmployeeCreateDto;
import com.superkl.backend.dto.EmployeeUpdateDto;
import com.superkl.backend.entity.Employee;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.info.EmployeeInfo;

import java.util.List;
import java.util.Set;

public class EmployeeConverter {
    private EmployeeConverter() {} // 私有化，防止外部创建对象

    // 实体转Info
    public static EmployeeInfo toInfo(Employee employee) {
        return EmployeeInfo.builder()
                .id(employee.getEmployeeId())
                .name(employee.getEmployeeName())
                .code(employee.getEmployeeCode())
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
