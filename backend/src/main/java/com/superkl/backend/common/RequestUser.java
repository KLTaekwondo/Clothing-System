package com.superkl.backend.common;

import com.superkl.backend.exception.BusinessException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Getter
@Setter
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestUser {
    private Long requestId;
    private String requestRole;
    private String requestName;
    private String requestCode;

    private static RequestUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof RequestUser) {
            return (RequestUser) principal;
        }
        return null;
    }

    /**
     * 返回格式化操作人信息，直接用在日志里
     * 例如：操作人：张三(ID=2)
     */
    public static void log() {
        RequestUser user = current();
        if (user == null) {
            log.info("操作人：未知");
            return;
        }
        log.info("操作人：{}(ID={}) 角色：{} 编码：{}", user.getRequestName(), user.getRequestId(), user.getRequestRole(), user.getRequestCode());
    }

    // 检查是否是管理员
    public static boolean isAdmin() {
        RequestUser user = current();
        if (user == null) {
            return false;
        }
        return user.getRequestRole().equals("ROLE_ADMIN");
    }

    public static RequestUser notNull() {
        RequestUser user = current();
        if (user == null) {
            throw new BusinessException("未获取到当前用户信息！");
        }
        return user;
    }

    // 检查是否是当前仓库
    public static boolean isCurrentWareHouse(Long wareHouseId) {
        RequestUser user = current();
        if (user == null) {
            return false;
        }
        return user.getRequestRole().equals("ROLE_WAREHOUSE") && user.getRequestId().equals(wareHouseId);
    }
}
