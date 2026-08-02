package com.superkl.backend.info.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StockCheckWithItemsInfo extends StockCheckInfo {
    private List<StockCheckItemInfo> stockCheckItems;
}
