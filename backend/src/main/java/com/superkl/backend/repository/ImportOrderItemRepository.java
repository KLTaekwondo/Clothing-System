package com.superkl.backend.repository;

import com.superkl.backend.entity.ImportOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportOrderItemRepository extends JpaRepository<ImportOrderItem, Long> {
}
