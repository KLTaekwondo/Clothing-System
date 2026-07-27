package com.superkl.backend.repository.product;

import com.superkl.backend.entity.product.ProductSku;
import com.superkl.backend.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    @Query("SELECT p FROM ProductSku p " +
            "JOIN FETCH p.product " +
            "WHERE p.product.productId = :id")
    List<ProductSku> findByProductId(@Param("id") Long id);

    @Query("SELECT p FROM ProductSku p " +
            "JOIN FETCH p.product " +
            "WHERE p.skuCode = :skuCode")
    Optional<ProductSku> findBySkuCode(@Param("skuCode") String skuCode);

    @Query("SELECT p FROM ProductSku p " +
            "JOIN FETCH p.product " +
            "WHERE p.product.productCode = :productCode AND p.status = :status")
    List<ProductSku> findByProductCodeAndStatus(@Param("productCode") String productCode, @Param("status") StatusEnum status);
}
