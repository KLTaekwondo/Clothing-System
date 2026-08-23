package com.superkl.backend.repository.product;

import com.superkl.backend.entity.product.OptionValue;
import com.superkl.backend.enums.OptionTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionValueRepository extends JpaRepository<OptionValue, Long> {
    List<OptionValue> findByOptionType(OptionTypeEnum type);

    @Query("SELECT Count(o) > 0 FROM OptionValue o " +
            "WHERE o.optionType = :type " +
            "AND o.optionValue = :value")
    boolean existsByTypeAndValue(@Param("type") OptionTypeEnum type,
                                 @Param("value") String value);

    @Query("SELECT Count(o) > 0 FROM OptionValue o " +
            "WHERE o.optionType = :type " +
            "AND o.optionValue = :value " +
            "AND o.optionValueId <> :id")
    boolean existsByTypeAndValueNotId(@Param("id") Long id,
                                        @Param("type") OptionTypeEnum type,
                                        @Param("value") String value);

    @Query(value = "SELECT COUNT(*) > 0 FROM t_product_sku " +
            "WHERE JSON_VALID(spec_attributes) " +
            "AND JSON_SEARCH(spec_attributes, 'one', :value) IS NOT NULL",
            nativeQuery = true)
    boolean existsBySpecExactValue(@Param("value") String value);
}
