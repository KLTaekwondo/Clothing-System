<template>
    <div class="item-card">
        <div class="item-header">
            <div class="item-title">
                <strong>{{ item.productName || '-' }}</strong>
                <span>{{ item.productCode || '-' }}</span>
            </div>
            <button
                class="btn-danger"
                @click="$emit('remove', item.skuCode)"
            >移除</button>
        </div>
        <div class="item-content">
            <div class="sku-info">
                <strong>{{ item.name || item.skuCode }}</strong>
                <code>{{ item.skuCode }}</code>
                <span>{{ formatSpec(item.spec) }}</span>
            </div>
            <div class="quantity-field">
                <label>系统数量</label>
                <strong>{{ item.systemQuantity }}</strong>
            </div>
            <div class="quantity-field">
                <label>实际数量</label>
                <input
                    :value="item.actualQuantity"
                    min="0"
                    step="1"
                    type="number"
                    @input="updateQuantity"
                />
            </div>
            <div class="quantity-field">
                <label>盘点差异</label>
                <strong :class="diffClass">{{ formatDiff(diffQuantity) }}</strong>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
    item: {
        type: Object,
        required: true
    }
})

const emit = defineEmits(['remove', 'update-quantity'])

const diffQuantity = computed(() => {
    return Number(props.item.actualQuantity || 0) - Number(props.item.systemQuantity || 0)
})

const diffClass = computed(() => {
    if (diffQuantity.value > 0) return 'diff-positive'
    if (diffQuantity.value < 0) return 'diff-negative'
    return 'diff-zero'
})

function updateQuantity(event) {
    const value = Number(event.target.value)
    const quantity = Number.isInteger(value) && value >= 0 ? value : 0
    emit('update-quantity', props.item.skuCode, quantity)
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
.item-card {
    padding: 18px 20px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 14px;
    box-shadow: 0 7px 22px rgba(22, 83, 78, 0.05);
}

.item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding-bottom: 13px;
    border-bottom: 1px solid var(--border-light);
}

.item-title {
    display: flex;
    align-items: center;
    gap: 10px;
}

.item-title span {
    color: var(--text-muted);
    font-size: 12px;
}

.item-header .btn-danger {
    padding: 5px 11px;
    font-size: 12px;
}

.item-content {
    display: flex;
    align-items: center;
    gap: 22px;
    padding-top: 15px;
    flex-wrap: wrap;
}

.sku-info {
    width: 34%;
    min-width: 230px;
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.sku-info code {
    color: var(--primary);
}

.sku-info span {
    color: var(--text-muted);
    font-size: 12px;
}

.quantity-field {
    width: 130px;
    display: flex;
    flex-direction: column;
    gap: 7px;
}

.quantity-field label {
    color: var(--text-muted);
    font-size: 12px;
}

.quantity-field strong {
    font-size: 17px;
}

.quantity-field input {
    width: 120px;
    height: 38px;
    padding: 0 10px;
    border: 1px solid #dceae7;
    border-radius: 9px;
    background: #fbfefd;
    font-weight: 700;
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

@media (max-width: 900px) {
    .sku-info {
        width: 100%;
    }
}
</style>
