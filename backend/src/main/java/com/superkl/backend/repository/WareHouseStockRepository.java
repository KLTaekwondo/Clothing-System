package com.superkl.backend.repository;

import com.superkl.backend.entity.WareHouseStock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WareHouseStockRepository extends CrudRepository<WareHouseStock, Long> {
    // 根据仓库id和skuId查询库存记录
    @Query("SELECT ws FROM WareHouseStock ws " +
            "JOIN ws.productSku sku " +
            "WHERE sku.product.productId = :productId AND ws.wareHouse.wareHouseId = :warehouseId")
    List<WareHouseStock> findByProductIdAndWareHouseId(@Param("productId") Long productId,
                                                       @Param("warehouseId") Long warehouseId);

    // 根据skuId查询库存记录
    @Query("SELECT ws FROM WareHouseStock ws " +
            "JOIN ws.productSku sku " +
            "WHERE sku.skuId = :skuId AND ws.wareHouse.wareHouseId = :warehouseId")
    Optional<WareHouseStock> findBySkuIdAndWarehouseId(@Param("skuId") Long skuId,
                                                       @Param("warehouseId") Long warehouseId);
}
