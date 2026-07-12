package com.superkl.backend.service;

import com.superkl.backend.converter.WareHouseConverter;
import com.superkl.backend.dto.LoginDto;
import com.superkl.backend.dto.WareHouseCreateDto;
import com.superkl.backend.dto.WareHouseUpdateDto;
import com.superkl.backend.entity.Admin;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.WareHouseInfo;
import com.superkl.backend.repository.AdminRepository;
import com.superkl.backend.repository.WareHouseRepository;
import com.superkl.backend.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WareHouseService {
    private final WareHouseRepository wareHouseRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 新增仓库
    @Transactional
    public void create(WareHouseCreateDto wareHouseCreateDto) {
        // 先查找管理员是否存在
        Admin admin = adminRepository.findById(wareHouseCreateDto.getAdminId())
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        // 转换为仓库实体
        WareHouse wareHouse = WareHouseConverter.toEntity(wareHouseCreateDto, admin);
        wareHouseRepository.save(wareHouse);
    }

    // 更新仓库
    @Transactional
    public void update(Long wareHouseId, WareHouseUpdateDto wareHouseUpdateDto) {
        // 先查找仓库是否存在
        WareHouse wareHouse = wareHouseRepository.findById(wareHouseId)
                .orElseThrow(() -> new BusinessException("仓库不存在"));

        // 更新仓库实体
        WareHouseConverter.updateEntity(wareHouse, wareHouseUpdateDto);

        // 保存更新后的仓库实体
        wareHouseRepository.save(wareHouse);
    }

    // 删除仓库(物理删除，等级危险)
    @Transactional
    public void delete(Long wareHouseId) {
        // 先查找仓库是否存在
        WareHouse wareHouse = wareHouseRepository.findById(wareHouseId)
                .orElseThrow(() -> new BusinessException("仓库不存在"));

        // 删除仓库实体
        wareHouseRepository.delete(wareHouse);
    }

    // 查询单个仓库
    @Transactional
    public WareHouseInfo search(Long wareHouseId) {
        WareHouse wareHouse =  wareHouseRepository.findById(wareHouseId)
                .orElseThrow(() -> new BusinessException("仓库不存在"));

        return WareHouseConverter.toInfo(wareHouse);
    }

    // 查询整个仓库列表
    public List<WareHouseInfo> searchList() {
        List<WareHouse> wareHouseList = wareHouseRepository.findAll();
        return WareHouseConverter.toInfoList(wareHouseList);
    }

    // 登录
    public WareHouseInfo login(LoginDto dto, HttpServletResponse response) {
        // 先提取出来，更加方便操作
        String account = dto.getAccount();
        String password = dto.getPassword();

        // 先查找仓库是否存在
        WareHouse wareHouse = wareHouseRepository.findByAccount(account)
                .orElseThrow(() -> new BusinessException("仓库不存在"));

        // 校验密码
        String dbPassword = wareHouse.getWareHousePassword();
        StatusEnum status = wareHouse.getStatus();

        // 校验密码是否正确
        if(!passwordEncoder.matches(password, dbPassword)) {
            throw new BusinessException("密码错误");
        }

        // 校验仓库状态是否启用
        if(status.equals(StatusEnum.DISABLE)) {
            throw new BusinessException("仓库已禁用！请联系管理员处理！");
        }

        Long wareHouseId = wareHouse.getWareHouseId();
        Map<String , Object> claims = new HashMap<>();
        claims.put("role","WAREHOUSE");
        String token = jwtUtil.generateToken(wareHouseId,claims);

        // 设置Cookie
        Cookie cookie = new Cookie("token",token);
        cookie.setPath("/");
        cookie.setMaxAge(3600*24*7);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return WareHouseConverter.toInfo(wareHouse);
    }

    // 注销
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
