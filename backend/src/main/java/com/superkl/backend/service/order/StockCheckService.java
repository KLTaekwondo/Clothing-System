package com.superkl.backend.service.order;

import com.superkl.backend.common.PageResult;
import com.superkl.backend.common.RequestUser;
import com.superkl.backend.converter.order.StockCheckConverter;
import com.superkl.backend.dto.order.StockCheckDraftDto;
import com.superkl.backend.dto.stock.StockContext;
import com.superkl.backend.entity.basic.WareHouse;
import com.superkl.backend.entity.order.StockCheck;
import com.superkl.backend.entity.order.StockCheckItem;
import com.superkl.backend.enums.*;
import com.superkl.backend.exception.BusinessException;
import com.superkl.backend.info.order.StockCheckInfo;
import com.superkl.backend.info.order.StockCheckItemInfo;
import com.superkl.backend.info.order.StockCheckWithItemsInfo;
import com.superkl.backend.repository.basic.WareHouseRepository;
import com.superkl.backend.repository.order.StockCheckRepository;
import com.superkl.backend.service.stock.WareHouseStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCheckService {
    private final StockCheckRepository stockCheckRepository;
    private final StockCheckItemService stockCheckItemService;
    private final WareHouseRepository wareHouseRepository;
    private final WareHouseStockService wareHouseStockService;

    // 创建库存盘点单(此时仓库应无法出售任何商品)
    @Transactional
    public void create(StockCheckDraftDto stockCheckDraftDto) {
        // 现生成库存盘点单号
        StockCheck stockCheck = StockCheckConverter.toEntity();
        applyStockCheck(stockCheck, stockCheckDraftDto);

        // 日志记录
        RequestUser.log();
        log.info("创建库存盘点单，库存盘点单号：{}, 目标仓库：{}-{} , 盘点数量：{}",
                stockCheck.getStockCheckNo(),
                stockCheck.getTargetWarehouse().getWareHouseCode(),
                stockCheck.getTargetWarehouse().getWareHouseName(),
                stockCheck.getStockCheckItems().size());
    }

    // 提交库存盘点单，自动进入CHECKING模式
    @Transactional
    public void check (Long stockCheckId) {
        StockCheck stockCheck = stockCheckRepository.findById(stockCheckId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存盘点单不存在"));
        if (!stockCheck.isDraft()) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "库存盘点单不处于草稿状态，无法提交");
        }
        stockCheck.setStatus(AuditStatusEnum.CHECKING);
        stockCheckRepository.save(stockCheck);
        // 日志记录
        RequestUser.log();
        log.info("提交库存盘点单，库存盘点单号：{}", stockCheck.getStockCheckNo());

    }

    // 审批通过库存盘点单，自动进入APPROVED模式，并更新库存数量
    @Transactional
    public void approve (Long stockCheckId) {
        StockCheck stockCheck = stockCheckRepository.findById(stockCheckId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存盘点单不存在"));
        if (!stockCheck.isChecking()) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "库存盘点单不处于审查中状态，无法审批通过");
        }
        // 更新库存数量
        Set<StockCheckItem> stockCheckItems = stockCheck.getStockCheckItems();
        StockContext stockContext = StockContext.builder()
                .sourceNo(stockCheck.getStockCheckNo())
                .changeType(StockChangeTypeEnum.STOCK_CHECK)
                .sourceType(StockSourceTypeEnum.STOCK_CHECK)
                .build();
        for (StockCheckItem stockCheckItem : stockCheckItems) {
            wareHouseStockService.checkUpdateStock(stockCheckItem.getStockId(), stockCheckItem, stockContext);
        }

        stockCheck.setStatus(AuditStatusEnum.APPROVED);
        // 释放仓库的CHECKING字段
        stockCheck.getTargetWarehouse().setCheckStatus(CheckEnum.NO_CHECK);
        // 保存库存盘点单
        stockCheckRepository.save(stockCheck);
        // 日志记录
        RequestUser.log();
        log.info("审批通过库存盘点单，库存盘点单号：{}", stockCheck.getStockCheckNo());
    }

    // 拒绝库存盘点单，自动进入REJECTED模式
    @Transactional
    public void reject (Long stockCheckId) {
        StockCheck stockCheck = stockCheckRepository.findById(stockCheckId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存盘点单不存在"));
        if (!stockCheck.isChecking()) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "库存盘点单不处于审查中状态，无法拒绝");
        }
        stockCheck.setStatus(AuditStatusEnum.REJECTED);
        // 释放仓库的CHECKING字段
        stockCheck.getTargetWarehouse().setCheckStatus(CheckEnum.NO_CHECK);
        stockCheckRepository.save(stockCheck);
        // 日志记录
        RequestUser.log();
        log.info("拒绝库存盘点单，库存盘点单号：{}", stockCheck.getStockCheckNo());
    }

    // 更新盘点单草稿
    @Transactional
    public void update(Long stockCheckId, StockCheckDraftDto stockCheckDraftDto) {
        StockCheck stockCheck = stockCheckRepository.findById(stockCheckId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存盘点单不存在"));
        // 短暂释放仓库的CHECKING字段
        stockCheck.getTargetWarehouse().setCheckStatus(CheckEnum.NO_CHECK);
        if (!stockCheck.isDraft()) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "库存盘点单不处于草稿状态，无法更新");
        }
        // 删除老旧的盘点项
        stockCheckItemService.updateDelete(stockCheckId);
        // 更新盘点项
        applyStockCheck(stockCheck, stockCheckDraftDto);
        // 保存库存盘点单
        stockCheckRepository.save(stockCheck);
        // 日志记录
        RequestUser.log();
        log.info("更新库存盘点单，库存盘点单号：{}", stockCheck.getStockCheckNo());
    }

    // 查询库存盘点单详情
    public StockCheckWithItemsInfo search(Long stockCheckId) {
        StockCheck stockCheck = stockCheckRepository.findById(stockCheckId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存盘点单不存在"));
        List<StockCheckItemInfo> items = stockCheckItemService.findByStockCheckId(stockCheckId);
        return StockCheckConverter.toInfoWithItems(stockCheck, items);
    }

    // 查询库存分页
    public PageResult<StockCheckInfo> searchPage(Pageable pageable) {
        Page<StockCheck> page = stockCheckRepository.findPage(pageable);
        return StockCheckConverter.toInfoPage(page);
    }

    // 删除草稿
    @Transactional
    public void delete(Long stockCheckId) {
        StockCheck stockCheck = stockCheckRepository.findById(stockCheckId)
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "库存盘点单不存在"));
        if (!stockCheck.isDraft()) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "库存盘点单不处于草稿状态，无法删除");
        }
        // 先释放仓库的CHECKING字段
        stockCheck.getTargetWarehouse().setCheckStatus(CheckEnum.NO_CHECK);
        stockCheckRepository.delete(stockCheck);
        // 日志记录
        RequestUser.log();
        log.info("删除库存盘点单，库存盘点单号：{}", stockCheck.getStockCheckNo());
    }

    private void applyStockCheck(StockCheck stockCheck, StockCheckDraftDto dto) {
        WareHouse wareHouse = wareHouseRepository.findById(dto.getWareHouseId())
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.RULE_NOT_FOUND, "仓库不存在"));
        // 检查仓库是否启用
        if (!wareHouse.isEnabled()) {
            throw new BusinessException(ErrorCodeEnum.RULE_FORBIDDEN, "仓库已禁用");
        }

        // 暂未添加仓库是否已绑定库存盘点单的检查，如果正在盘点，不能重复盘点
        if (wareHouse.isUnderCheck()) {
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "仓库正在盘点，不能重复盘点");
        }

        // 如果没有正在进行的库存绑定单，则给仓库的CHECKING字段设置为true，目前还未给仓库添加CHECKING字段，需要在仓库实体中添加
        wareHouse.setCheckStatus(CheckEnum.UNDER_CHECK);
        stockCheck.setTargetWarehouse(wareHouse);
        List<StockCheckItem> stockCheckItems = stockCheckItemService.createList(dto.getStockCheckItems(), stockCheck);

        stockCheck.setWareHouseName(wareHouse.getWareHouseName());
        stockCheck.setWareHouseCode(wareHouse.getWareHouseCode());
        stockCheck.setStockCheckItems(new HashSet<>(stockCheckItems));
        stockCheck.setRemark(dto.getRemark());

        stockCheckRepository.save(stockCheck);
    }
}
