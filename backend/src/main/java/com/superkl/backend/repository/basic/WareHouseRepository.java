package com.superkl.backend.repository.basic;

import com.superkl.backend.entity.basic.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, Long> {

    @Query("select w from WareHouse w where w.wareHouseCode = :account or w.wareHouseName = :account")
    Optional<WareHouse> findByAccount(String account);

    // 检查仓库编号是否已经存在
    @Query("SELECT COUNT(w) > 0 FROM WareHouse w WHERE w.wareHouseCode = :wareHouseCode")
    boolean existsByCode(@Param("wareHouseCode") String wareHouseCode);
}
