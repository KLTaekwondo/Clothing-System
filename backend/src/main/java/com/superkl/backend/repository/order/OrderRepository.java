package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.Order;
import com.superkl.backend.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("SELECT o FROM Order o WHERE o.wareHouse.wareHouseId = :warehouseId")
    List<Order> findByWareHouseId(@Param("warehouseId") Long warehouseId);

    List<Order> findByStatus(OrderStatusEnum status);

    Optional<Order> findByOrderNo(String orderNo);

    @Query("SELECT o FROM Order o ORDER BY o.createTime DESC")
    Page<Order> findPage(Pageable pageable);
}
