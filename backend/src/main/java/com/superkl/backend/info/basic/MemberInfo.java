package com.superkl.backend.info.basic;

import com.superkl.backend.enums.MemberLevelEnum;
import com.superkl.backend.info.BaseInfo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MemberInfo extends BaseInfo {
    private String memberName;
    private String memberPhone;
    private MemberLevelEnum memberLevel;
    private Long memberPoints;
    private Long memberLevelPoints;
    private BigDecimal memberDiscount;
    private LocalDate memberBirthday;
}
