package com.superkl.backend.entity.basic;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.enums.CheckEnum;
import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_warehouse")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WareHouse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wareHouseId;// 仓库ID

    @Column(nullable = false , unique = true)
    private String wareHouseCode;// 仓库编码

    @Column(nullable = false , unique = true)
    private String wareHouseName;// 仓库名称

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLE;// 仓库状态，默认启用

    @Column(nullable = false)
    private String wareHousePassword;// 仓库密码,用于登录前端的售货系统

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CheckEnum checkStatus = CheckEnum.NO_CHECK;// 仓库盘点状态，默认未在盘点


    // ===== 关联属性 =====
    // 关联管理员
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    // 辅助方法
    public boolean isEnabled() {
        return StatusEnum.ENABLE.equals(status);
    }
    public boolean isUnderCheck() {
        return CheckEnum.UNDER_CHECK.equals(checkStatus);
    }
}
