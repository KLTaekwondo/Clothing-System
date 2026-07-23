package com.superkl.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "t_transfer_order_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TransferOrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferItemId; // 转移订单项ID

    @Column(nullable = false)
    private Long skuId; // 商品SKU ID

    @Column(nullable = false)
    private String productName; // 商品名称

    @Column(nullable = false)
    private String skuName; // 商品SKU名称

    @Column(nullable = false)
    private BigDecimal price; // 商品单价

    @Column(nullable = false)
    private Integer quantity; // 商品数量

    @Column(nullable = false)
    private BigDecimal itemTotalPrice; // 商品总价

    // 关联属性
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "transfer_order_id")
    private TransferOrder transferOrder;

}
