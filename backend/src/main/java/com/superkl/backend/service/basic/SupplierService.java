package com.superkl.backend.service.basic;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.basic.SupplierConverter;
import com.superkl.backend.dto.basic.SupplierCreateDto;
import com.superkl.backend.dto.basic.SupplierUpdateDto;
import com.superkl.backend.entity.basic.Admin;
import com.superkl.backend.entity.basic.Supplier;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.basic.SupplierInfo;
import com.superkl.backend.repository.basic.AdminRepository;
import com.superkl.backend.repository.basic.SupplierRepository;
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
        Admin admin = adminRepository.findById(RequestUser.notNull().getRequestId())
                .orElseThrow(() -> new BusinessException("管理员不存在"));
        // 检查是否已经存在一样的供应商编号
        if(supplierRepository.existsByCode(dto.getSupplierCode())){
            throw new BusinessException(403, "供应商编号已存在！");
        }

        Supplier supplier = SupplierConverter.toEntity(dto , admin);
        supplierRepository.save(supplier);
    }

    // 2. 更新供应商
    @Transactional
    public void update (Long supplierId , SupplierUpdateDto dto){
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new BusinessException("供应商不存在"));
        // 检查是否和其他的供应商编号重复
        String code = dto.getSupplierCode();
        if(supplierRepository.existsByCode(code) && !code.equals(supplier.getSupplierCode())){
            throw new BusinessException(403, "供应商编号已存在！");
        }
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
