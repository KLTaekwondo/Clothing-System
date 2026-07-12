package com.superkl.backend.entity;

import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_admin")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Admin extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;// 管理员ID

    @Column(unique = true, nullable = false)
    private String adminCode;// 管理员账号

    @Column(unique = true, nullable = false)
    private String username;// 管理员用户名

    @Column(nullable = false)
    private String password;// 管理员密码

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEnum status = StatusEnum.ENABLE;// 账号状态，默认启用


    @OneToMany(mappedBy = "admin" , fetch = FetchType.LAZY)// 懒加载，避免查询所有仓库，同时防止嵌套查询
    @Builder.Default
    private Set<WareHouse> warehouses = new HashSet<>();
}
