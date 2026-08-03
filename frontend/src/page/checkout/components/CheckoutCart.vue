<template>
    <div class="order-item-list">
        <div class="cart-toolbar">
            <div class="cart-title-area">
                <span class="cart-title">本单商品</span>
                <span class="cart-count">{{ items.length }} 个 SKU · {{ totalUnits }} 件</span>
            </div>
            <div class="cart-direction-summary">
                <span class="sale-summary">销售 {{ saleCount }}</span>
                <span class="refund-summary">退货 {{ refundCount }}</span>
            </div>
        </div>
        <div class="cart-scroll-area">
        <div
            v-if="items.length > 0"
            class="cart-header"
        >
            <span class="header-direction">类型</span>
            <span class="header-product">商品信息</span>
            <span class="header-sku">SKU 信息</span>
            <span class="header-special">是否特价</span>
            <span class="header-price">销售单价</span>
            <span class="header-quantity">数量</span>
            <span class="header-discount">折扣</span>
            <span class="header-price">实付单价</span>
            <span class="header-subtotal">小计</span>
            <span class="header-action">操作</span>
        </div>
        <div
            v-for="(item, index) in items"
            :key="`${item.skuCode}-${item.direction}`"
            class="cart-row"
        >
            <div class="cart-row-direction">
                <span
                    :class="item.direction === 'OUT' ? 'direction-refund' : 'direction-sale'"
                    class="direction-badge"
                >{{ item.direction === 'OUT' ? '退货' : '销售' }}</span>
            </div>
            <div class="cart-row-product-info">
                <strong>{{ item.productName || item.skuName || item.skuCode }}</strong>
                <span>{{ item.productCode || '-' }}</span>
            </div>
            <div class="cart-row-sku-info">
                <strong>{{ item.skuName || item.skuCode }}</strong>
                <span>{{ item.skuCode }}</span>
            </div>
            <div class="cart-row-special">
                <span :class="item.special ? 'special-badge' : 'regular-badge'">
                    {{ item.special ? '是' : '否' }}
                </span>
            </div>
            <span class="cart-unit-price">¥{{ item.unitPrice.toFixed(2) }}</span>
            <div class="cart-row-qty">
                <button
                    class="qty-btn"
                    @click="$emit('decrease', index)"
                >−</button>
                <input
                    v-model.number="item.quantity"
                    class="qty-input"
                    min="1"
                    type="number"
                    @change="$emit('normalize-quantity', index)"
                />
                <button
                    class="qty-btn"
                    @click="$emit('increase', index)"
                >+</button>
            </div>
            <div class="cart-row-discount">
                <input
                    v-model.number="item.discount"
                    :disabled="item.special"
                    :title="item.special ? '特价商品不可打折' : '输入商品折扣'"
                    class="discount-input"
                    max="1"
                    min="0.01"
                    step="0.01"
                    type="number"
                    @change="$emit('normalize-discount', index)"
                />
            </div>
            <span class="cart-actual-price">
                ¥{{ (item.unitPrice * getItemDiscount(item)).toFixed(2) }}
            </span>
            <strong class="line-total">
                ¥{{ (item.unitPrice * getItemDiscount(item) * item.quantity).toFixed(2) }}
            </strong>
            <div class="cart-row-action">
                <button
                    class="cart-row-remove"
                    title="移除商品"
                    @click="$emit('remove', index)"
                >&times;</button>
            </div>
        </div>
        <div
            v-if="items.length === 0"
            class="cart-empty"
        >
            <div class="empty-icon"><IconGraphic name="receipt"/></div>
            <div class="empty-text">扫描或搜索商品开始结账</div>
        </div>
        </div>
    </div>
</template>

<script setup>
import {computed} from 'vue'
import IconGraphic from '../../../component/IconGraphic.vue'

const props = defineProps({
    items: {
        type: Array,
        default: () => []
    }
})

const totalUnits = computed(() => props.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0))
const saleCount = computed(() => props.items.filter(item => item.direction !== 'OUT').length)
const refundCount = computed(() => props.items.filter(item => item.direction === 'OUT').length)

function getItemDiscount(item) {
    return item.special ? 1 : item.discount
}

defineEmits([
    'decrease',
    'increase',
    'normalize-quantity',
    'normalize-discount',
    'remove'
])
</script>

<style scoped>
.order-item-list {
    height: calc(94% - 20px);
    min-height: 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.cart-scroll-area {
    width: 100%;
    height: calc(100% - 48px);
    min-height: 0;
    overflow-x: auto;
    overflow-y: auto;
    overscroll-behavior: contain;
    scrollbar-color: #8ecdc3 #e7f3f1;
    scrollbar-width: thin;
}

.cart-scroll-area::-webkit-scrollbar {
    width: 9px;
    height: 9px;
}

.cart-scroll-area::-webkit-scrollbar-track {
    background: #e7f3f1;
    border-radius: 10px;
}

.cart-scroll-area::-webkit-scrollbar-thumb {
    background: linear-gradient(180deg, #8ecdc3, #5fb8aa);
    border: 2px solid #e7f3f1;
    border-radius: 10px;
}

.cart-scroll-area::-webkit-scrollbar-thumb:hover {
    background: linear-gradient(180deg, #5fb8aa, #31988a);
}

.cart-scroll-area::-webkit-scrollbar-corner {
    background: transparent;
}

.cart-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    min-height: 42px;
    padding: 0 4px 2px;
    background: #f5faf9;
}

.cart-title-area,
.cart-direction-summary {
    display: flex;
    align-items: center;
    gap: 10px;
}

.cart-title {
    color: #285f5a;
    font-size: 16px;
    font-weight: 800;
}

.cart-count {
    color: #78908d;
    font-size: 12px;
}

.sale-summary,
.refund-summary {
    padding: 4px 8px;
    border-radius: 7px;
    font-size: 11px;
    font-weight: 700;
}

.sale-summary {
    color: #0f766e;
    background: #ccfbf1;
}

.refund-summary {
    color: #dc2626;
    background: #fee2e2;
}


.cart-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    height: calc(100% - 4px);
    min-height: 260px;
    margin-top: 4px;
    color: #94a3b8;
    background: rgba(255, 255, 255, 0.56);
    border: 1px dashed #bfe4dd;
    border-radius: 14px;
}

.cart-empty::after {
    content: '商品会显示在这里，支持销售与退货混合结算';
    color: #a4b5b2;
    font-size: 12px;
}

.cart-header {
    position: sticky;
    top: 0;
    z-index: 5;
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 1060px;
    padding: 13px 14px;
    border: 1px solid #bfe4dd;
    border-radius: 10px;
    background: #dff3ef;
    color: #285f5a;
    font-size: 14px;
    font-weight: 800;
    box-shadow: 0 3px 10px rgba(13, 148, 136, 0.08);
}

.header-direction {
    width: 60px;
    text-align: center;
}

.header-product {
    width: 190px;
    text-align: center;
}

.header-sku {
    width: 170px;
    text-align: center;
}

.header-product,
.header-sku,
.header-special,
.header-price,
.header-quantity,
.header-discount,
.header-subtotal,
.header-action {
    border-left: 1px solid rgba(40, 95, 90, 0.2);
}

.header-special {
    width: 70px;
    text-align: center;
}

.header-price {
    width: 110px;
    text-align: center;
}

.header-quantity {
    width: 112px;
    text-align: center;
}

.header-discount {
    width: 74px;
    text-align: center;
}

.header-subtotal {
    width: 115px;
    text-align: center;
}

.header-action {
    width: 36px;
    margin-left: auto;
    text-align: center;
}

.cart-row {
    min-width: 1060px;
    display: flex;
    align-items: center;
    gap: 12px;
    min-height: 68px;
    padding: 10px 14px;
    background: #fff;
    border-radius: 10px;
    border: 1px solid #e3efed;
    box-shadow: 0 3px 10px rgba(22, 83, 78, 0.025);
    transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.cart-row:hover {
    border-color: #bfe4dd;
    box-shadow: 0 5px 14px rgba(22, 83, 78, 0.08);
    transform: translateY(-1px);
}

.cart-row-direction {
    width: 60px;
    text-align: center;
}

.cart-row-product-info,
.cart-row-sku-info {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 3px;
    text-align: center;
}

.cart-row-product-info {
    width: 190px;
}

.cart-row-sku-info {
    width: 170px;
}

.cart-row-product-info strong,
.cart-row-sku-info strong,
.cart-row-product-info span,
.cart-row-sku-info span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.cart-row-product-info strong {
    color: #334155;
    font-size: 14px;
}

.cart-row-sku-info strong {
    color: #0f766e;
    font-size: 13px;
}

.cart-row-product-info span,
.cart-row-sku-info span {
    color: #94a3b8;
    font-family: monospace;
    font-size: 11px;
}

.direction-badge {
    display: inline-block;
    margin-bottom: 3px;
    padding: 2px 7px;
    border-radius: 10px;
    font-size: 10px;
    font-weight: 800;
}

.direction-sale {
    background: #ccfbf1;
    color: #0f766e;
}

.direction-refund {
    background: #fee2e2;
    color: #dc2626;
}

.cart-row-product-info,
.cart-row-sku-info,
.cart-row-special,
.cart-unit-price,
.cart-row-qty,
.cart-row-discount,
.cart-actual-price,
.line-total,
.cart-row-action {
    border-left: 1px solid #e3efed;
}

.cart-row-special {
    width: 70px;
    display: flex;
    justify-content: center;
}

.special-badge,
.regular-badge {
    min-width: 38px;
    padding: 4px 8px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 800;
    text-align: center;
}

.special-badge {
    background: #fef3c7;
    color: #b45309;
}

.regular-badge {
    background: #f1f5f9;
    color: #64748b;
}

.cart-unit-price,
.cart-actual-price {
    width: 110px;
    padding-left: 12px;
    color: #475569;
    font-size: 13px;
    font-variant-numeric: tabular-nums;
    text-align: center;
}

.cart-actual-price {
    color: #0f766e;
    font-weight: 700;
}

.cart-row-qty {
    width: 112px;
    padding-left: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
}

.qty-btn {
    width: 28px;
    height: 28px;
    padding: 0;
    border: 1px solid #dceae7;
    border-radius: 6px;
    background: #fbfefd;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #475569;
}

.qty-btn:hover {
    background: #e2f5f2;
    border-color: #14b8a6;
    color: #0d9488;
}

.qty-input {
    width: 48px;
    height: 28px;
    padding: 0;
    border: 1px solid #dceae7;
    border-radius: 6px;
    text-align: center;
    font-size: 14px;
    font-weight: 700;
    background: #fff;
}

.cart-row-discount {
    width: 74px;
    padding-left: 8px;
    display: flex;
    justify-content: center;
}

.discount-input {
    width: 64px;
    height: 30px;
    padding: 0 6px;
    border: 1px solid #dceae7;
    border-radius: 6px;
    background: #fff;
    font-size: 13px;
    font-weight: 700;
    text-align: center;
}

.discount-input:disabled {
    border-color: #fde68a;
    background: #fffbeb;
    color: #b45309;
    cursor: not-allowed;
}

.discount-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.line-total {
    width: 115px;
    padding-left: 12px;
    font-size: 15px;
    font-weight: 800;
    color: #0d9488;
    font-variant-numeric: tabular-nums;
    text-align: center;
}

.cart-row-action {
    width: 36px;
    margin-left: auto;
    padding-left: 8px;
    display: flex;
    justify-content: center;
}

.cart-row-remove {
    width: 36px;
    height: 30px;
    padding: 0;
    border: 1px solid #fecaca;
    border-radius: 7px;
    background: #fff1f2;
    font-size: 20px;
    color: #dc2626;
    cursor: pointer;
    line-height: 1;
}

.cart-row-remove:hover {
    border-color: #ef4444;
    background: #ef4444;
    color: #fff;
    box-shadow: 0 4px 10px rgba(239, 68, 68, 0.22);
}
</style>
