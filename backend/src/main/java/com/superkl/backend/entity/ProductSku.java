package com.superkl.backend.entity;

import com.superkl.backend.enums.SeasonEnum;
import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_product_sku")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProductSku extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skuId;// 商品种类id

    @Column(nullable = false)
    private String skuName;// 商品种类名称

    @Column(nullable = false, unique = true)
    private String skuCode;// 商品条形码

    // 商品规格属性{"color" : "红色", "size" : "小号", "material" : "中号", "material" : "棉质"}
    @Column(columnDefinition = "TEXT")
    private String specAttributes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLE;// 状态

    // 关联属性
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;// 商品


    // 辅助方法
    public boolean isEnabled() {
        return StatusEnum.ENABLE.equals(status);
    }
}
