package com.superkl.backend.service.stock;

import com.superkl.backend.converter.stock.StockRecordConverter;
import com.superkl.backend.entity.stock.StockRecord;
import com.superkl.backend.info.stock.StockRecordInfo;
import com.superkl.backend.repository.stock.StockRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockRecordService {
    private final StockRecordRepository stockRecordRepository;

    // 查询库存记录列表
    @Transactional(readOnly = true)
    public List<StockRecordInfo> searchList(){
        List<StockRecord> stockRecords = stockRecordRepository.findAll();
        return StockRecordConverter.toInfoList(stockRecords);
    }
}
