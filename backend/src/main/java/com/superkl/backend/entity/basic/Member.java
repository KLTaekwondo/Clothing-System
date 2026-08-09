package com.superkl.backend.entity.basic;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.enums.MemberLevelEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "t_member")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false , unique = true)
    private String memberPhone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberLevelEnum memberLevel = MemberLevelEnum.COMMON;// 默认普通

    @Column(nullable = false)
    @Builder.Default
    private Long memberPoints = 0L; // 会员积分点

    @Column(nullable = false)
    @Builder.Default
    private Long memberLevelPoints = 0L; // 会员等级点

    @Column(nullable = false)
    private LocalDate memberBirthday;// 会员生日

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal memberDiscount = BigDecimal.valueOf(0.85);// 会员折扣率默认0.85

    // 辅助方法
    public boolean isVIP(){
        return MemberLevelEnum.VIP.equals(memberLevel);
    }

    // 是否普通用户
    public boolean isCOMMON(){
        return MemberLevelEnum.COMMON.equals(memberLevel);
    }

    // 是否自定义会员
    public boolean isCUSTOMER(){
        return MemberLevelEnum.CUSTOMER.equals(memberLevel);
    }

    // 是否高级会员
    public boolean isMVP() {
        return MemberLevelEnum.MVP.equals(memberLevel);
    }
}
