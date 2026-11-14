package com.superkl.backend.common;

import lombok.*;

import java.util.List;

// 一个下拉框的配置：作用范围 + 候选值
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
    private List<String> list;

    // 快捷构造：默认从第 1 行到第 500 行，且只作用于单列
    public ExcelPullList(int column, List<String> list) {
        this.startRow = 1;
        this.endRow = 500;
        this.startColumn = column;
        this.endColumn = column;
        this.list = list;
    }
}
