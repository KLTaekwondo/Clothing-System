package com.superkl.backend.service.order;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.order.ImportOrderConverter;
import com.superkl.backend.dto.order.ImportOrderDraftDto;
import com.superkl.backend.dto.stock.StockContext;
import com.superkl.backend.entity.order.ImportOrder;
import com.superkl.backend.entity.order.ImportOrderItem;
import com.superkl.backend.entity.basic.Supplier;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.enums.AuditStatusEnum;
import com.superkl.backend.enums.DirectionEnum;
import com.superkl.backend.enums.StockChangeTypeEnum;
import com.superkl.backend.enums.StockSourceTypeEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.ImportOrderInfo;
import com.superkl.backend.info.order.ImportOrderItemInfo;
import com.superkl.backend.info.order.ImportOrderWithItemsInfo;
import com.superkl.backend.repository.order.ImportOrderItemRepository;
import com.superkl.backend.repository.order.ImportOrderRepository;
import com.superkl.backend.repository.basic.SupplierRepository;
import com.superkl.backend.repository.basic.WareHouseRepository;
import com.superkl.backend.service.stock.WareHouseStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportOrderService {
    private final ImportOrderRepository importOrderRepository;
    private final ImportOrderItemRepository importOrderItemRepository;
    private final WareHouseRepository wareHouseRepository;
    private final SupplierRepository supplierRepository;
    private final ImportOrderItemService importOrderItemService;
    private final WareHouseStockService wareHouseStockService;


    // 创建进货订单，前端对应点击保存按钮
    @Transactional
    public void create(ImportOrderDraftDto dto) {
        // 1.先生成进货订单号，同时校验供应商和仓库是否存在
        ImportOrder importOrder = ImportOrderConverter.toEntity(dto);
        applyImportOrder(importOrder, dto);
    }

    // 更新进货订单，前端更新订单项后，点击保存按钮
    @Transactional
    public void update(Long importOrderId, ImportOrderDraftDto dto) {
        // 首先找订单是否存在
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new BusinessException(403, "进货订单不存在"));
        // 先检查是否是草稿状态
        if(!importOrder.isDraft()){
            throw new BusinessException(405, "进货订单状态不是草稿，不能修改");
        }
        // 清除已存在的商品项
        importOrderItemService.updateDelete(importOrderId);
        // 存储新的商品项
        applyImportOrder(importOrder, dto);
    }

    // 前端提交，后端将进货订单状态设置为审核中
    @Transactional
    public void check(Long importOrderId){
        // 首先找订单是否存在
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new BusinessException(403, "进货订单不存在"));
        // 先检查是否是草稿状态
        if(!importOrder.isDraft()){
            throw new BusinessException(405, "进货订单状态不是草稿，不能设置审核中");
        }
        // 设置审核中状态
        importOrder.setStatus(AuditStatusEnum.CHECKING);
        // 更新订单
        importOrderRepository.save(importOrder);

        // 日志记录
        RequestUser.log();
        log.info("进货订单审核中，订单编号：{}", importOrder.getImportOrderNo());
    }

    // 将进货订单状态设置为已审核，同时将库存增加/减少，根据方向进行
    @Transactional
    public void approve(Long importOrderId){
        // 首先找订单是否存在
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new BusinessException(403, "进货订单不存在"));
        // 先检查是否是审核中状态
        if(!importOrder.isChecking()){
            throw new BusinessException(405, "进货订单状态不是审核中，不能通过");
        }
        // 增加/减少库存
        Long wareHouseId = importOrder.getWareHouse().getWareHouseId();
        List<ImportOrderItem> items = importOrderItemRepository.findByImportOrderId(importOrderId);
        DirectionEnum direction = importOrder.getDirection();

        StockContext context = StockContext.builder()
                .sourceNo(importOrder.getImportOrderNo())
                .sourceType(StockSourceTypeEnum.IMPORT_ORDER)
                .build();
        for(ImportOrderItem item : items) {
            // 增加库存
            if(DirectionEnum.IN.equals(direction)){
                context.setChangeType(StockChangeTypeEnum.IMPORT_IN);
                wareHouseStockService.increaseStock(wareHouseId, item.getSkuId(), item.getQuantity(), context);
            }

            // 减少库存
            if(DirectionEnum.OUT.equals(direction)){
                context.setChangeType(StockChangeTypeEnum.IMPORT_RETURN);
                wareHouseStockService.decreaseStock(wareHouseId, item.getSkuId(), item.getQuantity(), context);
            }
        }
        // 审核通过
        importOrder.setStatus(AuditStatusEnum.APPROVED);

        RequestUser.log();
        log.info("进货订单审核通过，订单编号：{}", importOrder.getImportOrderNo());
    }

    // 拒绝进货订单，前端点击拒绝按钮，后端将进货订单状态设置为已拒绝
    @Transactional
    public void reject(Long importOrderId){
        // 首先找订单是否存在
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new BusinessException(403, "进货订单不存在"));
        // 先检查是否是审核中状态
        if(!importOrder.isChecking()){
            throw new BusinessException(405, "进货订单状态不是审核中，不能拒绝");
        }
        // 审核拒绝
        importOrder.setStatus(AuditStatusEnum.REJECTED);
        // 更新订单
        importOrderRepository.save(importOrder);

        RequestUser.log();
        log.info("进货订单审核拒绝，订单编号：{}", importOrder.getImportOrderNo());
    }

    // 注意：只能删除草稿状态的订单，同时级联删除，关联的所有订单项都会被删除
    @Transactional
    public void delete(Long importOrderId){
        // 首先找订单是否存在
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new BusinessException(403, "进货订单不存在"));
        // 先检查是否是草稿状态
        if(!importOrder.isDraft()){
            throw new BusinessException(405, "进货订单已被处理，不能删除");
        }
        // 删除订单
        importOrderRepository.deleteById(importOrderId);
    }

    // 查询单个进货订单详情
    public ImportOrderWithItemsInfo search(Long importOrderId){
        ImportOrder importOrder = importOrderRepository.findById(importOrderId)
                .orElseThrow(() -> new BusinessException(403, "进货订单不存在"));
        List<ImportOrderItemInfo> items = importOrderItemService.findByImportOrderId(importOrderId);
        return ImportOrderConverter.toInfoWithItems(importOrder, items);
    }

    // 查询进货订单列表
    public PageResult<ImportOrderInfo> searchPage(Pageable pageable){
        Page<ImportOrder> importOrders = importOrderRepository.findPage(pageable);
        return ImportOrderConverter.toInfoPage(importOrders);
    }


    // 辅助方法：校验+创建进货订单项
    private void applyImportOrder(ImportOrder importOrder , ImportOrderDraftDto dto) {
        WareHouse warehouse = wareHouseRepository.findById(dto.getWareHouseId())
                .orElseThrow(() -> new BusinessException(403, "仓库不存在"));
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new BusinessException(403, "供应商不存在"));

        if(!warehouse.isEnabled()){
            throw new BusinessException(405, "仓库已被禁用！请检查后重试");
        }
        if(!supplier.isEnabled()){
            throw new BusinessException(405, "供应商已被禁用！请检查后重试");
        }

        List<ImportOrderItem> importItems = importOrderItemService.createList(dto.getImportItems(), importOrder);

        // 3. 校验金额是否正确
        BigDecimal f_totalPrice = dto.getTotalAmount();
        BigDecimal b_totalPrice = BigDecimal.ZERO;

        // 计算后端金额
        for(ImportOrderItem importOrderItem : importItems){
            b_totalPrice = b_totalPrice.add(importOrderItem.getTotalPrice());
        }

        if(f_totalPrice.compareTo(b_totalPrice) != 0){
            throw new BusinessException("金额不一致");
        }

        // 保存订单
        importOrder.setSupplierName(supplier.getSupplierName());
        importOrder.setTotalAmount(b_totalPrice);
        importOrder.setImportOrderItems(new HashSet<>(importItems));
        importOrder.setWareHouse(warehouse);
        importOrder.setSupplier(supplier);
        importOrder.setRemark(dto.getRemark());
        importOrderRepository.save(importOrder);

        RequestUser.log();
        log.info("保存进货订单成功，订单编号：{} , 进货总金额：{} , 进货仓库：{}"
                , importOrder.getImportOrderNo(), b_totalPrice, warehouse.getWareHouseName());
    }
}
