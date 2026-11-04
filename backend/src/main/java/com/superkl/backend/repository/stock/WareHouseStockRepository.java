package com.superkl.backend.repository.stock;

import com.superkl.backend.entity.stock.WareHouseStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            "JOIN fetch ws.productSku sku " +
            "WHERE sku.product.productId = :productId AND ws.wareHouse.wareHouseId = :warehouseId")
    List<WareHouseStock> findByProductIdAndWareHouseId(@Param("productId") Long productId,
                                                       @Param("warehouseId") Long warehouseId);

    // 根据skuId查询库存记录
    @Query("SELECT ws FROM WareHouseStock ws " +
            "JOIN fetch ws.productSku sku " +
            "JOIN fetch sku.product p " +
            "JOIN fetch ws.wareHouse wh " +
            "WHERE sku.skuId = :skuId AND wh.wareHouseId = :warehouseId")
    Optional<WareHouseStock> findBySkuIdAndWarehouseId(@Param("skuId") Long skuId,
                                                       @Param("warehouseId") Long warehouseId);

    // 上锁：通过skuId和wareHouseId锁定库存记录
    @Query(value = "SELECT stock FROM t_ware_house_stock " +
            "WHERE sku_id = :skuId AND ware_house_id = :warehouseId FOR UPDATE",
            nativeQuery = true)
    Optional<Integer> lockStockBySkuIdAndWarehouseId(@Param("skuId") Long skuId,
                             @Param("warehouseId") Long warehouseId);

    // 上锁：通过stockId锁定库存记录
    @Query(value = "SELECT stock FROM t_ware_house_stock " +
            "WHERE stock_id = :stockId FOR UPDATE",
            nativeQuery = true)
    Optional<Integer> lockStockById(@Param("stockId") Long stockId);

    @Query("SELECT ws FROM WareHouseStock ws " +
            "JOIN FETCH ws.productSku sku " +
            "JOIN FETCH sku.product p " +
            "JOIN FETCH ws.wareHouse wh " +
            "WHERE ws.stockId = :stockId")
    Optional<WareHouseStock> findByStockId(@Param("stockId") Long stockId);

    // 分页查询某一个仓库的商品库存记录
    @Query("SELECT ws FROM WareHouseStock ws " +
            "JOIN fetch ws.productSku sku " +
            "JOIN fetch sku.product p " +
            "JOIN fetch ws.wareHouse wh " +
            "WHERE wh.wareHouseId = :warehouseId")
    Page<WareHouseStock> findPageByWareHouseId(@Param("warehouseId") Long warehouseId,
                                           Pageable pageable);
}
