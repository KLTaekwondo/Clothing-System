package com.superkl.backend.controller.basic;

import com.superkl.backend.annotation.OpLogs;
import com.superkl.backend.common.extralparams.PageParam;
import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.Result;
import com.superkl.backend.common.extralreturn.MemberCounts;
import com.superkl.backend.dto.basic.MemberCreateDto;
import com.superkl.backend.dto.basic.MemberMaxCreateDto;
import com.superkl.backend.dto.basic.MemberUpdateDto;
import com.superkl.backend.enums.MemberLevelEnum;
import com.superkl.backend.info.basic.MemberInfo;
import com.superkl.backend.service.basic.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 1.收银界面注册会员
    @OpLogs(module = "会员", description = "收银界面注册会员")
    @PostMapping("/register/checkout")
    public Result<Void> registerCheckout(@Valid @RequestBody MemberCreateDto dto) {
        memberService.create_checkout(dto);
        return Result.successMessage("会员注册成功！");
    }

    // 2.后台注册会员
    @OpLogs(module = "会员", description = "后台注册会员")
    @PostMapping("/register/manage")
    public Result<Void> registerManage(@Valid @RequestBody MemberMaxCreateDto dto) {
        memberService.create_manage(dto);
        return Result.successMessage("会员注册成功！");
    }

    // 3.更新会员信息
    @OpLogs(module = "会员", description = "更新会员信息")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MemberUpdateDto dto) {
        memberService.update(id, dto);
        return Result.successMessage("会员更新成功！");
    }

    // 4.查询所有会员
    @GetMapping("/search/page")
    public Result<PageResult<MemberInfo>> searchPage(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) MemberLevelEnum memberLevel,
                                                     @Valid PageParam pageParam) {
        Pageable pageable = pageParam.toPageable();
        return Result.success(memberService.searchPage(keyword, memberLevel, pageable));
    }

    // 5.查询单个会员
    @GetMapping("/search/{phone}")
    public Result<MemberInfo> search(@PathVariable String phone) {
        return Result.success(memberService.search(phone));
    }

    // 6.一次性统计
    @GetMapping("/count")
    public Result<List<MemberCounts>> countByMemberLevel() {
        return Result.success(memberService.countByMemberLevel());
    }
}