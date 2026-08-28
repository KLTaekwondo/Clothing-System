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
            "ORDER BY i.createTime DESC")
    Page<ImportOrder> findPage(Pageable pageable);


    // 查询指定季节的进货金额
    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN i.direction = :dirIn THEN i.totalAmount " +
            "     WHEN i.direction = :dirOut THEN -i.totalAmount " +
            "ELSE 0 END), 0) " +
            "FROM ImportOrder i " +
            "WHERE i.status = :status " +
            "AND i.createTime >= :startTime AND i.createTime < :endTime")
    BigDecimal sumAmountByStatusAndTime(@Param("dirIn") DirectionEnum dirIn,
                                        @Param("dirOut") DirectionEnum dirOut,
                                        @Param("status") AuditStatusEnum status,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    // 查询指定供应商的进货金额
    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN i.direction = :dirIn THEN i.totalAmount " +
            "     WHEN i.direction = :dirOut THEN -i.totalAmount " +
            "ELSE 0 END), 0) " +
            "FROM ImportOrder i " +
            "WHERE i.status = :status " +
            "AND i.supplier.supplierId = :supplierId " +
            "AND i.createTime >= :startTime AND i.createTime < :endTime")
    BigDecimal sumAmountByStatusAndSupplierAndTime(@Param("dirIn") DirectionEnum dirIn,
                                                @Param("dirOut") DirectionEnum dirOut,
                                                @Param("status") AuditStatusEnum status,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("supplierId") Long supplierId);



}
