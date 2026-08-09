package com.superkl.backend.entity.order;

import com.superkl.backend.entity.BaseEntity;
import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "t_order")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Order extends BaseEntity {

    // 订单本身属性
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false,unique = true)
    private String orderNo;// 订单号

    @Column(nullable =false)
    @Enumerated(EnumType.STRING)
    private PayMethodEnum payMethod;// 支付方式

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatusEnum status = OrderStatusEnum.DRAFT;// 订单状态，默认挂单

    @Column(nullable =false)
    private BigDecimal totalPrice;// 订单总价

    @Column(nullable =false)
    private BigDecimal actualPrice;// 实际支付金额

    @Column(length = 100)
    private String remark;// 订单备注

    private String memberPhone;// 会员手机号

    // 关联属性
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<OrderItem> orderItems = new HashSet<>();// 订单项列表

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;// 关联员工

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private WareHouse wareHouse;// 关联仓库

    // 辅助方法
    public boolean isDraft() {
        return OrderStatusEnum.DRAFT.equals(status);
    }

    public boolean isCompleted() {
        return OrderStatusEnum.COMPLETED.equals(status);
    }
}
