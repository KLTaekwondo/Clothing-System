package com.superkl.backend.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
public class PageParam {
    @Min(value = 0, message = "页码不能小于0")
    private int page = 0;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 40, message = "每页数量不能大于40")
    private int size = 10;


    // 私有方法，将PageParam转换为Pageable
    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }
}
