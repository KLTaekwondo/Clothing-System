package com.superkl.backend.service.order;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.dto.order.OrderCreateDto;
import com.superkl.backend.dto.order.OrderItemCreateDto;
import com.superkl.backend.entity.basic.Employee;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.entity.order.Order;
import com.superkl.backend.entity.order.OrderItem;
import com.superkl.backend.enums.DirectionEnum;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class OrderServiceTest {
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
    // 快捷订单项数据
    private static final BigDecimal price = new BigDecimal("20.00");
    private static final BigDecimal normalDiscount = new BigDecimal("1.00");
    // ===== 启用的仓库 =====
    private static final String WAREHOUSE_CODE = "WH001";
    private static final String WAREHOUSE_NAME = "测试仓库";
    private static final Long WAREHOUSE_ID = 1L;

    // ===== 禁用的仓库 =====
    private static final String D_WAREHOUSE_CODE = "WH002";
    private static final String D_WAREHOUSE_NAME = "测试仓库2";
    private static final Long D_WAREHOUSE_ID = 2L;

    // ==== 启动的仓库2 =====
    private static final String WAREHOUSE2_CODE = "WH003";
    private static final String WAREHOUSE2_NAME = "测试仓库3";
    private static final Long WAREHOUSE2_ID = 3L;

    // ===== 测试员工，位于启用仓库中 =====
    private static final String EWE_EMPLOYEE_NAME = "测试员工";
    private static final String EWE_EMPLOYEE_CODE = "EMP001";
    private static final Long EWE_EMPLOYEE_ID = 10L; // 测试员工 ID

    // ===== 测试员工，位于禁用仓库中 =====
    private static final String DWE_EMPLOYEE_NAME = "测试员工2";
    private static final String DWE_EMPLOYEE_CODE = "EMP002";
    private static final Long DWE_EMPLOYEE_ID = 20L; // 测试员工 ID

    // ===== 禁用员工 位于启用仓库中 =====
    private static final String EWD_EMPLOYEE_NAME = "测试员工3";
    private static final String EWD_EMPLOYEE_CODE = "EMP003";
    private static final Long EWD_EMPLOYEE_ID = 30L; // 测试员工 ID

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

        // 模拟登录不在此处，而是在测试方法中调用，如果在这里使用，则全部测试用例都使用这个登录用户
        // 但是可以统一清除 SecurityContextHolder，避免测试用例之间互相污染
        clearContext();
    }

    // 测试用例
    // 1.测试正常状态下写入订单-> 返回正常的订单号，同时有操作信息
    @Test
    void test_NormalOrder(){
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        stubE_E();
        stubOrderItems(price, 1,0, normalDiscount, normalDiscount);
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE_ID, EWE_EMPLOYEE_ID, new BigDecimal("20.00"), new BigDecimal("20.00"));

        // 断言
        String orderNo = orderService.complete(dto);

        assertNotNull(orderNo);
        verify(orderRepository).save(any(Order.class));
    }

    // 2.测试前后端金额错误的情况下写入订单 -> 抛出异常
    @Test
    void test_WrongFrontAmount(){
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        stubE_E();
        stubOrderItems(price, 1,0, normalDiscount, normalDiscount);
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE_ID, EWE_EMPLOYEE_ID, new BigDecimal("15.00"), new BigDecimal("15.00"));

        // 断言
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.complete(dto));
        assertEquals("订单金额与后台计算金额不一致！", ex.getMessage());
        log.warn(ex.getMessage());

        // 验证不保存是否生效
        verify(orderRepository, never()).save(any(Order.class));

    }

    // 3.测试禁用仓库的情况下写入订单 -> 抛出异常
    @Test
    void test_DisabledWarehouseComplete() {
        // 登录仓库，并创造数据
        login(D_WAREHOUSE_ID, D_WAREHOUSE_NAME, D_WAREHOUSE_CODE);
        stubD_E();
        OrderCreateDto dto = buildOrderCreateDto(D_WAREHOUSE_ID, DWE_EMPLOYEE_ID, new BigDecimal("20.00"), new BigDecimal("20.00"));

        // 断言
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("仓库已禁用！不可创建订单！"));
        log.warn(ex.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 4.测试启用仓库，员工禁用的情况下写入订单 -> 抛出异常
    @Test
    void test_DisabledEmployeeComplete() {
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        stubE_D();
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE_ID, EWD_EMPLOYEE_ID, new BigDecimal("20.00"), new BigDecimal("20.00"));

        // 断言
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("员工已禁用！不可创建订单！"));
        log.warn(ex.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 5.测试正常混合模式订单 -> 正常写入
    @Test
    void test_MixedOrderComplete() {
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        stubE_E();
        stubOrderItems(price, 1,1, normalDiscount, normalDiscount);
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE_ID, EWE_EMPLOYEE_ID, new BigDecimal("00.00"), new BigDecimal("00.00"));

        // 断言
        String orderNo = orderService.complete(dto);

        assertNotNull(orderNo);
        verify(orderRepository).save(any(Order.class));
    }

    // 6.测试登录不同仓库情况下，利用其他的仓库创建订单 -> 抛出异常
    @Test
    void test_DifferentWarehouseComplete() {
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE2_ID, EWE_EMPLOYEE_ID, new BigDecimal("20.00"), new BigDecimal("20.00"));

        // 断言
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("您没有权限操作该订单！"));
        log.warn(ex.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 7.测试登录相同仓库的情况下，利用其他仓库的员工创建订单 -> 抛出异常
    @Test
    void test_DifferentEmployeeComplete() {
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        // 情况特殊，手动构造员工所属仓库
        WareHouse BelongWarehouse = WareHouse.builder()
                .wareHouseId(WAREHOUSE2_ID)
                .wareHouseCode(WAREHOUSE2_CODE)
                .wareHouseName(WAREHOUSE2_NAME)
                .status(StatusEnum.ENABLE)
                .build();
        // 构造测试员工
        buildEmployee(EWE_EMPLOYEE_ID, EWE_EMPLOYEE_NAME, EWE_EMPLOYEE_CODE, BelongWarehouse, StatusEnum.ENABLE);
        // 构造登录仓库
        buildWareHouse(WAREHOUSE_ID, WAREHOUSE_CODE, WAREHOUSE_NAME, StatusEnum.ENABLE);
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE_ID, EWE_EMPLOYEE_ID, new BigDecimal("20.00"), new BigDecimal("20.00"));

        // 断言
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.complete(dto));
        assertTrue(ex.getMessage().contains("员工不属于"));
        log.warn(ex.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 8.非草稿订单无法完成 -> 抛出异常
    @Test
    void test_NotDraftOrderNotComplete() {
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE_ID, EWE_EMPLOYEE_ID, new BigDecimal("20.00"), new BigDecimal("20.00"));
        // 伪造订单ID
        dto.setOrderId(99L);


        WareHouse wareHouse = WareHouse.builder()
                .wareHouseId(WAREHOUSE_ID)
                .wareHouseCode(WAREHOUSE_CODE)
                .wareHouseName(WAREHOUSE_NAME)
                .status(StatusEnum.ENABLE)
                .build();

        // 伪造已完成订单
        Order exiting = Order.builder()
                .orderId(99L)
                .wareHouse(wareHouse)
                .status(OrderStatusEnum.COMPLETED)
                .build();
        when(orderRepository.findById(exiting.getOrderId())).thenReturn(Optional.of(exiting));

        // 断言
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.complete(dto));
        log.warn(ex.getMessage());
        assertTrue(ex.getMessage().contains("订单不是草稿状态，不可完成！"));


        verify(orderRepository, never()).save(any(Order.class));
    }

    // 9. 订单项为空时，无法完成 -> 抛出异常
    @Test
    void test_WithNoItemsComplete() {
        // 登录仓库，并创造数据
        login(WAREHOUSE_ID, WAREHOUSE_NAME, WAREHOUSE_CODE);
        stubE_E();
        OrderCreateDto dto = buildOrderCreateDto(WAREHOUSE_ID, EWE_EMPLOYEE_ID, new BigDecimal("20.00"), new BigDecimal("20.00"));
        // 将销售和退回全部设置为空
        dto.setSaleItems(Collections.emptyList());
        dto.setRefundItems(Collections.emptyList());

        // 断言
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.complete(dto));
        log.warn(ex.getMessage());
        assertTrue(ex.getMessage().contains("订单项不能为空"));

        verify(orderRepository, never()).save(any(Order.class));
    }

    // 辅助方法
    // 模拟登录
    private void login(Long RequestId, String Name, String Code) {
        RequestUser user = RequestUser.builder()
                .requestId(RequestId)
                .requestRole("ROLE_WAREHOUSE")// 这里只给仓库角色，管理员可能越权，导致测试失败
                .requestName(Name)
                .requestCode(Code)
                .build();

        // 注入 SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.singleton(user::getRequestRole))
        );
    }

    // 清除 SecurityContextHolder，避免测试用例之间互相污染
    private void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // 构造测试用的仓库
    private WareHouse buildWareHouse(Long wareHouseId, String wareHouseCode, String wareHouseName, StatusEnum status) {
        WareHouse wareHouse = WareHouse.builder()
                .wareHouseId(wareHouseId)
                .wareHouseCode(wareHouseCode)
                .wareHouseName(wareHouseName)
                .status(status)
                .build();

        // 直接注册仓库，避免在测试中调用 save 方法
        when(wareHouseRepository.findById(wareHouseId)).thenReturn(Optional.of(wareHouse));

        return wareHouse;
    }

    // 构造测试用的员工
    private Employee buildEmployee(Long employeeId, String employeeName, String employeeCode, WareHouse wareHouse , StatusEnum status) {
        Employee employee = Employee.builder()
                .employeeId(employeeId)
                .employeeName(employeeName)
                .employeeCode(employeeCode)
                .wareHouse(wareHouse)
                .status(status)
                .build();

        // 直接注册员工，避免在测试中调用 save 方法
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        return employee;
    }

    // 构造一个订单项实体
    private List<OrderItem> buildOrderItem(BigDecimal price, Integer quantity, BigDecimal discount, DirectionEnum direction) {
        OrderItem item = OrderItem.builder()
                .skuId(1L)
                .skuCode("TEST001")
                .skuName("测试SKU")
                .unitPrice(price)
                .quantity(quantity)
                .discount(discount)
                .totalPrice(price.multiply(BigDecimal.valueOf(quantity)))
                .actualPrice(price.multiply(BigDecimal.valueOf(quantity)))
                .direction(direction)
                .build();

        List<OrderItem> orderItems;

        List<OrderItem> saleItems = new ArrayList<>();
        List<OrderItem> refundItems = new ArrayList<>();
        if(DirectionEnum.IN.equals(direction)){
            // 销售项
            saleItems.add(item);
            orderItems = saleItems;
            when(orderItemService.createList(anyList(), any(Order.class), eq(direction))).thenReturn(saleItems);
        }else{
            // 退款项
            refundItems.add(item);
            orderItems = refundItems;
            when(orderItemService.createList(anyList(), any(Order.class), eq(direction))).thenReturn(refundItems);
        }

        return orderItems;
    }


    private OrderCreateDto buildOrderCreateDto(Long wareHouseId, Long employeeId, BigDecimal actualAmount, BigDecimal totalAmount) {
        OrderCreateDto dto = new OrderCreateDto();
        // 开始设置数据
        dto.setPayMethod(PayMethodEnum.CASH);
        dto.setEmployeeId(employeeId);
        dto.setWareHouseId(wareHouseId);
        dto.setActualAmount(actualAmount);
        dto.setTotalAmount(totalAmount);
        dto.setRemark("单元测试订单");

        // 随便设置一个订单项，因为真正的计算需要自己手动操作
        OrderItemCreateDto itemDto = new OrderItemCreateDto();
        itemDto.setSkuCode("TEST001");
        itemDto.setQuantity(1);
        itemDto.setDiscount(new BigDecimal("1.00"));
        dto.setSaleItems(Collections.singletonList(itemDto));
        dto.setRefundItems(Collections.singletonList(itemDto));

        return dto;
    }

    // 生成启用的仓库 + 员工
    private void stubE_E() {
        // 构造测试仓库
        WareHouse E_WAREHOUSE = buildWareHouse(WAREHOUSE_ID, WAREHOUSE_CODE, WAREHOUSE_NAME, StatusEnum.ENABLE);
        // 构造测试员工，未使用变量，为了直观展示参数
        Employee EWE_EMPLOYEE = buildEmployee(EWE_EMPLOYEE_ID, EWE_EMPLOYEE_NAME, EWE_EMPLOYEE_CODE, E_WAREHOUSE, StatusEnum.ENABLE);

    }

    // 生成禁用的仓库 + 启用的员工
    private void stubD_E() {
        // 构造测试仓库
        WareHouse D_WAREHOUSE = buildWareHouse(D_WAREHOUSE_ID, D_WAREHOUSE_CODE, D_WAREHOUSE_NAME, StatusEnum.DISABLE);
        // 构造测试员工，未使用变量，为了直观展示参数
        Employee DWE_EMPLOYEE = buildEmployee(DWE_EMPLOYEE_ID, DWE_EMPLOYEE_NAME, DWE_EMPLOYEE_CODE, D_WAREHOUSE, StatusEnum.ENABLE);
    }

    // 生成启用的仓库 + 禁用的员工
    private void stubE_D() {
        // 构造测试仓库
        WareHouse E_WAREHOUSE = buildWareHouse(WAREHOUSE_ID, WAREHOUSE_CODE, WAREHOUSE_NAME, StatusEnum.ENABLE);
        // 构造测试员工，未使用变量，为了直观展示参数
        Employee EWD_EMPLOYEE = buildEmployee(EWD_EMPLOYEE_ID, EWD_EMPLOYEE_NAME, EWD_EMPLOYEE_CODE, E_WAREHOUSE, StatusEnum.DISABLE);
    }


    // 一次装配订单项
    private void stubOrderItems(BigDecimal price , Integer S_Quantity , Integer R_Quantity , BigDecimal S_Discount , BigDecimal R_Discount) {
        List<OrderItem> saleItems = buildOrderItem(price, S_Quantity, S_Discount, DirectionEnum.IN);
        List<OrderItem> refundItems = buildOrderItem(price, R_Quantity, R_Discount, DirectionEnum.OUT);
    }
}