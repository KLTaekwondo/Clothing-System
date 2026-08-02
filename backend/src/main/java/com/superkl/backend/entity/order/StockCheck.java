package com.superkl.backend.entity.order;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.enums.AuditStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_stock_check")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StockCheck extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockCheckId;// 库存盘点订单ID

    @Column(nullable = false , unique = true)
    private String stockCheckNo;// 库存盘点订单编号

    @Column(nullable = false)
    private String wareHouseName;// 仓库快照名称

    @Column(nullable = false)
    private String wareHouseCode;// 仓库快照编码

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuditStatusEnum status = AuditStatusEnum.DRAFT;// 审核状态

    @Column(length = 100)
    private String remark;// 备注

    // 关联关系
    @OneToMany(mappedBy = "stockCheck", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<StockCheckItem> stockCheckItems = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private WareHouse targetWarehouse;// 目标仓库


    // 辅助方法
    public boolean isDraft() {
        return AuditStatusEnum.DRAFT.equals(status);
    }

    public boolean isChecking() {
        return AuditStatusEnum.CHECKING.equals(status);
    }

    public boolean isApproved() {
        return AuditStatusEnum.APPROVED.equals(status);
    }

    public boolean isRejected() {
        return AuditStatusEnum.REJECTED.equals(status);
    }
}
