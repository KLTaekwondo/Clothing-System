package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.StockCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockCheckItemRepository extends JpaRepository<StockCheckItem, Long> {
    @Query("SELECT s FROM StockCheckItem s WHERE s.stockCheck.stockCheckId = :stockCheckId")
    List<StockCheckItem> findByStockCheckId(@Param("stockCheckId") Long stockCheckId);
}
