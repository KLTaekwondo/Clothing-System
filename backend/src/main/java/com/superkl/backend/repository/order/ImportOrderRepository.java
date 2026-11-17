package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.ImportOrder;
import com.superkl.backend.enums.AuditStatusEnum;
import com.superkl.backend.enums.DirectionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface ImportOrderRepository extends JpaRepository<ImportOrder, Long> {
    @Query("SELECT i FROM ImportOrder i " +
            "JOIN FETCH i.wareHouse " +
            "JOIN FETCH i.supplier " +
            "WHERE (:keyword IS NULL " +
            "OR i.importOrderNo LIKE :keyword " +
            "OR i.wareHouse.wareHouseName LIKE :keyword " +
            "OR i.supplier.supplierName LIKE :keyword) " +
            "AND (:status IS NULL OR i.status = :status) " +
            "ORDER BY i.createTime DESC")
    Page<ImportOrder> findPage(@Param("keyword") String keyword,
                               @Param("status") AuditStatusEnum status,
                               Pageable pageable);


    // 查询指定季节的进货金额
    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN i.direction = :dirIn THEN i.totalAmount " +
            "     WHEN i.direction = :dirOut THEN -i.totalAmount " +
            "ELSE 0 END), 0) " +
            "FROM ImportOrder i " +
            "WHERE i.status = :status " +
            "AND (:supplierId IS NULL OR i.supplier.supplierId = :supplierId) " +
            "AND i.createTime >= :startTime AND i.createTime < :endTime")
    BigDecimal sumAmount(@Param("dirIn") DirectionEnum dirIn,
                         @Param("dirOut") DirectionEnum dirOut,
                         @Param("status") AuditStatusEnum status,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime,
                         @Param("supplierId") Long supplierId);
}
