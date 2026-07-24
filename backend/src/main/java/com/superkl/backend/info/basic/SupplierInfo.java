package com.superkl.backend.info.basic;

import com.superkl.backend.info.BaseInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SupplierInfo extends BaseInfo {
    private String supplierCode;
    private String supplierName;
    private String contactPhone;
    private String remark;
}
