package com.superkl.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "t_import_order_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ImportOrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long importItemId;// 进货订单项Id

    @Column(nullable = false)
    private Long skuId;// 商品SKU Id(追溯商品SKU，弱关联)

    @Column(nullable = false)
    private String skuName;// 商品SKU 名称(弱关联快照，用于记录当时进货时的商品SKU名称)

    @Column(nullable = false)
    private String productName;// 商品名称(弱关联快照，用于记录当时进货时的商品名称)

    @Column(nullable = false)
    private BigDecimal importPrice;// 商品单价(弱关联快照，用于记录当时进货价)

    @Column(nullable = false)
    private Integer quantity;// 商品数量

    @Column(nullable = false)
    private BigDecimal totalPrice;// 商品总价

    // 关联属性
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_order_id")
    private ImportOrder importOrder;// 进货订单(追溯进货订单，强关联)
}
