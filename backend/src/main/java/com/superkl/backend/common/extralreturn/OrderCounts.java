package com.superkl.backend.common.extralreturn;

import com.superkl.backend.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCounts extends Counts {
    private OrderStatusEnum orderStatus;

    // native 查询回来的是 String，Hibernate 不会自动转枚举，这里手动转
    public OrderCounts(String orderStatus, Long mount) {
        super(mount);
        this.orderStatus = OrderStatusEnum.valueOf(orderStatus);
    }
}
