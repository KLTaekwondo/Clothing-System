package com.superkl.backend.repository.stock;

import com.superkl.backend.entity.stock.StockRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Long> {
    @Query("select s from StockRecord s order by s.createTime desc")
    Page<StockRecord> findPage(Pageable pageable);
}
