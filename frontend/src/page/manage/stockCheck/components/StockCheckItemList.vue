<template>
    <div class="stock-check-items">
        <div class="item-list-header">
            <span class="header-product">商品名称</span>
            <span class="header-code">商品编码</span>
            <span class="header-sku">SKU 数量</span>
            <span class="header-action">操作</span>
        </div>

        <div
            v-for="group in productGroups"
            :key="group.key"
            class="product-card"
        >
            <div
                class="product-header"
                @click="toggleGroup(group.key)"
            >
                <div class="product-name-cell">
                    <span class="product-mark"><IconGraphic name="product"/></span>
                    <strong>{{ group.productName || '-' }}</strong>
                </div>
                <code class="product-code">{{ group.productCode || '-' }}</code>
                <span class="sku-count">{{ group.items.length }} 个</span>
                <div class="product-actions">
                    <span class="expand-icon">{{ isExpanded(group.key) ? '▼' : '▶' }}</span>
                </div>
            </div>

            <Transition name="sku-expand">
                <div
                    v-show="isExpanded(group.key)"
                    class="sku-wrapper"
                >
                    <div class="sku-list-header">
                        <span class="sku-header-name">SKU 名称</span>
                        <span class="sku-header-code">SKU 编码</span>
                        <span class="sku-header-spec">规格</span>
                        <span class="sku-header-system">系统数量</span>
                        <span class="sku-header-actual">实际数量</span>
                        <span class="sku-header-diff">盘点差异</span>
                        <span class="sku-header-action">操作</span>
                    </div>
                    <div
                        v-for="item in group.items"
                        :key="item.skuCode"
                        class="sku-row"
                    >
                        <strong class="sku-name">{{ item.name || item.skuCode }}</strong>
                        <code class="sku-code">{{ item.skuCode }}</code>
                        <span class="sku-spec">{{ formatSpec(item.spec) }}</span>
                        <strong class="system-quantity">{{ item.systemQuantity ?? 0 }}</strong>
                        <input
                            :value="item.actualQuantity"
                            class="actual-quantity"
                            min="0"
                            step="1"
                            type="number"
                            @input="updateQuantity(item, $event)"
                            @keydown.enter.prevent="focusNext($event)"
                        />
                        <strong
                            :class="diffClass(item)"
                            class="difference"
                        >{{ formatDiff(getDifference(item)) }}</strong>
                        <button
                            class="remove-button"
                            type="button"
                            @click.stop="emit('remove', item.skuCode)"
                        >移除</button>
                    </div>
                </div>
            </Transition>
        </div>
    </div>
</template>

<script setup>
import {computed, ref} from 'vue'

const props = defineProps({
    items: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['remove', 'update-quantity'])
const collapsedGroups = ref(new Set())

const productGroups = computed(() => {
    return props.items.reduce((groups, item) => {
        const key = item.productCode || item.productName || item.skuCode
        let group = groups.find(value => value.key === key)
        if (!group) {
            group = {
                key,
                productName: item.productName,
                productCode: item.productCode,
                items: []
            }
            groups.push(group)
        }
        group.items.push(item)
        return groups
    }, [])
})

function toggleGroup(key) {
    const next = new Set(collapsedGroups.value)
    if (next.has(key)) next.delete(key)
    else next.add(key)
    collapsedGroups.value = next
}

function isExpanded(key) {
    return !collapsedGroups.value.has(key)
}

function updateQuantity(item, event) {
    const value = Number(event.target.value)
    const quantity = Number.isInteger(value) && value >= 0 ? value : 0
    emit('update-quantity', item.skuCode, quantity)
}

function focusNext(event) {
    const wrapper = event.currentTarget.closest('.sku-wrapper')
    if (!wrapper) return
    const inputs = [...wrapper.querySelectorAll('.actual-quantity')]
    const currentIndex = inputs.indexOf(event.currentTarget)
    const nextInput = inputs[currentIndex + 1]
    if (nextInput) {
        nextInput.focus()
        nextInput.select()
    }
}

function getDifference(item) {
    return Number(item.actualQuantity || 0) - Number(item.systemQuantity || 0)
}

function diffClass(item) {
    const difference = getDifference(item)
    if (difference > 0) return 'diff-positive'
    if (difference < 0) return 'diff-negative'
    return 'diff-zero'
}

function formatDiff(value) {
    if (value > 0) return `+${value}`
    return String(value)
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
</script>

<style scoped>
.stock-check-items {
    width: 100%;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.item-list-header,
.product-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-right: 20px;
    padding-left: 20px;
}

.item-list-header {
    padding-top: 8px;
    padding-bottom: 8px;
    color: #47615e;
    font-size: 12px;
    font-weight: 700;
    background: #dcebe8;
    border: 1px solid #c5ddd8;
    border-radius: 9px;
    box-shadow: 0 2px 5px rgba(22, 83, 78, 0.1);
}

.header-product,
.product-name-cell {
    width: 220px;
}

.header-code,
.product-code {
    width: calc(100% - 500px);
}

.header-sku,
.sku-count {
    width: 88px;
    text-align: center;
}

.header-action,
.product-actions {
    width: 100px;
    text-align: center;
}

.header-product,
.header-code,
.header-sku,
.header-action,
.product-name-cell,
.product-code,
.sku-count,
.product-actions {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 10px;
    border-right: 1px solid #b8d2cd;
}

.header-action,
.product-actions {
    border-right: none;
}

.product-card {
    overflow: hidden;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.product-header {
    min-height: 58px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
}

.product-header:hover {
    background: #f4fbfa;
}

.product-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
}

.product-name-cell strong,
.product-code {
    overflow: hidden;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-mark {
    flex-shrink: 0;
    font-size: 19px;
}

.product-code {
    color: var(--primary);
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
}

.sku-count {
    color: var(--text-secondary);
    font-size: 13px;
}

.product-actions {
    display: flex;
    align-items: center;
    justify-content: center;
}

.expand-icon {
    width: 16px;
    color: var(--text-muted);
    font-size: 12px;
    text-align: center;
}

.sku-wrapper {
    padding: 10px 14px 12px;
    background: #f8fcfb;
    box-shadow: inset 0 3px 8px rgba(22, 83, 78, 0.06);
}

.sku-expand-enter-active,
.sku-expand-leave-active {
    overflow: hidden;
    transition: max-height 0.24s ease, opacity 0.2s ease;
}

.sku-expand-enter-from,
.sku-expand-leave-to {
    max-height: 0;
    opacity: 0;
}

.sku-expand-enter-to,
.sku-expand-leave-from {
    max-height: 900px;
    opacity: 1;
}

.sku-list-header,
.sku-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding-right: 10px;
    padding-left: 10px;
}

.sku-list-header {
    padding-top: 8px;
    padding-bottom: 8px;
    color: #47615e;
    font-size: 12px;
    font-weight: 700;
    background: #dcebe8;
    border: 1px solid #c5ddd8;
    border-radius: 8px;
}

.sku-row {
    min-height: 52px;
    margin-top: 6px;
    border: 1px solid #e1eeeb;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 2px 6px rgba(22, 83, 78, 0.07);
}

.sku-header-name,
.sku-name {
    width: 140px;
}

.sku-header-code,
.sku-code {
    width: 150px;
}

.sku-header-spec,
.sku-spec {
    width: calc(100% - 570px);
}

.sku-header-system,
.system-quantity,
.sku-header-actual,
.actual-quantity,
.sku-header-diff,
.difference {
    width: 78px;
    text-align: center;
}

.sku-header-action,
.remove-button {
    width: 64px;
    text-align: center;
}

.sku-header-name,
.sku-header-code,
.sku-header-spec,
.sku-header-system,
.sku-header-actual,
.sku-header-diff,
.sku-header-action,
.sku-name,
.sku-code,
.sku-spec,
.system-quantity,
.difference {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 7px;
    border-right: 1px solid #c5ddd8;
}

.sku-header-action,
.remove-button,
.difference {
    border-right: none;
}

.sku-name,
.sku-code,
.sku-spec {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.sku-name {
    font-size: 13px;
}

.sku-code {
    color: var(--primary);
    font-size: 12px;
}

.sku-spec {
    color: var(--text-secondary);
    font-size: 12px;
}

.system-quantity,
.difference {
    font-size: 13px;
}

.actual-quantity {
    height: 32px;
    box-sizing: border-box;
    padding: 0 5px;
    border: 1px solid #dceae7;
    border-radius: 7px;
    background: #fbfefd;
    font-size: 14px;
    font-weight: 700;
}

.actual-quantity:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.remove-button {
    padding: 5px 7px;
    border: 1px solid #fecaca;
    border-radius: 7px;
    color: #dc2626;
    background: #fff;
    font-size: 11px;
    cursor: pointer;
}

.remove-button:hover {
    border-color: #f87171;
    background: #fef2f2;
}

.diff-positive {
    color: #16a34a;
}

.diff-negative {
    color: #dc2626;
}

.diff-zero {
    color: var(--text-secondary);
}

@media (prefers-reduced-motion: reduce) {
    .sku-expand-enter-active,
    .sku-expand-leave-active {
        transition: none;
    }
}

@media (max-width: 900px) {
    .item-list-header {
        display: none;
    }

    .product-header {
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
    .sku-count,
    .product-actions {
        width: auto;
        padding: 0;
        border-right: none;
    }

    .sku-list-header {
        display: none;
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
    .system-quantity,
    .actual-quantity,
    .difference,
    .remove-button {
        width: auto;
        padding: 0;
        border-right: none;
        text-align: left;
    }

    .sku-name {
        width: 100%;
    }

    .actual-quantity {
        width: 84px;
        text-align: center;
    }
}
</style>
