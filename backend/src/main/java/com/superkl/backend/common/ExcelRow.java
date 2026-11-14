package com.superkl.backend.common;

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
