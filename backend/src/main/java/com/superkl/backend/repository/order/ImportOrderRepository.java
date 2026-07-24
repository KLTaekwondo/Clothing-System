package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.ImportOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOrderRepository extends JpaRepository<ImportOrder, Long> {
}
