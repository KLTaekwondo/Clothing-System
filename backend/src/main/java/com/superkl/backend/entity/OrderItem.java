package com.superkl.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "t_order_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;// 订单项ID

    // 弱关联属性
    @Column(nullable = false)
    private Long skuId;// 关联SKU

    @Column(nullable = false)
    private String productName;// 商品名称

    @Column(nullable = false)
    private BigDecimal unitPrice;// 商品单价

    @Column(nullable = false)
    private Integer stock;// 商品数量

    @Column(nullable = false)
    private BigDecimal discount;// 商品折扣

    @Column(nullable = false)
    private BigDecimal totalPrice;// 商品总价


    // 强关联属性
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 关联订单
}
