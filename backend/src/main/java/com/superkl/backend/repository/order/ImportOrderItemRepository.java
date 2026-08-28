package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.ImportOrderItem;
import com.superkl.backend.enums.AuditStatusEnum;
import com.superkl.backend.enums.DirectionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImportOrderItemRepository extends JpaRepository<ImportOrderItem, Long> {
    @Query("SELECT i FROM ImportOrderItem i WHERE i.importOrder.importOrderId = :importOrderId")
    List<ImportOrderItem> findByImportOrderId(Long importOrderId);

    // 计算指定时间范围内指定状态的进货订单项数量总数
    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN oi.importOrder.direction = :dirIn THEN oi.quantity " +
            "     WHEN oi.importOrder.direction = :dirOut THEN -oi.quantity " +
            "ELSE 0 END), 0) " +
            "FROM ImportOrderItem oi " +
            "WHERE oi.importOrder.status = :status " +
            "AND oi.createTime >= :startTime " +
            "AND oi.createTime < :endTime")
    Long sumQuantityByStatusAndTime(@Param("dirIn") DirectionEnum dirIn,
                                        @Param("dirOut") DirectionEnum dirOut,
                                        @Param("status") AuditStatusEnum status,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN oi.importOrder.direction = :dirIn THEN oi.quantity " +
            "     WHEN oi.importOrder.direction = :dirOut THEN -oi.quantity " +
            "ELSE 0 END), 0) " +
            "FROM ImportOrderItem oi " +
            "WHERE oi.importOrder.status = :status " +
            "AND oi.importOrder.supplier.supplierId = :supplierId " +
            "AND oi.createTime >= :startTime " +
            "AND oi.createTime < :endTime")
    Long sumQuantityByStatusAndSupplierAndTime(@Param("dirIn") DirectionEnum dirIn,
                                                @Param("dirOut") DirectionEnum dirOut,
                                                @Param("status") AuditStatusEnum status,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("supplierId") Long supplierId);
}
