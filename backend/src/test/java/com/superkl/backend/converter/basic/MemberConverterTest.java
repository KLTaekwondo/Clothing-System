package com.superkl.backend.converter.basic;

import com.superkl.backend.dto.basic.MemberUpdateDto;
import com.superkl.backend.entity.basic.Member;
import com.superkl.backend.enums.MemberLevelEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MemberConverter.updateEntity 的等级门槛规则。
 *
 * 重点防回归：COMMON 和 CUSTOMER 的 levelPoints 是 null，
 * 之前直接拿来比大小会拆箱 NPE。
 */
class MemberConverterTest {

    @Test
    void updateEntity_whenCommon_shouldKeepAdminInput() {
        Member member = buildMember();

        MemberConverter.updateEntity(buildDto(MemberLevelEnum.COMMON, 3000L), member);

        assertEquals(MemberLevelEnum.COMMON, member.getMemberLevel());
        assertEquals(3000L, member.getMemberLevelPoints());
    }

    @Test
    void updateEntity_whenCustomer_shouldKeepAdminInput() {
        Member member = buildMember();

        MemberConverter.updateEntity(buildDto(MemberLevelEnum.CUSTOMER, 3000L), member);

        assertEquals(MemberLevelEnum.CUSTOMER, member.getMemberLevel());
        assertEquals(3000L, member.getMemberLevelPoints());
    }

    @Test
    void updateEntity_whenVipBelowThreshold_shouldRaiseToThreshold() {
        Member member = buildMember();

        MemberConverter.updateEntity(buildDto(MemberLevelEnum.VIP, 30000L), member);

        assertEquals(MemberLevelEnum.VIP, member.getMemberLevel());
        assertEquals(MemberLevelEnum.VIP.getLevelPoints(), member.getMemberLevelPoints());
    }

    @Test
    void updateEntity_whenVipAboveThreshold_shouldKeepAdminInput() {
        Member member = buildMember();

        MemberConverter.updateEntity(buildDto(MemberLevelEnum.VIP, 60000L), member);

        assertEquals(60000L, member.getMemberLevelPoints());
    }

    @Test
    void updateEntity_whenMvpBelowThreshold_shouldRaiseToThreshold() {
        Member member = buildMember();

        MemberConverter.updateEntity(buildDto(MemberLevelEnum.MVP, 100000L), member);

        assertEquals(MemberLevelEnum.MVP, member.getMemberLevel());
        assertEquals(MemberLevelEnum.MVP.getLevelPoints(), member.getMemberLevelPoints());
    }

    // ==================================================================

    private Member buildMember() {
        return Member.builder()
                .memberId(1L)
                .memberName("测试会员")
                .memberPhone("13800000000")
                .memberBirthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    private MemberUpdateDto buildDto(MemberLevelEnum level, Long levelPoints) {
        MemberUpdateDto dto = new MemberUpdateDto();
        dto.setName("测试会员");
        dto.setLevel(level);
        dto.setPoints(100L);
        dto.setLevelPoints(levelPoints);
        dto.setBirthday(LocalDate.of(2000, 1, 1));
        dto.setDiscount(new BigDecimal("0.85"));
        return dto;
    }
}
