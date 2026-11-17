package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.StockCheck;
import com.superkl.backend.enums.AuditStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCheckRepository extends JpaRepository<StockCheck, Long> {
    @Query("SELECT s FROM StockCheck s " +
            "WHERE (:keyword IS NULL " +
            "OR s.stockCheckNo LIKE :keyword " +
            "OR s.wareHouseName LIKE :keyword) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "ORDER BY s.createTime DESC")
    Page<StockCheck> findPage(@Param("keyword") String keyword,
                              @Param("status") AuditStatusEnum status,
                              Pageable pageable);
}
