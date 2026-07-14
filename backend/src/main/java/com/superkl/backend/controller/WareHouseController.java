package com.superkl.backend.controller;

import com.superkl.backend.common.Result;
import com.superkl.backend.dto.LoginDto;
import com.superkl.backend.dto.WareHouseCreateDto;
import com.superkl.backend.dto.WareHouseUpdateDto;
import com.superkl.backend.info.WareHouseInfo;
import com.superkl.backend.service.WareHouseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse") // 纯RESTful风格("/api/warehouses")
@RequiredArgsConstructor
public class WareHouseController {
    private final WareHouseService wareHouseService;

    // 1.新增仓库
    // 纯RESTful风格
    // @PostMapping
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody WareHouseCreateDto wareHouseCreateDto) {
        wareHouseService.create(wareHouseCreateDto);
        return Result.successMessage("仓库新增成功");
    }

    // 2.更新仓库
    // 纯RESTful风格
    // @PutMapping("/{id}")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody WareHouseUpdateDto wareHouseUpdateDto) {
        wareHouseService.update(id,wareHouseUpdateDto);
        return Result.successMessage("仓库更新成功");
    }

    // 3.删除仓库
    // 纯RESTful风格
    // @DeleteMapping("/{id}")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wareHouseService.delete(id);
        return Result.successMessage("仓库删除成功");
    }

    // 4.查询单个仓库
    // 纯RESTful风格
    // @GetMapping("/{id}")
    @GetMapping("/search/{id}")
    public Result<WareHouseInfo> search(@PathVariable Long id) {
        return Result.success(wareHouseService.search(id));
    }

    // 5.查询整个仓库列表
    // 纯RESTful风格
    // @GetMapping
    @GetMapping("/search/list")
    public Result<List<WareHouseInfo>> searchList() {
        return Result.success(wareHouseService.searchList());
    }

    // 6.登录
    // 纯RESTful风格
    // @PostMapping
    @PostMapping("/login")
    public Result<WareHouseInfo> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
        return Result.success(wareHouseService.login(loginDto,response));
    }

    // 7.注销
    // 纯RESTful风格
    // @PostMapping("/logout")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        wareHouseService.logout(response);
        return Result.successMessage("仓库注销成功");
    }

}
