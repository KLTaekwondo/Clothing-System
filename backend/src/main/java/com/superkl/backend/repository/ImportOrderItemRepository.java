package com.superkl.backend.repository;

import com.superkl.backend.entity.ImportOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportOrderItemRepository extends JpaRepository<ImportOrderItem, Long> {
    @Query("SELECT i FROM ImportOrderItem i WHERE i.importOrder.importOrderId = :importOrderId")
    List<ImportOrderItem> findByImportOrderId(Long importOrderId);
}
