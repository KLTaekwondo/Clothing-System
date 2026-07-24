package com.superkl.backend.repository.product;

import com.superkl.backend.entity.product.OptionValue;
import com.superkl.backend.enums.OptionTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
    List<OptionValue> findByOptionType(OptionTypeEnum type);
}
