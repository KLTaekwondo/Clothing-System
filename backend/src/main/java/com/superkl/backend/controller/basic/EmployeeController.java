package com.superkl.backend.controller.basic;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.basic.EmployeeCreateDto;
import com.superkl.backend.dto.basic.EmployeeUpdateDto;
import com.superkl.backend.info.basic.EmployeeInfo;
import com.superkl.backend.service.basic.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")// 纯RESTful风格("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    // 1.新增员工
    // 纯RESTful风格
    // @PostMapping
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody EmployeeCreateDto employeeCreateDto) {
        employeeService.create(employeeCreateDto);
        return Result.successMessage("新增员工成功");
    }

    // 2.更新员工
    // 纯RESTful风格
    // @PutMapping("/{id}")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDto employeeUpdateDto) {
        employeeService.update(id, employeeUpdateDto);
        return Result.successMessage("更新员工成功");
    }

    // 3.删除员工
    // 纯RESTful风格
    // @DeleteMapping("/{id}")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.successMessage("删除员工成功");
    }

    // 4.查询单个员工
    // 纯RESTful风格
    // @GetMapping("/{id}")
    @GetMapping("/search/{id}")
    public Result<EmployeeInfo> search(@PathVariable Long id) {
        return Result.success(employeeService.search(id));
    }

    // 5.查询整个员工列表
    // 纯RESTful风格
    // @GetMapping
    @GetMapping("/search/list")
    public Result<List<EmployeeInfo>> searchList() {
        return Result.success(employeeService.searchList());
    }

    // 6.收银前端验证员工
    // 纯RESTful风格
    // @GetMapping("/{id}")
    @GetMapping("/verify/{id}")
    public Result<EmployeeInfo> verify(@PathVariable Long id) {
        return Result.success(employeeService.verify(id));
    }

    // 7.收银前端查询所有属于该仓库的员工
    // 纯RESTful风格
    // @GetMapping("/verify/list")
    @GetMapping("/verify/list")
    public Result<List<EmployeeInfo>> verifyList() {
        return Result.success(employeeService.verifyList());
    }
}
