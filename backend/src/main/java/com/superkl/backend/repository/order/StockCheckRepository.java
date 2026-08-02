package com.superkl.backend.repository.order;

import com.superkl.backend.entity.order.StockCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCheckRepository extends JpaRepository<StockCheck, Long> {
    @Query("SELECT s FROM StockCheck s order by s.createTime desc")
    Page<StockCheck> findPage(Pageable pageable);
}
