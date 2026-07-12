package com.superkl.backend.entity;

import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_employee")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Employee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;// 员工ID

    @Column(nullable = false , unique = true)
    private String employeeCode;// 员工编码

    @Column(nullable = false)
    private String employeeName;// 员工姓名

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLE;// 员工状态，默认启用

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ware_house_id")
    private WareHouse wareHouse; // 关联仓库
}
