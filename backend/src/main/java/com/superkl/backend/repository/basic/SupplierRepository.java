package com.superkl.backend.repository.basic;

import com.superkl.backend.entity.basic.Supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);

    // 检查供应商编号是否已经存在
    @Query("SELECT COUNT(s) > 0 FROM Supplier s WHERE s.supplierCode = :supplierCode")
    boolean existsByCode(@Param("supplierCode") String supplierCode);
}
