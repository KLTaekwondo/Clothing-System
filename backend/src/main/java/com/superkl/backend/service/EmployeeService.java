package com.superkl.backend.service;

import com.superkl.backend.converter.EmployeeConverter;
import com.superkl.backend.dto.EmployeeCreateDto;
import com.superkl.backend.dto.EmployeeUpdateDto;
import com.superkl.backend.entity.Employee;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.EmployeeInfo;
import com.superkl.backend.repository.EmployeeRepository;
import com.superkl.backend.repository.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final WareHouseRepository wareHouseRepository;

    // 新增员工
    @Transactional
    public void create(EmployeeCreateDto employeeCreateDto) {
        // 从仓库仓库ID查询仓库
        WareHouse wareHouse = wareHouseRepository.findById(employeeCreateDto.getWareHouseId())
                .orElseThrow(() -> new BusinessException("仓库不存在"));

        // 转换实体
        Employee employee = EmployeeConverter.toEntity(employeeCreateDto , wareHouse);
        employeeRepository.save(employee);
    }

    // 更新员工
    @Transactional
    public void update(Long employeeId, EmployeeUpdateDto employeeUpdateDto) {
        // 从员工ID查询员工
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("员工不存在"));

        // 判断一下是否更新了仓库
        Long newId = employeeUpdateDto.getWareHouseId();
        Long currentId = employee.getWareHouse().getWareHouseId();
        if (!newId.equals(currentId)) {
            // 从新仓库ID查询仓库
            WareHouse wareHouse = wareHouseRepository.findById(newId)
                    .orElseThrow(() -> new BusinessException("仓库不存在"));
            employee.setWareHouse(wareHouse);
        }
        // 转换器更新员工
        EmployeeConverter.updateEntity(employee, employeeUpdateDto);
        employeeRepository.save(employee);
    }

    // 删除员工（物理删除！极其严重操作！谨慎使用！）
    @Transactional
    public void delete(Long employeeId) {
        // 从员工ID查询员工
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        // 删除员工
        employeeRepository.deleteById(employeeId);
    }

    // 查询员工
    public EmployeeInfo search(Long employeeId) {
        // 从员工ID查询员工
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        // 转换信息
        return EmployeeConverter.toInfo(employee);
    }

    // 查询所有员工
    public List<EmployeeInfo> searchList() {
        // 查询所有员工
        List<Employee> employees = employeeRepository.findAll();
        // 转换信息
        return EmployeeConverter.toInfoList(employees);
    }
}
