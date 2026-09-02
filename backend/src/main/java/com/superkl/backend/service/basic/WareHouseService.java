package com.superkl.backend.service.basic;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.basic.WareHouseConverter;
import com.superkl.backend.dto.basic.LoginDto;
import com.superkl.backend.dto.basic.WareHouseCreateDto;
import com.superkl.backend.dto.basic.WareHouseUpdateDto;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.entity.stock.WareHouseStock;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.basic.WareHouseInfo;
import com.superkl.backend.repository.product.ProductSkuRepository;
import com.superkl.backend.repository.basic.WareHouseRepository;
import com.superkl.backend.repository.stock.WareHouseStockRepository;
import com.superkl.backend.service.auth.AuthRedisService;
import com.superkl.backend.utils.AuthContext;
import com.superkl.backend.utils.JwtUtil;
import com.superkl.backend.utils.TokenCookieManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WareHouseService {
    private final WareHouseRepository wareHouseRepository;
    private final WareHouseStockRepository wareHouseStockRepository;
    private final ProductSkuRepository productSkuRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthRedisService authRedisService;
    private final JwtUtil jwtUtil;

    @Value("${auth.cookie.secure}")
    private boolean isSecure;

    // 新增仓库
    @Transactional
    public void create(WareHouseCreateDto wareHouseCreateDto) {
        // 检查仓库编号是否已经存在
        if(wareHouseRepository.existsByCode(wareHouseCreateDto.getCode())){
            throw new BusinessException(ErrorCodeEnum.RULE_CONFLICT, "仓库编号已存在！");
        }
        // 检查仓库名称是否已经存在
        if(wareHouseRepository.existsByName(wareHouseCreateDto.getName())){
            throw new BusinessException(ErrorCodeEnum.RULE_CONFLICT, "仓库名称已存在！");
        }

        // 转换为仓库实体
        WareHouse wareHouse = WareHouseConverter.toEntity(wareHouseCreateDto);
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(wareHouseCreateDto.getPassword());
        wareHouse.setWareHousePassword(encryptedPassword);
        // 保存仓库实体
        wareHouseRepository.save(wareHouse);
        log.info("新增仓库：{}", wareHouse.getWareHouseName());
        RequestUser.log();

        // 处理库存问题
        List<WareHouseStock> stocks = new ArrayList<>();
        // 遍历商品SKU列表，创建库存实体
        productSkuRepository.findAll().forEach(productSku -> {
            WareHouseStock stock = new WareHouseStock();
            stock.setWareHouse(wareHouse);
            stock.setProductSku(productSku);
            stock.setStock(0);
            stocks.add(stock);
        });

        // 保存库存实体
        wareHouseStockRepository.saveAll(stocks);

    }

    // 更新仓库
    @Transactional
    public void update(Long wareHouseId, WareHouseUpdateDto wareHouseUpdateDto) {
        // 先查找仓库是否存在
        WareHouse wareHouse = wareHouseRepository.findById(wareHouseId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "仓库不存在"));

        // 检查是否和其他的仓库编号重复
        String code = wareHouseUpdateDto.getCode();
        if(wareHouseRepository.existsByCode(code) && !code.equals(wareHouse.getWareHouseCode())){
            throw new BusinessException(ErrorCodeEnum.RULE_CONFLICT, "仓库编号已存在！");
        }

        // 检查是否和其他的仓库名称重复
        String name = wareHouseUpdateDto.getName();
        if(wareHouseRepository.existsByName(name) && !name.equals(wareHouse.getWareHouseName())){
            throw new BusinessException(ErrorCodeEnum.RULE_CONFLICT, "仓库名称已存在！");
        }

        // 更新仓库实体
        WareHouseConverter.updateEntity(wareHouse, wareHouseUpdateDto);
        // 加密密码
        if(wareHouseUpdateDto.getPassword() != null){
            String encryptedPassword = passwordEncoder.encode(wareHouseUpdateDto.getPassword());
            wareHouse.setWareHousePassword(encryptedPassword);
        }
        // 保存更新后的仓库实体
        wareHouseRepository.save(wareHouse);
        log.info("更新仓库：{}", wareHouse.getWareHouseName());
        RequestUser.log();
    }

    // 删除仓库(物理删除，等级危险)
    @Transactional
    public void delete(Long wareHouseId) {
        // 先查找仓库是否存在
        WareHouse wareHouse = wareHouseRepository.findById(wareHouseId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "仓库不存在"));

        // 禁用仓库状态
        wareHouse.setStatus(StatusEnum.DISABLE);
        wareHouseRepository.save(wareHouse);
        log.info("禁用仓库：{}", wareHouse.getWareHouseName());
        RequestUser.log();
    }

    // 查询单个仓库
    @Transactional(readOnly = true)
    public WareHouseInfo search(Long wareHouseId) {
        WareHouse wareHouse =  wareHouseRepository.findById(wareHouseId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "仓库不存在"));

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
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "仓库不存在"));

        // 校验密码
        String dbPassword = wareHouse.getWareHousePassword();
        StatusEnum status = wareHouse.getStatus();

        // 校验密码是否正确
        if(!passwordEncoder.matches(password, dbPassword)) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "密码错误");
        }

        // 校验仓库状态是否启用
        if(status.equals(StatusEnum.DISABLE)) {
            throw new BusinessException(ErrorCodeEnum.RULE_FORBIDDEN, "仓库已禁用！请联系管理员处理！");
        }

        Long wareHouseId = wareHouse.getWareHouseId();
        Map<String , Object> claims = new HashMap<>();
        claims.put("role","WAREHOUSE");
        claims.put("username", wareHouse.getWareHouseName());
        claims.put("code", wareHouse.getWareHouseCode());
        String token = jwtUtil.generateToken(wareHouseId,claims);

        // 设置Cookie
        Cookie cookie = TokenCookieManager.writeTokenCookie(token, isSecure);
        response.addCookie(cookie);

        // 设置redis
        authRedisService.recordToken("ROLE_WAREHOUSE", wareHouseId, token, Duration.ofDays(7));
        RequestUser.log();
        log.info("仓库登录成功：{}", account);
        return WareHouseConverter.toInfo(wareHouse);
    }

    // 注销
    public void logout(HttpServletRequest request , HttpServletResponse response) {
        // 从请求中获取token
        String token = AuthContext.extractTokenFromCookie(request, jwtUtil);
        Long wareHouseId = RequestUser.notNull().getRequestId();
        // 从redis中删除token
        if(token != null) {
            authRedisService.kick("ROLE_WAREHOUSE", wareHouseId, token);
        }

        // 清空后再覆盖cookie
        Cookie cookie = TokenCookieManager.clearTokenCookie(isSecure);
        response.addCookie(cookie);
        RequestUser.log();
        log.info("仓库注销成功：{}", wareHouseId);
    }
}
