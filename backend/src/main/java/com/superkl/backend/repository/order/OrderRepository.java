package com.superkl.backend.repository.order;

import com.superkl.backend.common.extralreturn.OrderCounts;
import com.superkl.backend.entity.order.Order;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    // 按订单状态统计数量（全量，不是当前页）
    @Query(value = "SELECT status AS orderStatus, COUNT(*) AS mount FROM t_order GROUP BY status", nativeQuery = true)
    List<OrderCounts> countByStatus();

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.employee " +
            "JOIN FETCH o.wareHouse " +
            "WHERE o.wareHouse.wareHouseId = :warehouseId " +
            "AND o.status = :status " +
            "AND (:keyword IS NULL " +
            "OR o.orderNo LIKE :keyword " +
            "OR o.employee.employeeName LIKE :keyword) " +
            "ORDER BY o.createTime DESC")
    Page<Order> pageByWareHouseAndStatus(@Param("warehouseId") Long warehouseId ,
                                         @Param("status") OrderStatusEnum status ,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    // 后端总返回订单分页
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.employee " +
            "JOIN FETCH o.wareHouse " +
            "WHERE (:keyword IS NULL " +
            "OR o.orderNo LIKE :keyword " +
            "OR o.employee.employeeName LIKE :keyword " +
            "OR o.wareHouse.wareHouseName LIKE :keyword) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "ORDER BY o.createTime DESC")
    Page<Order> findPage(@Param("keyword") String keyword,
                         @Param("status") OrderStatusEnum status,
                         Pageable pageable);

    // 收银台总返回订单分页
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.employee " +
            "JOIN FETCH o.wareHouse " +
            "WHERE o.createTime >= :startTime " +
            "AND o.createTime < :endTime " +
            "AND o.status = :status " +
            "AND (:employeeId IS NULL OR o.employee.employeeId = :employeeId) " +
            "AND o.wareHouse.wareHouseId = :warehouseId " +
            "AND (:keyword IS NULL " +
            "OR o.orderNo LIKE :keyword " +
            "OR o.employee.employeeName LIKE :keyword) " +
            "ORDER BY o.createTime DESC")
    Page<Order> findPageCheckOut(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("status") OrderStatusEnum status,
                                 @Param("employeeId") Long employeeId,
                                 @Param("warehouseId") Long warehouseId,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    // 后端查看总订单销售额汇总
    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN oi.direction = :dirIn THEN oi.actualPrice " +
            "     WHEN oi.direction = :dirOut THEN -oi.actualPrice " +
            "     ELSE 0 END" +
            "), 0) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status = :status " +
            "AND (:payMethodEnum IS NULL OR oi.order.payMethod = :payMethodEnum) " +
            "AND (:employeeId IS NULL OR oi.order.employee.employeeId = :employeeId) " +
            "AND (:wareHouseId IS NULL OR oi.order.wareHouse.wareHouseId = :wareHouseId) " +
            "AND oi.createTime >= :start AND oi.createTime < :end")
    BigDecimal sumTotalAmount(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("dirIn") DirectionEnum dirIn,
                              @Param("dirOut") DirectionEnum dirOut,
                              @Param("status") OrderStatusEnum status,
                              @Param("payMethodEnum") PayMethodEnum payMethodEnum,
                              @Param("wareHouseId") Long wareHouseId,
                              @Param("employeeId") Long employeeId);



    // 后端查看总订单进货汇总
    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN oi.direction = :dirIn  THEN  oi.quantity * p.importPrice " +   // 售出：成本为正
            "     WHEN oi.direction = :dirOut THEN -oi.quantity * p.importPrice " +   // 退货：成本冲回
            "     ELSE 0 END" +
            "), 0) " +
            "FROM OrderItem oi, ProductSku sku, Product p " +
            "WHERE sku.skuId = oi.skuId " +                    // ① 明细 → SKU
            "AND sku.product.productId = p.productId " +       // ② SKU → 商品（拿进货价）
            "AND oi.order.status = :status " +
            "AND (:employeeId IS NULL OR oi.order.employee.employeeId = :employeeId) " +
            "AND (:wareHouseId IS NULL OR oi.order.wareHouse.wareHouseId = :wareHouseId) " +
            "AND (:payMethodEnum IS NULL OR oi.order.payMethod = :payMethodEnum) " +
            "AND oi.createTime >= :start AND oi.createTime < :end")
    BigDecimal sumImportAmount(@Param("start") LocalDateTime start,
                               @Param("end")   LocalDateTime end,
                               @Param("dirIn")  DirectionEnum dirIn,
                               @Param("dirOut") DirectionEnum dirOut,
                               @Param("status") OrderStatusEnum status,
                               @Param("payMethodEnum") PayMethodEnum payMethodEnum,
                               @Param("wareHouseId") Long wareHouseId,
                               @Param("employeeId") Long employeeId);

    // 收银端直接返回汇总
    @Query("SELECT COALESCE(SUM(" +
            "CASE WHEN oi.direction = :dirIn THEN oi.actualPrice " +
            "     WHEN oi.direction = :dirOut THEN -oi.actualPrice " +
            "     ELSE 0 END" +
            "), 0) " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status = :status " +
            "AND oi.order.wareHouse.wareHouseId = :wareHouseId " +
            "AND (:payMethod IS NULL OR oi.order.payMethod = :payMethod) " +
            "AND oi.createTime >= :start AND oi.createTime < :end")
    BigDecimal sumAmountCheckOut(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("dirIn") DirectionEnum dirIn,
                                 @Param("dirOut") DirectionEnum dirOut,
                                 @Param("status") OrderStatusEnum status,
                                 @Param("wareHouseId") Long wareHouseId,
                                 @Param("payMethod") PayMethodEnum payMethod);
}
