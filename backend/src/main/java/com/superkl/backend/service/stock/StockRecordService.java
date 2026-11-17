package com.superkl.backend.service.stock;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.converter.stock.StockRecordConverter;
import com.superkl.backend.entity.stock.StockRecord;
import com.superkl.backend.info.stock.StockRecordInfo;
import com.superkl.backend.repository.stock.StockRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockRecordService {
    private final StockRecordRepository stockRecordRepository;

    // 查询库存记录列表
    @Transactional(readOnly = true)
    public PageResult<StockRecordInfo> searchPage(String keyword, Pageable pageable){
        // 空关键词不参与过滤；否则会拼出 LIKE ''，一条也查不到
        String pattern = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";
        Page<StockRecord> stockRecords = stockRecordRepository.findPage(pattern, pageable);
        return StockRecordConverter.toInfoPage(stockRecords);
    }
}
