package com.superkl.backend.repository.stock;

import com.superkl.backend.entity.stock.StockRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Long> {
    @Query("select s from StockRecord s " +
            "where (:keyword is null " +
            "or s.productCode like :keyword " +
            "or s.productName like :keyword " +
            "or s.skuCode like :keyword " +
            "or s.skuName like :keyword " +
            "or s.sourceNo like :keyword) " +
            "order by s.createTime desc")
    Page<StockRecord> findPage(@Param("keyword") String keyword, Pageable pageable);
}
