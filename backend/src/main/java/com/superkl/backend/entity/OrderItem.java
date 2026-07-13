package com.superkl.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "t_order_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;// 订单项ID

    // 弱关联属性
    @Column(nullable = false)
    private Long skuId;// 关联SKU

    @Column(nullable = false)
    private String productName;// 商品名称快照

    @Column(nullable = false)
    private String skuName;// 商品名称快照

    @Column(nullable = false)
    private BigDecimal unitPrice;// 商品单价

    @Column(nullable = false)
    private Integer quantity;// 商品数量

    @Column(nullable = false)
    private BigDecimal discount;// 商品折扣

    @Column(nullable = false)
    private BigDecimal totalPrice;// 商品总价

    @Column(nullable = false)
    private BigDecimal actualPrice;// 商品实际总价格


    // 强关联属性
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 关联订单
}
