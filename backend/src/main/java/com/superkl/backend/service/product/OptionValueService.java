package com.superkl.backend.service.product;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.product.OptionValueConverter;
import com.superkl.backend.dto.product.OptionValueCreateDto;
import com.superkl.backend.dto.product.OptionValueUpdateDto;
import com.superkl.backend.entity.product.OptionValue;
import com.superkl.backend.enums.OptionTypeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.product.OptionValueInfo;
import com.superkl.backend.repository.product.OptionValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptionValueService {
    private final OptionValueRepository optionValueRepository;


    // 1. 创建选项值
    @Transactional
    public void create(OptionValueCreateDto dto) {
        // 1. 检查选项值是否存在
        if (optionValueRepository.existsByTypeAndValue(dto.getOptionType(), dto.getOptionValue())) {
            throw new BusinessException(403, "选项值已存在");
        }
        // 2. 创建选项值实体
        OptionValue optionValue = OptionValueConverter.toEntity(dto);
        // 3. 保存选项值
        optionValueRepository.save(optionValue);
        log.info("新增选项值：{}，类型：{}", optionValue.getOptionValue(), optionValue.getOptionType());
        RequestUser.log();
    }

    // 2. 更新选项值
    @Transactional
    public void update(Long id ,  OptionValueUpdateDto dto) {
        // 1. 从数据库中查询选项值
        OptionValue optionValue = optionValueRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "选项值不存在"));
        if(optionValueRepository.existsByTypeAndValueNotId(id,dto.getOptionType(),dto.getOptionValue())) {
            throw new BusinessException(403,"选项值已经存在");
        }

        // 2. 更新选项值
        OptionValueConverter.updateEntity(optionValue, dto);
        // 3. 保存更新后的选项值
        optionValueRepository.save(optionValue);
        log.info("更新选项值：ID={}", id);
        RequestUser.log();
    }

    // 3. 删除选项值
    @Transactional
    public void delete(Long id) {
        // 1. 从数据库中查询选项值
        OptionValue optionValue = optionValueRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "选项值不存在"));
        // 2. 检查是否被引用
        if(optionValueRepository.existsBySpecExactValue(optionValue.getOptionValue())){
            throw new BusinessException(403,"选项值被引用，不能删除");
        }
        // 3. 删除选项值
        optionValueRepository.deleteById(id);
        log.info("删除选项值：ID={}", id);
        RequestUser.log();
    }

    // 4. 查询选项值
    public OptionValueInfo search(Long id) {
        // 1. 从数据库中查询选项值
        OptionValue optionValue = optionValueRepository.findById(id)
                .orElseThrow(() -> new BusinessException(403, "选项值不存在"));
        // 2. 转换为选项值信息
        return OptionValueConverter.toInfo(optionValue);
    }

    // 5. 查询选项值列表
    public List<OptionValueInfo> searchList() {
        // 1. 从数据库中查询选项值列表
        List<OptionValue> optionValueList = optionValueRepository.findAll();
        // 2. 转换为选项值信息列表
        return OptionValueConverter.toInfoList(optionValueList);
    }

    // 6. 类别查询列表
    public List<OptionValueInfo> searchListByType(String type) {
        // 1. 从数据库中查询选项值列表
        OptionTypeEnum oType ;
        try{
            oType = OptionTypeEnum.valueOf(type);
        }catch(Exception e){
            throw new BusinessException(403,"类型不存在！");
        }
        List<OptionValue> optionValueList = optionValueRepository.findByOptionType(oType);
        // 2. 转换为选项值信息列表
        return OptionValueConverter.toInfoList(optionValueList);
    }
}
