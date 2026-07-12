package com.superkl.backend.repository;

import com.superkl.backend.entity.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, Long> {

    @Query("select w from WareHouse w where w.wareHouseCode = :account or w.wareHouseName = :account")
    Optional<WareHouse> findByAccount(String account);
}
