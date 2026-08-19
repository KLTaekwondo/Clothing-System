package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.Order;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.employee " +
            "JOIN FETCH o.wareHouse " +
            "WHERE o.wareHouse.wareHouseId = :warehouseId AND o.status = :status")
    Page<Order> findStatusByWareHouseId(@Param("warehouseId") Long warehouseId ,
                                        @Param("status") OrderStatusEnum status ,
                                        Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.employee " +
            "JOIN FETCH o.wareHouse " +
            "ORDER BY o.createTime DESC")
    Page<Order> findPage(Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.employee " +
            "JOIN FETCH o.wareHouse " +
            "WHERE o.createTime >= :startTime " +
            "AND o.createTime < :endTime " +
            "AND o.status = :status " +
            "AND o.wareHouse.wareHouseId = :warehouseId ")
    Page<Order> findPageByTimeAndWId(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("status") OrderStatusEnum status,
                                    @Param("warehouseId") Long warehouseId,
                                    Pageable pageable);

    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN oi.direction = :dirIn THEN oi.actualPrice " +
            "     WHEN oi.direction = :dirOut THEN -oi.actualPrice " +
            "     ELSE 0 END" +
            "), 0) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status = :status " +
            "AND oi.createTime >= :start AND oi.createTime < :end")
    BigDecimal sumTotalAmountByTime(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("dirIn") DirectionEnum dirIn,
                                    @Param("dirOut") DirectionEnum dirOut,
                                    @Param("status") OrderStatusEnum status);

    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN oi.direction = :dirIn  THEN  oi.quantity * p.importPrice " +   // 售出：成本为正
            "     WHEN oi.direction = :dirOut THEN -oi.quantity * p.importPrice " +   // 退货：成本冲回
            "     ELSE 0 END" +
            "), 0) " +
            "FROM OrderItem oi, ProductSku sku, Product p " +
            "WHERE sku.skuId = oi.skuId " +                    // ① 明细 → SKU
            "AND sku.product.productId = p.productId " +       // ② SKU → 商品（拿进货价）
            "AND oi.order.status = :status " +
            "AND oi.createTime >= :start AND oi.createTime < :end")
    BigDecimal sumImportAmountByTime(@Param("start") LocalDateTime start,
                                     @Param("end")   LocalDateTime end,
                                     @Param("dirIn")  DirectionEnum dirIn,
                                     @Param("dirOut") DirectionEnum dirOut,
                                     @Param("status") OrderStatusEnum status);

}
