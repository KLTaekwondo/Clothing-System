package com.superkl.backend.repository.product;

import com.superkl.backend.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // 根据产品编码查询商品
    Optional<Product> findByProductCode(String productCode);
}
