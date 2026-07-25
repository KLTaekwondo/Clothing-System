package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.TransferOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {
    @Query("SELECT t FROM TransferOrder t ORDER BY t.createTime DESC")
    Page<TransferOrder> findPage(Pageable pageable);
}
