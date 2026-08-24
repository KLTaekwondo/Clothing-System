<template>
    <div class="import-order-items">
        <div class="product-list-header">
            <span class="product-header-name">商品名称</span>
            <span class="product-header-code">商品编码</span>
            <span class="product-header-sku">SKU 数量</span>
            <span class="product-header-action">操作</span>
        </div>

        <div
            v-for="group in groups"
            :key="group.productId"
            class="product-card"
        >
            <div
                class="product-row"
                @click="toggleGroup(group.productId)"
            >
                <div class="product-name-cell">
                    <span class="product-mark"><IconGraphic name="product"/></span>
                    <strong class="product-name">{{ group.productName || '-' }}</strong>
                </div>
                <code class="product-code">{{ group.productCode || '-' }}</code>
                <span class="product-sku-count">{{ group.skus.length }} 个</span>
                <div class="product-actions">
                    <span class="expand-icon">{{ isExpanded(group.productId) ? '▼' : '▶' }}</span>
                </div>
            </div>

            <Transition name="sku-expand">
                <div
                    v-show="isExpanded(group.productId)"
                    class="sku-wrapper"
                >
                    <div class="sku-list-header">
                        <span class="sku-header-name">SKU 名称</span>
                        <span class="sku-header-code">SKU 编码</span>
                        <span class="sku-header-spec">规格</span>
                        <span class="sku-header-price">采购单价</span>
                        <span class="sku-header-quantity">数量</span>
                        <span class="sku-header-subtotal">小计</span>
                        <span class="sku-header-action">操作</span>
                    </div>
                    <div
                        v-for="item in group.skus"
                        :key="item.skuId"
                        class="sku-row"
                    >
                        <strong class="sku-name">{{ item.skuName || item.skuCode || '-' }}</strong>
                        <code class="sku-code">{{ item.skuCode || '-' }}</code>
                        <span class="sku-spec">{{ formatSpec(item.spec) }}</span>
                        <span class="sku-price">¥{{ formatMoney(item.importPrice) }}</span>
                        <input
                            v-model.number="item.quantity"
                            class="quantity-input"
                            min="1"
                            step="1"
                            type="number"
                            @input="updateQuantity(item, $event)"
                            @keydown.enter.prevent="focusNextQuantity($event)"
                        />
                        <strong class="sku-subtotal">¥{{ calculateSubtotal(item) }}</strong>
                        <button
                            class="remove-button"
                            type="button"
                            @click.stop="emit('remove', item.skuId)"
                        >移除</button>
                    </div>
                </div>
            </Transition>
        </div>
    </div>
</template>

<script setup>
import {ref} from 'vue'

const props = defineProps({
    groups: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['remove', 'update-quantity'])
const collapsedGroups = ref(new Set())

function toggleGroup(productId) {
    const next = new Set(collapsedGroups.value)
    if (next.has(productId)) next.delete(productId)
    else next.add(productId)
    collapsedGroups.value = next
}

function isExpanded(productId) {
    return !collapsedGroups.value.has(productId)
}

function updateQuantity(item, event) {
    const value = Number(event.target.value)
    const quantity = Number.isInteger(value) && value >= 1 ? value : 1
    if (Number(event.target.value) !== quantity) event.target.value = quantity
    item.quantity = quantity
    emit('update-quantity', item.skuId, quantity)
}

function focusNextQuantity(event) {
    const wrapper = event.currentTarget.closest('.sku-wrapper')
    if (!wrapper) return
    const inputs = [...wrapper.querySelectorAll('.quantity-input')]
    const currentIndex = inputs.indexOf(event.currentTarget)
    const nextInput = inputs[currentIndex + 1]
    if (nextInput) {
        nextInput.focus()
        nextInput.select()
    }
}

function formatSpec(spec) {
    if (!spec) return '无规格'
    let parsed = spec
    if (typeof spec === 'string') {
        try {
            parsed = JSON.parse(spec)
        } catch {
            return spec
        }
    }
    const entries = Object.entries(parsed)
    if (!entries.length) return '无规格'
    return entries.map(([key, value]) => `${key}：${value}`).join(' / ')
}

function formatMoney(value) {
    return Number(value || 0).toFixed(2)
}

function calculateSubtotal(item) {
    return (Number(item.importPrice || 0) * Number(item.quantity || 0)).toFixed(2)
}
</script>

<style scoped>
.import-order-items {
    width: 100%;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.product-list-header,
.product-row {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-right: 20px;
    padding-left: 20px;
}

.product-header-name,
.product-name-cell {
    width: 220px;
}

.product-header-code,
.product-code {
    width: calc(100% - 500px);
}

.product-header-sku,
.product-sku-count {
    width: 88px;
    text-align: center;
}

.product-header-action,
.product-actions {
    width: 100px;
    text-align: center;
}

.product-header-name,
.product-header-code,
.product-header-sku,
.product-header-action,
.product-name-cell,
.product-code,
.product-sku-count,
.product-actions {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 10px;
    border-right: 1px solid #b8d2cd;
}

.product-header-action,
.product-actions {
    border-right: none;
}

.product-row {
    min-height: 58px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
}

.product-row:hover {
    background: var(--bg-hover);
}

.product-name,
.product-code {
    overflow: hidden;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.sku-list-header,
.sku-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding-right: 10px;
    padding-left: 10px;
}

.sku-header-name,
.sku-name {
    width: 130px;
}

.sku-header-code,
.sku-code {
    width: 145px;
}

.sku-header-spec,
.sku-spec {
    width: calc(100% - 602px);
}

.sku-header-price,
.sku-price,
.sku-header-quantity,
.quantity-input,
.sku-header-subtotal,
.sku-subtotal {
    width: 78px;
    text-align: center;
}

.sku-header-action,
.remove-button {
    width: 63px;
    text-align: center;
}

.sku-header-name,
.sku-header-code,
.sku-header-spec,
.sku-header-price,
.sku-header-quantity,
.sku-header-subtotal,
.sku-header-action,
.sku-name,
.sku-code,
.sku-spec,
.sku-price,
.sku-subtotal {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 7px;
    border-right: 1px solid #c5ddd8;
}

.sku-header-action,
.remove-button,
.sku-subtotal {
    border-right: none;
}

.sku-price {
    color: var(--text-secondary);
    font-size: 13px;
}

.sku-subtotal {
    color: var(--primary);
    font-size: 13px;
}

@media (prefers-reduced-motion: reduce) {
    .sku-expand-enter-active,
    .sku-expand-leave-active {
        transition: none;
    }
}

@media (max-width: 900px) {
    .product-list-header,
    .sku-list-header {
        display: none;
    }

    .product-row {
        align-items: flex-start;
        flex-wrap: wrap;
        padding-top: 12px;
        padding-bottom: 12px;
    }

    .product-name-cell {
        width: 100%;
        padding: 0;
        border-right: none;
    }

    .product-code,
    .product-sku-count,
    .product-actions {
        width: auto;
        padding: 0;
        border-right: none;
    }

    .sku-row {
        align-items: flex-start;
        flex-wrap: wrap;
        gap: 8px;
        padding-top: 10px;
        padding-bottom: 10px;
    }

    .sku-name,
    .sku-code,
    .sku-spec,
    .sku-price,
    .quantity-input,
    .sku-subtotal,
    .remove-button {
        width: auto;
        padding: 0;
        border-right: none;
        text-align: left;
    }

    .sku-name {
        width: 100%;
    }

    .quantity-input {
        width: 84px;
        padding: 0 5px;
        text-align: center;
    }
}</style>
