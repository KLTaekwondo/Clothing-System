package com.superkl.backend.repository.stock;

import com.superkl.backend.entity.stock.StockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Long> {
}
