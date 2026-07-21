package com.superkl.backend.entity;

import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.ImportOrderEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_import_order")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ImportOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long importOrderId;// 进货订单Id

    @Column(nullable = false, unique = true)
    private String importOrderNo;// 进货订单编号;

    @Column(nullable = false)
    private String supplierName;// 快照供应商名字

    @Column(length = 100)
    private String remark;// 备注

    @Column(nullable = false)
    private BigDecimal totalAmount;// 总金额

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ImportOrderEnum status = ImportOrderEnum.DRAFT;// 进货订单状态

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DirectionEnum direction;// 进货订单方向

    // 关联属性
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;// 供应商(追溯供应商，强关联)

    @OneToMany(mappedBy = "importOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<ImportOrderItem> importOrderItems = new HashSet<>();// 进货订单项(追溯进货订单项，强关联)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ware_house_id")
    private WareHouse wareHouse;// 仓库(追溯仓库，强关联)

    // 辅助方法
    public boolean isDraft() {
        return ImportOrderEnum.DRAFT.equals(status);
    }

    public boolean isChecking() {
        return ImportOrderEnum.CHECKING.equals(status);
    }

    public boolean isApproved() {
        return ImportOrderEnum.APPROVED.equals(status);
    }

    public boolean isRejected() {
        return ImportOrderEnum.REJECTED.equals(status);
    }
}
