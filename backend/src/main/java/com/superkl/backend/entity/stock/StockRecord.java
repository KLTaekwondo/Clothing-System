package com.superkl.backend.entity.stock;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_stock_record")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StockRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;// 记录id

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StockSourceTypeEnum sourceType;// 来源类型
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StockChangeTypeEnum changeType;// 变动类型


    @Column(nullable = false)
    private String productCode;// 商品编码
    @Column(nullable = false)
    private String productName;// 商品名称
    @Column(nullable = false)
    private String skuCode;// sku编码
    @Column(nullable = false)
    private String skuName;// sku名称

    @Column(nullable = false)
    private String wareHouseCode;// 仓库编码
    @Column(nullable = false)
    private String wareHouseName;// 仓库名称

    @Column(nullable = false)
    private String sourceNo;// 来源编号
    @Column(nullable = false)
    private Integer beforeQuantity;// 变动前库存
    @Column(nullable = false)
    private Integer changeQuantity;// 变动数量
    @Column(nullable = false)
    private Integer afterQuantity;// 变动后库存

    @Column(nullable = false)
    private String operationCode;// 操作人编码
    @Column(nullable = false)
    private String operationName;// 操作人名称
    @Column(nullable = false)
    private String operationRole;// 操作人角色

}
