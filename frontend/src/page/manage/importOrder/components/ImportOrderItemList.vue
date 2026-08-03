<template>
    <div class="items-list">
        <div
            v-for="group in groups"
            :key="group.productId"
            class="product-group-card"
        >
            <div class="product-header">
                <span class="product-mark"><IconGraphic name="product"/></span>
                <div>
                    <strong class="product-name">{{ group.productName }}</strong>
                    <span class="product-code">{{ group.productCode }}</span>
                </div>
            </div>
            <table class="sku-table">
                <thead>
                <tr>
                    <th>SKU</th>
                    <th>规格</th>
                    <th>采购单价</th>
                    <th>数量</th>
                    <th>小计</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="item in group.skus"
                    :key="item.skuId"
                >
                    <td>{{ item.skuName || '-' }}</td>
                    <td>{{ formatSpec(item.spec) }}</td>
                    <td>¥{{ item.importPrice ?? '0.00' }}</td>
                    <td class="qty-cell">
                        <input
                            v-model.number="item.quantity"
                            class="qty-input"
                            min="1"
                            type="number"
                        />
                    </td>
                    <td>
                        <strong>¥{{ calculateSubtotal(item) }}</strong>
                    </td>
                    <td>
                        <button
                            class="remove-button"
                            @click="$emit('remove', item.skuId)"
                        >移除</button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
defineProps({
    groups: {
        type: Array,
        default: () => []
    }
})

defineEmits(['remove'])

function formatSpec(spec) {
    if (!spec || Object.keys(spec).length === 0) return '-'
    return Object.entries(spec).map(([key, value]) => `${key}:${value}`).join(' / ')
}

function calculateSubtotal(item) {
    return (Number(item.importPrice || 0) * Number(item.quantity || 0)).toFixed(2)
}
</script>

<style scoped>
.items-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.product-group-card {
    overflow: hidden;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    background: #fff;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.product-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    border-bottom: 1px solid var(--border-light);
    background: #fafafa;
}

.product-mark {
    font-size: 22px;
}

.product-name {
    font-size: 15px;
}

.product-code {
    display: block;
    margin-top: 2px;
    font-size: 12px;
    color: var(--text-secondary);
}

.sku-table {
    width: 100%;
    border-collapse: collapse;
    font-size: var(--font-base);
}

.sku-table th {
    padding: 10px 14px;
    text-align: left;
    color: var(--text-secondary);
    background: #fbfdfd;
    border-bottom: 1px solid var(--border);
    font-size: var(--font-sm);
}

.sku-table td {
    height: 48px;
    padding: 11px 14px;
    border-bottom: 1px solid var(--border-light);
}

.qty-cell {
    padding: 3px 14px;
}

.qty-input {
    width: 72px;
    height: 36px;
    padding: 0;
    border: 1px solid #dceae7;
    border-radius: 8px;
    text-align: center;
    font-weight: 700;
    font-size: 15px;
    background: #fbfefd;
}

.qty-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.remove-button {
    height: 30px;
    padding: 4px 12px;
    border-radius: 9px;
    background: #ef4444;
    color: #fff;
    font-size: var(--font-sm);
    font-weight: 600;
}

.remove-button:hover {
    background: #dc2626;
}
</style>
