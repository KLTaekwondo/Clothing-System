package com.superkl.backend.entity.basic;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_supplier")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Supplier extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;// 供应商Id

    @Column(nullable = false,unique = true)
    private String supplierCode;// 供应商编码

    @Column(nullable = false)
    private String supplierName;// 供应商名称

    private String contactPhone;// 联系电话（可选，建议最好填上）

    @Column(length = 100)
    private String remark; // 备注（可选）

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLE;// 状态（默认启用）

    public boolean isEnabled(){
        return StatusEnum.ENABLE.equals(status);
    }
}
