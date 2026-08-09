package com.superkl.backend.converter.basic;

import com.superkl.backend.dto.basic.MemberCreateDto;
import com.superkl.backend.dto.basic.MemberMaxCreateDto;
import com.superkl.backend.dto.basic.MemberUpdateDto;
import com.superkl.backend.entity.basic.Member;
import com.superkl.backend.info.basic.MemberInfo;

import java.math.BigDecimal;
import java.util.List;

public class MemberConverter {
    private MemberConverter() {} // 私有构造函数，防止外部实例化

    // 实体转info
    public static MemberInfo toInfo(Member member) {
        return MemberInfo.builder()
                .memberName(member.getMemberName())
                .memberPhone(member.getMemberPhone())
                .memberLevel(member.getMemberLevel())
                .memberPoints(member.getMemberPoints())
                .memberLevelPoints(member.getMemberLevelPoints())
                .memberBirthday(member.getMemberBirthday())
                .memberDiscount(member.getMemberDiscount())
                .id(member.getMemberId())
                .createTime(member.getCreateTime())
                .updateTime(member.getUpdateTime())
                .build();
    }

    // 实体列表转info列表
    public static List<MemberInfo> toInfoList(List<Member> memberList) {
        return memberList.stream()
                .map(MemberConverter::toInfo)
                .toList();
    }

    // dto转实体
    public static Member toEntity(MemberCreateDto memberCreateDto) {
        return Member.builder()
                .memberName(memberCreateDto.getName())
                .memberPhone(memberCreateDto.getPhone())
                .memberBirthday(memberCreateDto.getBirthday())
                .build();
    }

    public static Member toMaxEntity(MemberMaxCreateDto memberMaxCreateDto) {
         Member member = Member.builder()
                .memberName(memberMaxCreateDto.getName())
                .memberPhone(memberMaxCreateDto.getPhone())
                .memberLevel(memberMaxCreateDto.getLevel())
                .memberPoints(memberMaxCreateDto.getPoints())
                .memberLevelPoints(memberMaxCreateDto.getLevelPoints())
                .memberBirthday(memberMaxCreateDto.getBirthday())
                .build();

        // 1.3 设置会员折扣率
        // 1.3.1 普通会员默认折扣率0.88
        if(member.isCOMMON()){
            member.setMemberDiscount(BigDecimal.valueOf(0.85));
        }
        // 1.3.2 VIP会员默认折扣率0.85
        if (member.isVIP()){
            member.setMemberDiscount(BigDecimal.valueOf(0.83));
        }

        // 1.3.3 MVP会员默认折扣率0.8
        if (member.isMVP()){
            member.setMemberDiscount(BigDecimal.valueOf(0.8));
        }

        // 1.3.4 自定义会员折扣率根据Dto中的折扣率
        if(member.isCUSTOMER()){
            member.setMemberDiscount(memberMaxCreateDto.getDiscount());
        }

        return member;
    }

    // 更新会员信息
    public static void updateEntity(MemberUpdateDto dto, Member member) {
        member.setMemberName(dto.getName());
        member.setMemberLevel(dto.getLevel());
        member.setMemberPoints(dto.getPoints());
        member.setMemberLevelPoints(dto.getLevelPoints());
        member.setMemberBirthday(dto.getBirthday());
        member.setMemberDiscount(dto.getDiscount());
    }
}
