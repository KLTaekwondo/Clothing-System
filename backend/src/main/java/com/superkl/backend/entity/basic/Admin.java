package com.superkl.backend.entity.basic;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "t_admin")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Admin extends BaseEntity {
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

    // 辅助方法
    public boolean isEnabled() {
        return StatusEnum.ENABLE.equals(status);
    }
}
