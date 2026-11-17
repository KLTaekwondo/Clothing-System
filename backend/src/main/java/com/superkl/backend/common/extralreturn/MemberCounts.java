package com.superkl.backend.common.extralreturn;

import com.superkl.backend.enums.MemberLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberCounts extends Counts {
    private MemberLevelEnum memberLevel;

    public MemberCounts(String memberLevel, Long mount) {
        super(mount);
        this.memberLevel = MemberLevelEnum.valueOf(memberLevel);
    }
}
