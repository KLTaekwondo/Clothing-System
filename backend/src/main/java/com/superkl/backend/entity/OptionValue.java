package com.superkl.backend.entity;

import com.superkl.backend.enums.OptionTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "t_product_option_value",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_option_type_option_value", columnNames = {"option_type", "option_value"})
                       }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OptionValue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionValueId;// 选项值ID

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OptionTypeEnum optionType;// 选项类型

    @Column(nullable = false)
    private String optionValue;// 选项值
}
