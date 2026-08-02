package com.superkl.backend.entity.order;

import com.superkl.backend.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_stock_check_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StockCheckItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockCheckItemId; // 盘点订单项

    @Column(nullable = false)
    private Long stockId;// 库存ID

    @Column(nullable = false)
    private Long skuId;// skuID

    @Column(nullable = false)
    private String skuName;// sku名称

    @Column(nullable = false)
    private String skuCode;// sku编码

    @Column(nullable = false)
    private String productName;// 商品名称

    @Column(nullable = false)
    private String productCode;// 商品编码

    @Column
    private Integer actualQuantity;// 实际数量

    @Column(nullable = false)
    private Integer systemQuantity;// 系统数量

    @Column
    private Integer diffQuantity;// 差异数量

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_check_id")
    private StockCheck stockCheck;// 盘点订单
}
