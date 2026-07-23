package com.superkl.backend.repository;

import com.superkl.backend.entity.TransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {
}
