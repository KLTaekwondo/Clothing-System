package com.superkl.backend.service.dash;

import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CacheDashService {
    private final OrderRepository orderRepository;
    private final CacheManager cacheManager;

    @Cacheable(cacheNames = "DaysCache", key = "#day")
    public BigDecimal sumSomeDaySaleAmount(LocalDate day){
        return orderRepository.sumTotalAmountByTime(day.atStartOfDay(),
                day.plusDays(1).atStartOfDay(),
                DirectionEnum.IN,
                DirectionEnum.OUT,
                OrderStatusEnum.COMPLETED);
    }

    public void clearCache(LocalDate day) {
        Cache cache = cacheManager.getCache("DaysCache");
        if (cache != null) {
            cache.evict(day);
        }
    }
}
