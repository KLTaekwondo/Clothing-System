package com.superkl.backend.dto.stock;

import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StockContext {
    private String sourceNo;// 来源编号
    private StockChangeTypeEnum changeType;// 变动类型
    private StockSourceTypeEnum sourceType;// 来源类型
}
