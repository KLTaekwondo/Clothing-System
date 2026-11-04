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
import java.math.RoundingMode;
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

    @Transactional
    public void managePoints(String phoneNumber, BigDecimal actualPrice) {
        // 先查出会员，看是否存在
        Member member = memberRepository.findMemberByPhoneForUpdate(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "会员不存在"));

        // 检查金额是否存在，避免null调用
        if (actualPrice == null){
            throw new BusinessException(ErrorCodeEnum.RULE_ERROR, "订单金额不能为空");
        }

        Integer points = 0;

        // 情况一：订单金额大于0
        if(actualPrice.compareTo(BigDecimal.ZERO) > 0){
            // 增加会员积分点
            points = actualPrice.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.DOWN).intValueExact();
            member.setMemberPoints(member.getMemberPoints() + points);
            // 检查是否升级
            checkUpgrade(member, points);
        }

        // 情况二：订单金额小于等于0
        if(actualPrice.compareTo(BigDecimal.ZERO) < 0){
            // 减少会员积分点
            points = actualPrice.multiply(BigDecimal.valueOf(-100)).setScale(0, RoundingMode.DOWN).intValueExact();
            // 稽查积分是否足够
            // 不足够直接为0，不报错
            // 足够则正常减少
            if (member.getMemberPoints() < points) {
                member.setMemberPoints(0L);
            }else{
                member.setMemberPoints(member.getMemberPoints() - points);
            }
        }

        memberRepository.save(member);
        RequestUser.log();
        log.info("会员积分点管理成功，会员姓名：{}，会员手机号：{}，订单金额：{}，变化积分点：{}", member.getMemberName(), member.getMemberPhone(), actualPrice, points);
    }

    //升级逻辑：注，无事务，直接加入会员等级点更新事务
    public void checkUpgrade(Member member , Integer points) {
        // 判断,自定义直接返回
        if(member.isCUSTOMER()) return;

        // 先赋值，并准备变量包装
        long newLevelPoints = member.getMemberLevelPoints() + points;
        // 检查是否升级为普通会员
        if(newLevelPoints >= MemberLevelEnum.VIP.getLevelPoints() && newLevelPoints <= MemberLevelEnum.MVP.getLevelPoints()){
            member.setMemberLevel(MemberLevelEnum.VIP);
            member.setMemberDiscount(MemberLevelEnum.VIP.getDiscount());
        }

        if(newLevelPoints >= MemberLevelEnum.MVP.getLevelPoints()){
            member.setMemberLevel(MemberLevelEnum.MVP);
            member.setMemberDiscount(MemberLevelEnum.MVP.getDiscount());
        }

        member.setMemberLevelPoints(newLevelPoints);
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

}
