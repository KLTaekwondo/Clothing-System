package com.superkl.backend.entity.product;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_product")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;// 产品ID

    @Column(unique = true, nullable = false)
    private String productCode;// 产品编码

    @Column(nullable = false)
    private String productName;// 产品名称

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SeasonEnum season;// 季节

    @Column(nullable = false ,length = 20)
    private String type;// 产品类型：男大童/女大童/男/女

    @Column(nullable = false ,length = 20)
    private String category;// 产品种类：裤子，衣服，T恤

    @Column(nullable = false ,length = 20)
    private String unit;// 衣服计数单位

    @Column(nullable = false ,length = 50)
    private String composition;// 面料组合

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLE;

    @Column(nullable = false)
    private BigDecimal importPrice;// 进货价格

    @Column(nullable = false)
    private BigDecimal salePrice;// 销售价格

    @Column(nullable = false)
    @Builder.Default
    private boolean special = false;// 是否特价

    // 关联字段
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ProductSku> productSkuList = new HashSet<>();

    // 辅助方法
    public boolean isEnabled() {
        return StatusEnum.ENABLE.equals(status);
    }
}
