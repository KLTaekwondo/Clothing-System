package com.superkl.backend.service.order;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.dto.order.OrderCreateDto;
import com.superkl.backend.dto.order.OrderItemCreateDto;
import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.entity.order.Order;
import com.superkl.backend.entity.order.OrderItem;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.OrderStatusEnum;
import com.superkl.backend.enums.PayMethodEnum;
import com.superkl.backend.enums.StatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.repository.basic.EmployeeRepository;
import com.superkl.backend.repository.basic.WareHouseRepository;
import com.superkl.backend.repository.order.OrderRepository;
import com.superkl.backend.service.basic.MemberService;
import com.superkl.backend.service.dash.CacheDashService;
import com.superkl.backend.service.stock.WareHouseStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 教学版测试：OrderService 的订单金额双向校验
 *
 * 套路讲解（三步走）：
 * 1. @ExtendWith(MockitoExtension.class) —— 启用 Mockito，让 @Mock 生效
 * 2. @Mock 声明所有依赖 —— 不连数据库，全部用假对象
 * 3. 每个测试方法 = 准备数据(given) → 调用(when) → 断言结果(then)
 *
 * 为什么这么测：
 * - OrderService 的 applyOrder() 里有"前端金额 vs 后端计算金额"的双向校验，
 *   这是业务核心规则，错一个数就是钱的问题，必须测
 * - 不测 CRUD 查询，因为那是 Spring Data JPA 框架干的活，测了等于白测
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    // ========== 第一步：声明所有依赖（@Mock 生成假对象） ==========

    @Mock
    private MemberService memberService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WareHouseRepository wareHouseRepository;

    @Mock
    private WareHouseStockService wareHouseStockService;

    @Mock
    private CacheDashService cacheDashService;

    // 被测对象：把上面 7 个假对象注入 OrderService（等价于 Spring 的 @Autowired）
    private OrderService orderService;

    // 测试用的公共数据
    private static final Long WAREHOUSE_ID = 1L;
    private static final Long EMPLOYEE_ID = 10L;

    @BeforeEach
    void setUp() {
        // 每次测试前，new 一个新的 OrderService，防止用例之间互相污染
        orderService = new OrderService(
                memberService,
                orderRepository,
                orderItemService,
                employeeRepository,
                wareHouseRepository,
                wareHouseStockService,
                cacheDashService
        );

        // 造一个"当前登录用户"（RequestUser 是静态方法读 SecurityContextHolder）
        // 不这么做，checkPermission() 里 RequestUser.isAdmin() 会返回 false
        RequestUser currentUser = RequestUser.builder()
                .requestId(WAREHOUSE_ID)
                .requestRole("ROLE_ADMIN")
                .requestName("测试管理员")
                .requestCode("ADMIN001")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                // 注意：必须用三参构造器！两参构造器创建的 token 是未认证状态(isAuthenticated=false)，
                // RequestUser.current() 第一行就检查 isAuthenticated()，会导致权限校验全部失败
                new UsernamePasswordAuthenticationToken(currentUser, null, List.of())
        );
    }

    // ========== 用例 1：金额一致 → 订单正常完成 ==========

    @Test
    void complete_whenAmountMatches_shouldSucceed() {
        // given：准备一个合法的订单请求
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("100.00"),   // 前端实付金额
                new BigDecimal("100.00")    // 前端总金额
        );

        // 员工存在且启用、仓库存在且启用
        Employee employee = buildEmployee(EMPLOYEE_ID, WAREHOUSE_ID, StatusEnum.ENABLE);
        WareHouse wareHouse = buildWareHouse(WAREHOUSE_ID, StatusEnum.ENABLE);
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(wareHouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(wareHouse));

        // 订单项服务返回"销售1件100元"的订单项（金额计算在 OrderItemConverter，这里直接给结果）
        OrderItem saleItem = buildOrderItem(new BigDecimal("100.00"), 1, DirectionEnum.IN);
        when(orderItemService.createList(anyList(), any(Order.class), eq(DirectionEnum.IN)))
                .thenReturn(List.of(saleItem));
        when(orderItemService.createList(anyList(), any(Order.class), eq(DirectionEnum.OUT)))
                .thenReturn(List.of());

        // when：调用被测方法（complete = 完成订单）
        String orderNo = orderService.complete(dto);

        // then：断言返回了订单号，且订单被保存
        assertNotNull(orderNo);
        verify(orderRepository).save(any(Order.class));
    }

    // ========== 用例 2：实付金额不一致 → 抛异常 ==========

    @Test
    void complete_whenActualAmountMismatch_shouldThrow() {
        // given：前端说实付 90，但订单项算出来是 100 → 必然不一致
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("90.00"),
                new BigDecimal("100.00")
        );
        stubBaseData(dto);
        stubOrderItems(new BigDecimal("100.00"), 1, List.of());

        // when + then：调用后必须抛 BusinessException，且消息包含关键词
        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.complete(dto));
        assertEquals(ErrorCodeEnum.RULE_VALID_ERROR.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("订单金额与商品项金额不一致"));

        // 关键：金额不一致时，订单绝不能保存（save 不能被调用）
        verify(orderRepository, never()).save(any(Order.class));
    }

    // ========== 用例 3：总金额不一致 → 抛异常 ==========

    @Test
    void complete_whenTotalAmountMismatch_shouldThrow() {
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("100.00"),
                new BigDecimal("80.00")
        );
        stubBaseData(dto);
        stubOrderItems(new BigDecimal("100.00"), 1, List.of());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("订单金额与商品项金额不一致"));
    }

    // ========== 用例 4：混合订单（销售 + 退货）金额加减法正确 ==========

    @Test
    void complete_whenMixedSaleAndRefund_amountCalculatedCorrectly() {
        // 销售 1 件 100 元 + 退货 1 件 30 元
        // 实付 = 100 - 30 = 70，总价 = 100 - 30 = 70
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("70.00"),
                new BigDecimal("70.00")
        );
        stubBaseData(dto);

        OrderItem saleItem = buildOrderItem(new BigDecimal("100.00"), 1, DirectionEnum.IN);
        OrderItem refundItem = buildOrderItem(new BigDecimal("30.00"), 1, DirectionEnum.OUT);
        when(orderItemService.createList(anyList(), any(Order.class), eq(DirectionEnum.IN)))
                .thenReturn(List.of(saleItem));
        when(orderItemService.createList(anyList(), any(Order.class), eq(DirectionEnum.OUT)))
                .thenReturn(List.of(refundItem));

        // 正常完成，不抛异常
        assertDoesNotThrow(() -> orderService.complete(dto));
    }

    // ========== 用例 5：员工不属于该仓库 → 抛异常 ==========

    @Test
    void complete_whenEmployeeNotInWarehouse_shouldThrow() {
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
        );

        // 员工属于仓库 2，但订单要下在仓库 1
        Employee employee = buildEmployee(EMPLOYEE_ID, 2L, StatusEnum.ENABLE);
        WareHouse wareHouse = buildWareHouse(WAREHOUSE_ID, StatusEnum.ENABLE);
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(wareHouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(wareHouse));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("员工不属于"));
    }

    // ========== 用例 6：员工被禁用 → 抛异常 ==========

    @Test
    void complete_whenEmployeeDisabled_shouldThrow() {
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
        );

        Employee employee = buildEmployee(EMPLOYEE_ID, WAREHOUSE_ID, StatusEnum.DISABLE);
        WareHouse wareHouse = buildWareHouse(WAREHOUSE_ID, StatusEnum.ENABLE);
        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(wareHouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(wareHouse));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("已禁用"));
    }

    // ========== 用例 7：订单项为空 → 抛异常 ==========

    @Test
    void complete_whenOrderItemsEmpty_shouldThrow() {
        // 销售项和退货项都是空列表
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("0.00"),
                new BigDecimal("0.00")
        );
        dto.setSaleItems(List.of());
        dto.setRefundItems(List.of());
        stubBaseData(dto);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("订单项不能为空"));
    }

    // ========== 用例 8：非草稿订单不可完成 ==========

    @Test
    void complete_whenOrderNotDraft_shouldThrow() {
        // 已有订单（orderId 不为空），且状态是已完成 → 不能再次完成
        OrderCreateDto dto = buildOrderDto(
                new BigDecimal("100.00"),
                new BigDecimal("100.00")
        );
        dto.setOrderId(99L);

        Order existing = Order.builder()
                .orderId(99L)
                .status(OrderStatusEnum.COMPLETED)
                .wareHouse(buildWareHouse(WAREHOUSE_ID, StatusEnum.ENABLE))
                .build();
        when(orderRepository.findById(99L)).thenReturn(Optional.of(existing));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("订单不是草稿状态"));
    }

    // ==================================================================
    // 以下是"造数据"的辅助方法（不包含任何断言，纯工具）
    // ==================================================================

    // 构造一个合法的订单请求 DTO
    private OrderCreateDto buildOrderDto(BigDecimal actualAmount, BigDecimal totalAmount) {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setPayMethod(PayMethodEnum.CASH);
        dto.setEmployeeId(EMPLOYEE_ID);
        dto.setWareHouseId(WAREHOUSE_ID);
        dto.setActualAmount(actualAmount);
        dto.setTotalAmount(totalAmount);
        dto.setRemark("单元测试订单");

        // 默认给一条销售项（SKU 编码 TEST001），用例 7 会覆盖成空
        OrderItemCreateDto itemDto = new OrderItemCreateDto();
        itemDto.setSkuCode("TEST001");
        itemDto.setQuantity(1);
        itemDto.setDiscount(new BigDecimal("1.00"));
        dto.setSaleItems(new ArrayList<>(List.of(itemDto)));
        dto.setRefundItems(new ArrayList<>());
        return dto;
    }

    // 造一个员工实体
    private Employee buildEmployee(Long employeeId, Long wareHouseId, StatusEnum status) {
        return Employee.builder()
                .employeeId(employeeId)
                .employeeCode("EMP001")
                .employeeName("测试员工")
                .status(status)
                .wareHouse(buildWareHouse(wareHouseId, StatusEnum.ENABLE))
                .build();
    }

    // 造一个仓库实体
    private WareHouse buildWareHouse(Long wareHouseId, StatusEnum status) {
        return WareHouse.builder()
                .wareHouseId(wareHouseId)
                .wareHouseCode("WH001")
                .wareHouseName("测试仓库")
                .status(status)
                .build();
    }

    // 造一个订单项实体（金额已经算好，模拟 OrderItemConverter 的结果）
    private OrderItem buildOrderItem(BigDecimal price, int quantity, DirectionEnum direction) {
        return OrderItem.builder()
                .skuId(1L)
                .skuCode("TEST001")
                .skuName("测试SKU")
                .unitPrice(price)
                .quantity(quantity)
                .discount(BigDecimal.ONE)
                .totalPrice(price.multiply(BigDecimal.valueOf(quantity)))
                .actualPrice(price.multiply(BigDecimal.valueOf(quantity)))
                .direction(direction)
                .build();
    }

    // 公共造数据：员工 + 仓库存在且启用
    private void stubBaseData(OrderCreateDto dto) {
        when(employeeRepository.findById(EMPLOYEE_ID))
                .thenReturn(Optional.of(buildEmployee(EMPLOYEE_ID, WAREHOUSE_ID, StatusEnum.ENABLE)));
        when(wareHouseRepository.findById(WAREHOUSE_ID))
                .thenReturn(Optional.of(buildWareHouse(WAREHOUSE_ID, StatusEnum.ENABLE)));
    }

    // 公共造数据：订单项服务返回固定金额的销售项
    private void stubOrderItems(BigDecimal price, int quantity, List<OrderItem> refundItems) {
        OrderItem saleItem = buildOrderItem(price, quantity, DirectionEnum.IN);
        when(orderItemService.createList(anyList(), any(Order.class), eq(DirectionEnum.IN)))
                .thenReturn(List.of(saleItem));
        when(orderItemService.createList(anyList(), any(Order.class), eq(DirectionEnum.OUT)))
                .thenReturn(refundItems);
    }
}
