package com.superkl.backend.service;

import com.superkl.backend.converter.OptionValueConverter;
import com.superkl.backend.dto.OptionValueCreateDto;
import com.superkl.backend.dto.OptionValueUpdateDto;
import com.superkl.backend.entity.OptionValue;
import com.superkl.backend.enums.OptionTypeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.OptionValueInfo;
import com.superkl.backend.repository.OptionValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionValueService {
    private final OptionValueRepository optionValueRepository;


    // 1. 创建选项值
    @Transactional
    public void create(OptionValueCreateDto optionValueCreateDto) {
        // 1. 构造选项值实体
        OptionValue optionValue = OptionValueConverter.toEntity(optionValueCreateDto);
        // 2. 保存选项值
        optionValueRepository.save(optionValue);
    }

    // 2. 更新选项值
    @Transactional
    public void update(Long id ,  OptionValueUpdateDto optionValueUpdateDto) {
        // 1. 从数据库中查询选项值
        OptionValue optionValue = optionValueRepository.findById(id).orElseThrow(() -> new BusinessException("选项值不存在"));
        // 2. 更新选项值
        OptionValueConverter.updateEntity(optionValue, optionValueUpdateDto);
        // 3. 保存更新后的选项值
        optionValueRepository.save(optionValue);
    }

    // 3. 删除选项值
    @Transactional
    public void delete(Long id) {
        // 1. 从数据库中查询选项值
        OptionValue optionValue = optionValueRepository.findById(id).orElseThrow(() -> new BusinessException("选项值不存在"));
        // 2. 删除选项值
        optionValueRepository.deleteById(id);
    }

    // 4. 查询选项值
    public OptionValueInfo search(Long id) {
        // 1. 从数据库中查询选项值
        OptionValue optionValue = optionValueRepository.findById(id).orElseThrow(() -> new BusinessException("选项值不存在"));
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
    public List<OptionValueInfo> searchListByType(OptionTypeEnum type) {
        // 1. 从数据库中查询选项值列表
        List<OptionValue> optionValueList = optionValueRepository.findByOptionType(type);
        // 2. 转换为选项值信息列表
        return OptionValueConverter.toInfoList(optionValueList);
    }
}
