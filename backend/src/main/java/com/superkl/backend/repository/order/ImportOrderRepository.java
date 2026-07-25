package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.ImportOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOrderRepository extends JpaRepository<ImportOrder, Long> {
    @Query("SELECT i FROM ImportOrder i ORDER BY i.createTime DESC")
    Page<ImportOrder> findPage(Pageable pageable);
}
