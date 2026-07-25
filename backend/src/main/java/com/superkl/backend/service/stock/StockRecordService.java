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
    public PageResult<StockRecordInfo> searchPage(Pageable pageable){
        Page<StockRecord> stockRecords = stockRecordRepository.findPage(pageable);
        return StockRecordConverter.toInfoPage(stockRecords);
    }
}
