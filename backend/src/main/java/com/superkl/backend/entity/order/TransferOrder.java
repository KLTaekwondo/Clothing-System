package com.superkl.backend.entity.order;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.enums.AuditStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_transfer_order")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class TransferOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferOrderId; // 转移订单ID

    @Column(nullable = false,unique = true)
    private String transferOrderNo; // 转移订单编号

    @Column(nullable = false)
    private String sourceWareHouseName; // 源仓库名称(快照，用于记录当时的仓库名称)
    @Column(nullable = false)
    private String targetWareHouseName; // 目标仓库名称(快照，用于记录当时的仓库名称)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuditStatusEnum status = AuditStatusEnum.DRAFT; // 转移订单状态

    @Column(length = 100)
    private String remark;

    @Column(nullable = false)
    private BigDecimal totalPrice; // 商品总价

    // 关联属性
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_warehouse_id")
    private WareHouse sourceWareHouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_warehouse_id")
    private WareHouse targetWareHouse;

    @OneToMany(mappedBy = "transferOrder",fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    @Builder.Default
    private Set<TransferOrderItem> transferOrderItems = new HashSet<>();// 转移订单项列表

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
