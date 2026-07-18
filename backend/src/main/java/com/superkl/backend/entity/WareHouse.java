package com.superkl.backend.entity;

import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_warehouse")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WareHouse extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wareHouseId;// 仓库ID

    @Column(nullable = false , unique = true)
    private String wareHouseCode;// 仓库编码

    @Column(nullable = false)
    private String wareHouseName;// 仓库名称

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLE;// 仓库状态，默认启用

    @Column(nullable = false)
    private String wareHousePassword;// 仓库密码,用于登录前端的售货系统

    // ===== 关联属性 =====
    // 关联管理员
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @OneToMany(mappedBy = "wareHouse",fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Order> orders = new HashSet<>();// 订单列表

    @OneToMany(mappedBy = "wareHouse",fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ImportOrder> importOrders = new HashSet<>();// 进货订单列表

    // 关联员工
    @OneToMany(mappedBy = "wareHouse",fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();

    // 辅助方法
    public boolean isEnabled() {
        return StatusEnum.ENABLE.equals(status);
    }
}
