package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.TransferOrder;
import com.superkl.backend.enums.AuditStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {
    @Query("SELECT t FROM TransferOrder t " +
            "WHERE (:keyword IS NULL " +
            "OR t.transferOrderNo LIKE :keyword " +
            "OR t.sourceWareHouseName LIKE :keyword " +
            "OR t.targetWareHouseName LIKE :keyword) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "ORDER BY t.createTime DESC")
    Page<TransferOrder> findPage(@Param("keyword") String keyword,
                                 @Param("status") AuditStatusEnum status,
                                 Pageable pageable);
}
