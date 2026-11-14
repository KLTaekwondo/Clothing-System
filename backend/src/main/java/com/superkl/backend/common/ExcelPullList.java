package com.superkl.backend.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExcelPullList {
    private int startRow;
    private int endRow;
    private int startColumn;
    private int endColumn;
    private String[] list;
}
