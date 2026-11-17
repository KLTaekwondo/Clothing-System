package com.superkl.backend.common.excel;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelRow {
    private int rowIndex;
    private String[] data;
}
