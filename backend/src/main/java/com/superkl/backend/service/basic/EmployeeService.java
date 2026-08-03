package com.superkl.backend.service.basic;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.basic.EmployeeConverter;
import com.superkl.backend.dto.basic.EmployeeCreateDto;
import com.superkl.backend.dto.basic.EmployeeUpdateDto;
import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.basic.EmployeeInfo;
import com.superkl.backend.repository.basic.EmployeeRepository;
import com.superkl.backend.repository.basic.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
                .orElseThrow(() -> new BusinessException(403, "仓库不存在！"));

        // 检查员工编号是否已经存在
        if(employeeRepository.existsByCode(employeeCreateDto.getCode())){
            throw new BusinessException(403, "员工编号已存在！");
        }

        // 转换实体
        Employee employee = EmployeeConverter.toEntity(employeeCreateDto , wareHouse);
        employeeRepository.save(employee);
        log.info("新增员工：{}，所属仓库：{}", employee.getEmployeeName(), wareHouse.getWareHouseName());
        RequestUser.log();
    }

    // 更新员工
    @Transactional
    public void update(Long employeeId, EmployeeUpdateDto employeeUpdateDto) {
        // 从员工ID查询员工
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(403, "员工不存在"));

        // 检查是否和其他的员工编号重复
        String code = employeeUpdateDto.getCode();
        if(employeeRepository.existsByCode(code) && !code.equals(employee.getEmployeeCode())){
            throw new BusinessException(403, "员工编号已存在！");
        }

        // 判断一下是否更新了仓库
        Long newId = employeeUpdateDto.getWareHouseId();
        Long currentId = employee.getWareHouse().getWareHouseId();
        if (!newId.equals(currentId)) {
            // 从新仓库ID查询仓库
            WareHouse wareHouse = wareHouseRepository.findById(newId)
                    .orElseThrow(() -> new BusinessException(403, "仓库不存在"));
            employee.setWareHouse(wareHouse);
        }
        // 转换器更新员工
        EmployeeConverter.updateEntity(employee, employeeUpdateDto);
        employeeRepository.save(employee);
        log.info("更新员工：{}", employee.getEmployeeName());
        RequestUser.log();
    }

    // 删除员工（物理删除！极其严重操作！谨慎使用！）
    @Transactional
    public void delete(Long employeeId) {
        // 从员工ID查询员工
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(403, "员工不存在"));
        // 禁用员工
        employee.setStatus(StatusEnum.DISABLE);
        employeeRepository.save(employee);
        log.info("禁用员工：{}", employee.getEmployeeName());
        RequestUser.log();
    }

    // 查询员工
    @Transactional(readOnly = true)
    public EmployeeInfo search(Long employeeId) {
        // 从员工ID查询员工
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(403, "员工不存在"));
        // 转换信息
        return EmployeeConverter.toInfo(employee);
    }

    // 查询所有员工
    @Transactional(readOnly = true)
    public List<EmployeeInfo> searchList() {
        // 查询所有员工
        List<Employee> employees = employeeRepository.findAll();
        // 转换信息
        return EmployeeConverter.toInfoList(employees);
    }

    // 收银前端验证员工
    @Transactional(readOnly = true)
    public EmployeeInfo verify(Long employeeId) {
        // 从员工ID查询员工
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(403, "员工不存在"));
        // 验证员工是否启用
        if(!employee.isEnabled()){
            throw new BusinessException(405, "员工已禁用!");
        }
        // 验证是否属于这个仓库
        Long wareHouseId = employee.getWareHouse().getWareHouseId();
        if(!RequestUser.isAdmin() && !RequestUser.isCurrentWareHouse(wareHouseId)){
            throw new BusinessException(403, "您没有权限操作该仓库的员工！");
        }
        // 转换信息
        return EmployeeConverter.toInfo(employee);
    }

    // 收银前端查询所有属于该仓库的员工
    @Transactional(readOnly = true)
    public List<EmployeeInfo> verifyList() {
        // 查询所有员工
        // 从RequestUser中获取当前仓库ID
        Long wareHouseId = RequestUser.notNull().getRequestId();
        List<Employee> employees = employeeRepository.findByWareHouseId(wareHouseId, StatusEnum.ENABLE);
        // 转换信息
        return EmployeeConverter.toInfoList(employees);
    }

}
