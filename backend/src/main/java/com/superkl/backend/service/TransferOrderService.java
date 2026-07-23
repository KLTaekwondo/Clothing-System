package com.superkl.backend.service;

import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.TransferOrderConverter;
import com.superkl.backend.dto.TransferOrderDraftDto;
import com.superkl.backend.entity.TransferOrder;
import com.superkl.backend.entity.TransferOrderItem;
import com.superkl.backend.entity.WareHouse;
import com.superkl.backend.enums.AuditStatusEnum;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.TransferOrderInfo;
import com.superkl.backend.info.TransferOrderItemInfo;
import com.superkl.backend.info.TransferOrderWithItemsInfo;
import com.superkl.backend.repository.TransferOrderRepository;
import com.superkl.backend.repository.WareHouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferOrderService {
    private final TransferOrderRepository transferOrderRepository;
    private final WareHouseRepository wareHouseRepository;
    private final TransferOrderItemService transferOrderItemService;
    private final WareHouseStockService wareHouseStockService;

    // 保存转移订单
    @Transactional
    public void create(TransferOrderDraftDto dto) {
        // 先生成订单号，然后填充订单信息
        TransferOrder transferOrder = TransferOrderConverter.toEntity();
        applyTransferOrder(transferOrder,dto);
    }

    // 更新转移订单
    @Transactional
    public void update(Long transferOrderId, TransferOrderDraftDto dto) {
        TransferOrder transferOrder = transferOrderRepository.findById(transferOrderId)
                .orElseThrow(() -> new BusinessException("转移订单不存在"));
        if(!transferOrder.isDraft()){
            throw new BusinessException("转移订单不是草稿状态，不能更新");
        }
        // 删除老的订单项
        transferOrderItemService.updateDelete(transferOrderId);

        // 填充新的订单
        applyTransferOrder(transferOrder,dto);
    }

    // 保存并提交了转移订单，自动设置为CHECKING状态
    @Transactional
    public void check(Long transferOrderId) {
        TransferOrder transferOrder = transferOrderRepository.findById(transferOrderId)
                .orElseThrow(() -> new BusinessException("转移订单不存在"));
        if(!transferOrder.isDraft()){
            throw new BusinessException("转移订单不是草稿状态，不能提交");
        }

        // 设置为审核中状态
        transferOrder.setStatus(AuditStatusEnum.CHECKING);
        transferOrderRepository.save(transferOrder);
    }

    // 校验通过了转移订单，自动设置为通过状态
    @Transactional
    public void approve(Long transferOrderId) {
        TransferOrder transferOrder = transferOrderRepository.findById(transferOrderId)
                .orElseThrow(() -> new BusinessException("转移订单不存在"));
        if(!transferOrder.isChecking()){
            throw new BusinessException("转移订单不是校验中状态，不能通过");
        }
        // 处理库存
        Long sourceWareHouseId = transferOrder.getSourceWareHouse().getWareHouseId();
        Long targetWareHouseId = transferOrder.getTargetWareHouse().getWareHouseId();
        List<TransferOrderItem> items = transferOrder.getTransferOrderItems().stream().toList();
        RequestUser.log();
        for(TransferOrderItem item : items){
            // 先扣除源仓库库存
            wareHouseStockService.decreaseStock(sourceWareHouseId, item.getSkuId(), item.getQuantity());
            // 再增加目标仓库库存
            wareHouseStockService.increaseStock(targetWareHouseId, item.getSkuId(), item.getQuantity());
            log.info("调货：SKU={}，从仓库{}到仓库{}，数量={}", item.getSkuId(), sourceWareHouseId, targetWareHouseId, item.getQuantity());
        }
        // 设置为通过状态
        transferOrder.setStatus(AuditStatusEnum.APPROVED);
        transferOrderRepository.save(transferOrder);

    }

    // 校验拒绝了转移订单，自动设置为拒绝状态
    @Transactional
    public void reject(Long transferOrderId) {
        TransferOrder transferOrder = transferOrderRepository.findById(transferOrderId)
                .orElseThrow(() -> new BusinessException("转移订单不存在"));
        if(!transferOrder.isChecking()){
            throw new BusinessException("转移订单不是校验中状态，不能拒绝");
        }
        // 设置为拒绝状态
        transferOrder.setStatus(AuditStatusEnum.REJECTED);
        transferOrderRepository.save(transferOrder);
    }

    // 删除转移订单(仅在草稿状态下删除)
    @Transactional
    public void delete(Long transferOrderId) {
        TransferOrder transferOrder = transferOrderRepository.findById(transferOrderId)
                .orElseThrow(() -> new BusinessException("转移订单不存在"));
        if(!transferOrder.isDraft()){
            throw new BusinessException("转移订单不是草稿状态，不能删除");
        }
        transferOrderRepository.delete(transferOrder);
    }

    // 查找特定转移订单
    @Transactional(readOnly = true)
    public TransferOrderWithItemsInfo search(Long transferOrderId) {
        TransferOrder transferOrder = transferOrderRepository.findById(transferOrderId)
                .orElseThrow(() -> new BusinessException("转移订单不存在"));

        // 查出所有订单项
        List<TransferOrderItemInfo> items = transferOrderItemService.findByTransferOrderId(transferOrderId);
        // 返回订单详情
        return TransferOrderConverter.toInfoWithItems(transferOrder, items);
    }


    // 查找所有转移订单
    @Transactional(readOnly = true)
    public List<TransferOrderInfo> searchList(){
        return TransferOrderConverter.toInfoList(transferOrderRepository.findAll());
    }


    // 填充订单
    private void applyTransferOrder(TransferOrder transferOrder , TransferOrderDraftDto dto) {
        Long sourceWareHouseId = dto.getSourceWareHouseId();
        Long targetWareHouseId = dto.getTargetWareHouseId();

        WareHouse sourceWareHouse = wareHouseRepository.findById(sourceWareHouseId)
                .orElseThrow(() -> new BusinessException("源仓库不存在"));

        WareHouse targetWareHouse = wareHouseRepository.findById(targetWareHouseId)
                .orElseThrow(() -> new BusinessException("目标仓库不存在"));

        if(sourceWareHouseId.equals(targetWareHouseId)){
            throw new BusinessException("源仓库和目标仓库不能相同");
        }

        if(!sourceWareHouse.isEnabled() || !targetWareHouse.isEnabled()){
            throw new BusinessException("源仓库或目标仓库已被禁用，请检查后再试");
        }

        // 检查是否为空列表
        boolean isEmpty = dto.getTransferOrderItems().isEmpty();
        if(isEmpty){
            throw new BusinessException("转移订单项列表不能为空");
        }

        List<TransferOrderItem> transferItems = transferOrderItemService.createList(dto.getTransferOrderItems(), transferOrder);

        // 校验金额
        BigDecimal f_totalPrice = dto.getTotalPrice();
        BigDecimal b_totalPrice = BigDecimal.ZERO;

        for(TransferOrderItem item : transferItems){
            b_totalPrice = b_totalPrice.add(item.getItemTotalPrice());
        }

        if(f_totalPrice.compareTo(b_totalPrice) != 0){
            throw new BusinessException("转移订单金额与商品总价不一致");
        }

        // 保存订单
        transferOrder.setTotalPrice(b_totalPrice);
        transferOrder.setTransferOrderItems(new HashSet<>(transferItems));
        transferOrder.setSourceWareHouse(sourceWareHouse);
        transferOrder.setTargetWareHouse(targetWareHouse);
        transferOrder.setSourceWareHouseName(sourceWareHouse.getWareHouseName());
        transferOrder.setTargetWareHouseName(targetWareHouse.getWareHouseName());
        transferOrder.setRemark(dto.getRemark());
        transferOrderRepository.save(transferOrder);
    }
}
