package com.superkl.backend.service.basic;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.basic.MemberConverter;
import com.superkl.backend.dto.basic.MemberCreateDto;
import com.superkl.backend.dto.basic.MemberMaxCreateDto;
import com.superkl.backend.dto.basic.MemberUpdateDto;
import com.superkl.backend.entity.basic.Member;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.MemberLevelEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.basic.MemberInfo;
import com.superkl.backend.repository.basic.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 1.checkout 入口创建会员
    @Transactional
    public void create_checkout(MemberCreateDto memberCreateDto) {
        // 1.1 检查会员是否存在
        if (memberRepository.existsMemberByPhone(memberCreateDto.getPhone())) {
            throw new BusinessException(ErrorCodeEnum.RULE_CONFLICT, "会员已存在");
        }

        // 1.2 创建会员
        Member member = MemberConverter.toEntity(memberCreateDto);
        memberRepository.save(member);

        RequestUser.log();
        log.info("收银台创建会员成功，会员姓名：{}，会员手机号：{}", member.getMemberName(), member.getMemberPhone());
    }

    // 2.管理员创建会员
    @Transactional
    public void create_manage(MemberMaxCreateDto dto) {
        // 1.1 检查会员是否存在
        if (memberRepository.existsMemberByPhone(dto.getPhone())) {
            throw new BusinessException(ErrorCodeEnum.RULE_CONFLICT, "会员已存在");
        }

        // 1.2 创建会员
        Member member = MemberConverter.toMaxEntity(dto);
        // 最后保存
        memberRepository.save(member);

        RequestUser.log();
        log.info("管理员创建会员成功，会员姓名：{}，会员手机号：{}", member.getMemberName(), member.getMemberPhone());
    }

    // 3.管理员更新会员信息
    @Transactional
    public void update(Long id ,MemberUpdateDto dto) {
        // 1.1 检查会员是否存在
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "会员不存在"));

        // 1.2 更新会员信息
        MemberConverter.updateEntity(dto, member);
        memberRepository.save(member);

        RequestUser.log();
        log.info("管理员更新会员成功，会员姓名：{}，会员手机号：{}", member.getMemberName(), member.getMemberPhone());
    }

    // 4.管理员增加会员积分点
    @Transactional
    public void increasePoints(Long id, Integer points) {
        // 1.1 检查会员是否存在
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "会员不存在"));

        if(points == null || points < 0){
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "增加积分点不能小于0");
        }

        // 1.2 增加会员积分点
        member.setMemberPoints(member.getMemberPoints() + points);
        // 1.3 更新会员等级点，检查是否达到升级标准
        Long newLevelPoints = member.getMemberLevelPoints() + points;
        if(member.isCOMMON() && newLevelPoints >= 50000) {
            member.setMemberLevel(MemberLevelEnum.VIP);
            member.setMemberDiscount(BigDecimal.valueOf(0.83));
            log.info("会员升级为普通会员，会员姓名：{}，会员手机号：{}", member.getMemberName(), member.getMemberPhone());
        }

        if(member.isVIP() && newLevelPoints >= 500000) {
            member.setMemberLevel(MemberLevelEnum.MVP);
            member.setMemberDiscount(BigDecimal.valueOf(0.8));
            log.info("会员升级为高级会员，会员姓名：{}，会员手机号：{}", member.getMemberName(), member.getMemberPhone());
        }

        member.setMemberLevelPoints(newLevelPoints);
        memberRepository.save(member);

        RequestUser.log();
        log.info("管理员增加会员积分点成功，会员姓名：{}，会员手机号：{}，增加积分点：{}", member.getMemberName(), member.getMemberPhone(), points);
    }

    // 5.管理员减少会员积分点
    @Transactional
    public void decreasePoints(Long id, Integer points) {
        // 1.1 检查会员是否存在
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "会员不存在"));

        if(points == null || points < 0){
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "减少积分点不能小于0");
        }

        // 1.2 减少会员积分点
        if (member.getMemberPoints() < points) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "会员积分不足");
        }
        member.setMemberPoints(member.getMemberPoints() - points);
        memberRepository.save(member);

        RequestUser.log();
        log.info("管理员减少会员积分点成功，会员姓名：{}，会员手机号：{}，减少积分点：{}", member.getMemberName(), member.getMemberPhone(), points);
    }

    // 查询单个会员
    public MemberInfo search(String phone) {
        Member member = memberRepository.findMemberByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "会员不存在"));
        return MemberConverter.toInfo(member);
    }

    // 查询所有会员
    public List<MemberInfo> searchList() {
        List<Member> members = memberRepository.findAll();
        return MemberConverter.toInfoList(members);
    }

    // 不外放，只用于内部调用的查询方法
    public Member getFromPhone(String phone) {
        return memberRepository.findMemberByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "会员不存在"));
    }
}
