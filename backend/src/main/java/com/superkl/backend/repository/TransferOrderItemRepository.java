package com.superkl.backend.repository;

import com.superkl.backend.entity.TransferOrderItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferOrderItemRepository extends CrudRepository<TransferOrderItem, Long> {
    @Query("SELECT t FROM TransferOrderItem t WHERE t.transferOrder.transferOrderId = :transferOrderId")
    List<TransferOrderItem> findByTransferOrderId(@Param("transferOrderId") Long transferOrderId);
}
