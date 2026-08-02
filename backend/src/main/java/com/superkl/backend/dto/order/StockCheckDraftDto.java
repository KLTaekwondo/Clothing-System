package com.superkl.backend.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class StockCheckDraftDto {
    @NotNull(message = "仓库ID不能为空")
    private Long wareHouseId;

    @NotEmpty(message = "盘点项列表不能为空")
    private List<@Valid StockCheckItemCreateDto> stockCheckItems;

    @Size(max = 100, message = "备注长度不能超过100个字符")
    private String remark;
}
