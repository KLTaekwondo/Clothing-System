<template>
    <div
        v-if="visible"
        class="receipt-overlay"
        @click.self="handleOverlayClick"
    >
        <div class="receipt-controls">
            <button
                class="controls-btn controls-print"
                @click="printReceipt"
            >🖨️ 打印小票</button>
            <button
                class="controls-btn controls-close"
                @click="$emit('close')"
            >✕ 关闭</button>
        </div>

        <div
            id="receipt-content"
            class="receipt-paper"
        >
            <!-- 店头 -->
            <div class="receipt-header">
                <div class="shop-name">{{ shopName }}</div>
                <div
                    v-if="shopSlogan"
                    class="shop-slogan"
                >{{ shopSlogan }}</div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 订单信息 -->
            <div class="order-meta">
                <div class="meta-row">
                    <span class="meta-label">订单编号</span>
                    <span class="meta-value code">{{ orderNo }}</span>
                </div>
                <div class="meta-row">
                    <span class="meta-label">收银员</span>
                    <span class="meta-value">{{ employeeName }}</span>
                </div>
                <div class="meta-row">
                    <span class="meta-label">门店</span>
                    <span class="meta-value">{{ warehouseName }}</span>
                </div>
                <div class="meta-row">
                    <span class="meta-label">时间</span>
                    <span class="meta-value">{{ printTime }}</span>
                </div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 商品明细表头 -->
            <div class="items-header">
                <span class="col-name">商品</span>
                <span class="col-price">单价</span>
                <span class="col-qty">数量</span>
                <span class="col-discount">折扣</span>
                <span class="col-total">小计</span>
            </div>

            <div class="receipt-divider-light">─────────────────────────</div>

            <!-- 销售商品 -->
            <div
                v-for="(item, index) in saleItems"
                :key="'sale-' + index"
                class="item-row"
            >
                <div class="item-name-row">
                    <span class="item-direction sale">售</span>
                    <span class="item-name">{{ item.productName || item.skuName }}</span>
                </div>
                <div class="item-numbers">
                    <span class="col-name"></span>
                    <span class="col-price">¥{{ formatPrice(item.unitPrice) }}</span>
                    <span class="col-qty">{{ item.quantity }}</span>
                    <span class="col-discount">{{ item.special ? '特价' : (item.discount * 100).toFixed(0) + '%' }}</span>
                    <span class="col-total">¥{{ formatPrice(item.unitPrice * item.quantity * getItemDiscount(item)) }}</span>
                </div>
                <div
                    v-if="item.skuCode"
                    class="item-sku-code"
                >货号: {{ item.skuCode }}</div>
            </div>

            <!-- 退货商品 -->
            <div
                v-for="(item, index) in refundItems"
                :key="'refund-' + index"
                class="item-row item-refund"
            >
                <div class="item-name-row">
                    <span class="item-direction refund">退</span>
                    <span class="item-name">{{ item.productName || item.skuName }}</span>
                </div>
                <div class="item-numbers">
                    <span class="col-name"></span>
                    <span class="col-price">¥{{ formatPrice(item.unitPrice) }}</span>
                    <span class="col-qty">-{{ item.quantity }}</span>
                    <span class="col-discount">{{ item.special ? '特价' : (item.discount * 100).toFixed(0) + '%' }}</span>
                    <span class="col-total">-¥{{ formatPrice(item.unitPrice * item.quantity * getItemDiscount(item)) }}</span>
                </div>
                <div
                    v-if="item.skuCode"
                    class="item-sku-code"
                >货号: {{ item.skuCode }}</div>
            </div>
<!-- 数量汇总 -->
            <div class="receipt-divider-light">─────────────────────────</div>
            <div class="qty-summary">
                销售 {{ saleQty }} 件
                <template v-if="refundQty > 0">
                    ，退货 {{ refundQty }} 件
                </template>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 金额汇总 -->
            <div class="amount-section">
                <div class="amount-row">
                    <span>总金额</span>
                    <span>¥{{ formatPrice(totalAmount) }}</span>
                </div>
                <div class="amount-row">
                    <span>优惠</span>
                    <span>-¥{{ formatPrice(discountAmount) }}</span>
                </div>
                <div class="amount-row amount-row-final">
                    <span>实付金额</span>
                    <span class="final-price">¥{{ formatPrice(actualAmount) }}</span>
                </div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 支付和会员 -->
            <div class="payment-row">
                <span>支付方式</span>
                <span>{{ payMethodLabel }}</span>
            </div>
            <div
                v-if="memberPhone"
                class="payment-row"
            >
                <span>会员</span>
                <span>{{ memberPhone }}</span>
            </div>

            <div
                v-if="remark"
                class="remark-row"
            >
                <div class="remark-label">备注</div>
                <div>{{ remark }}</div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 结尾 -->
            <div class="receipt-footer">
                <div class="footer-thanks">感谢您的光临！</div>
                <div class="footer-note">退换货请保留此小票</div>
            </div>
        </div>
    </div>
</template>
<script setup>
import {computed, onMounted, ref} from 'vue'
import {PAY_METHOD_LABELS} from '../../constants/payMethod.js'

const props = defineProps({
    visible: Boolean,
    orderNo: {type: String, default: ''},
    items: {type: Array, default: () => []},
    totalAmount: {type: Number, default: 0},
    actualAmount: {type: Number, default: 0},
    discountAmount: {type: Number, default: 0},
    employeeName: {type: String, default: ''},
    warehouseName: {type: String, default: ''},
    payMethod: {type: String, default: ''},
    memberPhone: {type: String, default: ''},
    remark: {type: String, default: ''},
    shopName: {type: String, default: ''},
    shopSlogan: {type: String, default: ''},
})

defineEmits(['close', 'print-done'])

const payMethodLabels = PAY_METHOD_LABELS
const printTime = ref('')

onMounted(() => {
    const now = new Date()
    const pad = n => String(n).padStart(2, '0')
    printTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
})

const payMethodLabel = computed(() => {
    return payMethodLabels[props.payMethod] || props.payMethod || '-'
})

const saleItems = computed(() => {
    return props.items.filter(item => item.direction !== 'OUT')
})

const refundItems = computed(() => {
    return props.items.filter(item => item.direction === 'OUT')
})

const saleQty = computed(() => {
    return saleItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const refundQty = computed(() => {
    return refundItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

function formatPrice(val) {
    return Number(val || 0).toFixed(2)
}

function getItemDiscount(item) {
    return item.special ? 1 : Number(item.discount || 1)
}

function printReceipt() {
    setReceiptPageHeight()
    window.print()
}

defineExpose({ print: printReceipt })

// 57mm 热敏纸：屏幕小票宽 300px ≈ 79.4mm，打印宽度收窄到 57mm 后
// 同一内容换行更多、实际高度按宽度比例放大，另加缓冲避免内容翻到第二页
function setReceiptPageHeight() {
    const el = document.getElementById('receipt-content')
    if (!el) return
    const heightPx = el.offsetHeight
    const screenWidthMm = (300 * 25.4) / 96
    const heightMm = Math.ceil((heightPx * 25.4) / 96 * (screenWidthMm / 57)) + 15
    let tag = document.getElementById('receipt-page-style')
    if (!tag) {
        tag = document.createElement('style')
        tag.id = 'receipt-page-style'
        document.head.appendChild(tag)
    }
    tag.textContent = `@page { size: 57mm ${Math.max(heightMm, 40)}mm; margin: 0; }`
}

function handleOverlayClick() {
    // 防止误点击关闭
}
</script>

<style scoped>
/* ============ 屏幕预览样式 ============ */
.receipt-overlay {
    position: fixed;
    inset: 0;
    z-index: 2000;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 24px;
    background: rgba(0, 0, 0, 0.45);
    overflow-y: auto;
}

.receipt-controls {
    position: sticky;
    top: 0;
    z-index: 10;
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
    padding: 10px 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.controls-btn {
    min-width: 120px;
    height: 42px;
    padding: 0 18px;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s;
}

.controls-print {
    background: #0d9488;
    color: #fff;
}

.controls-print:hover {
    background: #0f766e;
}

.controls-close {
    background: #f1f5f9;
    color: #475569;
}

.controls-close:hover {
    background: #e2e8f0;
}

/* ============ 小票纸张 ============ */
.receipt-paper {
    width: 300px;
    padding: 16px 18px;
    margin-bottom: 40px;
    background: #fff;
    font-family: 'Courier New', Courier, monospace;
    font-size: 12px;
    line-height: 1.5;
    color: #1e293b;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
    border-radius: 4px;
}

.receipt-header {
    text-align: center;
    margin-bottom: 8px;
}

.shop-name {
    font-size: 18px;
    font-weight: 800;
    letter-spacing: 2px;
    color: #0f172a;
}

.shop-slogan {
    font-size: 11px;
    color: #64748b;
    margin-top: 2px;
}

.receipt-divider {
    text-align: center;
    font-size: 10px;
    color: #94a3b8;
    letter-spacing: 1px;
    margin: 6px 0;
}

.receipt-divider-light {
    text-align: center;
    font-size: 10px;
    color: #cbd5e1;
    letter-spacing: 1px;
    margin: 4px 0;
}

/* 订单信息 */
.order-meta {
    margin: 4px 0;
}

.meta-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 22px;
}

.meta-label {
    color: #64748b;
}

.meta-value {
    font-weight: 600;
    text-align: right;
}

.meta-value.code {
    font-size: 11px;
    max-width: 170px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 商品表头 */
.items-header {
    display: flex;
    align-items: center;
    font-weight: 700;
    font-size: 11px;
    color: #475569;
    padding: 2px 0;
}

.col-name {
    flex: 1;
    text-align: left;
}

.col-price {
    width: 52px;
    text-align: right;
}

.col-qty {
    width: 36px;
    text-align: center;
}

.col-discount {
    width: 42px;
    text-align: center;
}

.col-total {
    width: 60px;
    text-align: right;
}

/* 商品行 */
.item-row {
    padding: 4px 0;
    border-bottom: 1px dashed #e2e8f0;
}

.item-row.item-refund {
    background: #fef2f2;
    margin: 0 -18px;
    padding: 4px 18px;
}

.item-name-row {
    display: flex;
    align-items: center;
    gap: 4px;
}

.item-direction {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    border-radius: 3px;
    font-size: 10px;
    font-weight: 800;
    flex-shrink: 0;
}

.item-direction.sale {
    background: #dcfce7;
    color: #16a34a;
}

.item-direction.refund {
    background: #fee2e2;
    color: #dc2626;
}

.item-name {
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
}

.item-numbers {
    display: flex;
    align-items: center;
    margin-top: 2px;
    font-size: 11px;
}

.item-numbers .col-price,
.item-numbers .col-qty,
.item-numbers .col-discount,
.item-numbers .col-total {
    font-weight: 600;
}

.item-sku-code {
    font-size: 10px;
    color: #94a3b8;
    padding-left: 22px;
}

/* 数量汇总 */
.qty-summary {
    text-align: center;
    color: #64748b;
    font-size: 11px;
    padding: 2px 0;
}

/* 金额 */
.amount-section {
    padding: 4px 0;
}

.amount-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 24px;
    font-size: 13px;
}

.amount-row-final {
    border-top: 2px solid #1e293b;
    margin-top: 4px;
    padding-top: 6px;
    font-size: 15px;
    font-weight: 800;
}

.final-price {
    color: #d97706;
    font-size: 18px;
}

/* 支付 */
.payment-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 22px;
    font-size: 12px;
}

.payment-row span:last-child {
    font-weight: 700;
}

/* 备注 */
.remark-row {
    font-size: 11px;
    color: #64748b;
    margin-top: 4px;
}

.remark-label {
    font-weight: 700;
    color: #475569;
}

/* 结尾 */
.receipt-footer {
    text-align: center;
    padding: 8px 0 4px;
}

.footer-thanks {
    font-size: 14px;
    font-weight: 800;
    color: #0f766e;
    letter-spacing: 1px;
}

.footer-note {
    font-size: 10px;
    color: #94a3b8;
    margin-top: 4px;
}
</style>

<style>
/* ============ 打印样式（全局生效） ============ */
@media print {
    html, body {
        height: auto !important;
        margin: 0 !important;
        padding: 0 !important;
    }
    body * {
        visibility: hidden !important;
    }
    .receipt-overlay {
        position: static !important;
        background: none !important;
        padding: 0 !important;
        overflow: visible !important;
        visibility: visible !important;
        height: auto !important;
        min-height: 0 !important;
    }
    .receipt-controls {
        display: none !important;
    }
    .receipt-paper,
    .receipt-paper * {
        visibility: visible !important;
    }
    .receipt-paper {
        position: absolute !important;
        left: 0 !important;
        top: 0 !important;
        width: 57mm !important;
        max-width: none !important;
        padding: 0 !important;
        margin: 0 !important;
        box-shadow: none !important;
        border-radius: 0 !important;
        font-size: 11px !important;
        line-height: 1.4 !important;
        page-break-inside: avoid !important;
        break-inside: avoid !important;
    }
    /* 57mm 窄纸下列宽压缩，避免金额列溢出 */
    .receipt-paper .col-price {
        width: 40px !important;
    }
    .receipt-paper .col-qty {
        width: 26px !important;
    }
    .receipt-paper .col-discount {
        width: 30px !important;
    }
    .receipt-paper .col-total {
        width: 46px !important;
    }
    .receipt-paper .shop-name {
        font-size: 15px !important;
    }
    .receipt-paper .amount-row {
        font-size: 12px !important;
    }
    .receipt-paper .amount-row-final {
        font-size: 13px !important;
    }
    .receipt-paper .final-price {
        font-size: 15px !important;
    }
}

/* 默认 @page 回退（动态 JS 会覆盖此值） */
@page {
    size: 57mm 120mm;
    margin: 0;
}
</style>