package com.superkl.backend.service;

import com.superkl.backend.converter.SupplierConverter;
import com.superkl.backend.dto.SupplierCreateDto;
import com.superkl.backend.dto.SupplierUpdateDto;
import com.superkl.backend.entity.Admin;
import com.superkl.backend.entity.Supplier;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.SupplierInfo;
import com.superkl.backend.repository.AdminRepository;
import com.superkl.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final AdminRepository adminRepository;

    // 1. 创建供应商
    @Transactional
    public void create (SupplierCreateDto dto){
        Admin admin = adminRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        Supplier supplier = SupplierConverter.toEntity(dto , admin);
        supplierRepository.save(supplier);
    }

    // 2. 更新供应商
    @Transactional
    public void update (Long supplierId , SupplierUpdateDto dto){
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        SupplierConverter.updateEntity(supplier , dto);
        supplierRepository.save(supplier);
    }

    // 3. 删除供应商
    @Transactional
    public void delete (Long supplierId){
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        supplier.setStatus(StatusEnum.DISABLE);
        supplierRepository.save(supplier);
    }

    // 4. 查询单个供应商
    public SupplierInfo search (String supplierCode){
        Supplier supplier = supplierRepository.findBySupplierCode(supplierCode)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        return SupplierConverter.toInfo(supplier);
    }

    // 5. 查询所有供应商
    public List<SupplierInfo> searchList () {
        return supplierRepository.findAll().stream()
                .map(SupplierConverter::toInfo)
                .toList();
    }
}
