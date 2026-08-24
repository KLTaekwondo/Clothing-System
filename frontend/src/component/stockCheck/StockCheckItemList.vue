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

.product-header {
    min-height: 58px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
}

.product-header:hover {
    background: var(--bg-hover);
}

.product-name-cell strong,
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

.system-quantity,
.difference {
    font-size: 13px;
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
}</style>
