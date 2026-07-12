package com.superkl.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_ware_house_stock")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WareHouseStock extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;// 库存id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id",nullable = false)
    private ProductSku productSku;// 关联的sku

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ware_house_id",nullable = false)
    private WareHouse wareHouse;// 关联的仓库

    @Column(nullable = false)
    private Integer stock;// 库存数量
}
