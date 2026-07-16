<template>
    <div class="checkout-page">
        <div class="checkout-header">
            <button class="btn-outline" @click="goBack">← 返回</button>
            <h2 class="page-title">收银结账</h2>
        </div>
        <div class="checkout-body">
            <!-- 左侧：商品选择与订单表单 -->
            <div class="left-panel">
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">商品选择</span>
                    </div>
                    <div class="product-search">
                        <input
                            v-model="productQuery"
                            placeholder="搜索商品名称..."
                            type="text"
                            @input="searchProducts"
                        />
                    </div>
                    <div v-if="productLoading" class="loading-overlay">
                        <div class="loading-spinner"></div>
                    </div>
                    <div v-else-if="productList.length === 0" class="empty-state">
                        <div class="empty-icon">📦</div>
                        <div class="empty-text">暂无商品</div>
                    </div>
                    <div v-else class="product-grid">
                        <div
                            v-for="item in productList"
                            :key="item.id"
                            class="product-item"
                            @click="openSkuPicker(item)"
                        >
                            <div class="product-name">{{ item.name }}</div>
                            <div class="product-id">{{ item.code }} · ¥{{ item.salePrice }}</div>
                        </div>
                    </div>
                </div>

                <div v-if="showSkuPicker" class="modal-overlay" @click.self="showSkuPicker = false">
                    <div class="modal-content sku-picker">
                        <div class="modal-header">
                            <span class="modal-title">选择 SKU - {{ skuProduct?.name }}</span>
                            <button class="modal-close" @click="showSkuPicker = false">&times;</button>
                        </div>
                        <div class="modal-body">
                            <div v-if="skuLoading" class="loading-overlay">
                                <div class="loading-spinner"></div>
                            </div>
                            <div v-else-if="skuList.length === 0" class="empty-state">
                                <div class="empty-icon">🏷️</div>
                                <div class="empty-text">该商品暂无可用 SKU</div>
                            </div>
                            <div v-else class="sku-picker-list">
                                <button v-for="sku in skuList" :key="sku.id" class="sku-picker-item"
                                        @click="addToCart(sku)">
                                    <strong>{{ sku.name }}</strong>
                                    <span>{{ sku.spec }}</span>
                                    <code>{{ sku.code }}</code>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <span class="card-title">购物车</span>
                        <span v-if="cart.length > 0" class="cart-count">{{ cart.length }} 项</span>
                    </div>
                    <div v-if="cart.length === 0" class="empty-state" style="padding: 24px;">
                        <div class="empty-icon">🛒</div>
                        <div class="empty-text">请选择商品</div>
                    </div>
                    <table v-else class="data-table">
                        <thead>
                        <tr>
                            <th>商品</th>
                            <th>数量</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="(item, idx) in cart" :key="idx">
                            <td>
                                <div>{{ item.name }}</div>
                                <small>{{ item.skuCode }}</small>
                            </td>
                            <td>
                                <input
                                    v-model.number="item.quantity"
                                    class="qty-input"
                                    min="1"
                                    type="number"
                                    @change="recalcTotal"
                                />
                            </td>
                            <td>
                                <button class="btn-danger btn-sm" @click="removeFromCart(idx)">移除</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div v-if="cart.length > 0" class="cart-total">
                        <span>合计：<strong>{{ cart.length }}</strong> 件商品</span>
                    </div>
                </div>
            </div>

            <!-- 右侧：订单操作 -->
            <div class="right-panel">
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">订单操作</span>
                    </div>
                    <div class="form-group">
                        <label>支付方式</label>
                        <select v-model="orderForm.payMethod">
                            <option v-for="item in payMethodOptions" :key="item.value" :value="item.value">{{
                                    item.label
                                }}
                            </option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>员工 ID</label>
                        <input v-model.number="orderForm.employeeId" min="1" placeholder="请输入员工 ID" type="number"/>
                    </div>
                    <div class="form-group">
                        <label>仓库 ID</label>
                        <input v-model.number="orderForm.wareHouseId" min="1" placeholder="请输入仓库 ID"
                               type="number"/>
                    </div>
                    <div class="form-group">
                        <label>备注</label>
                        <input v-model="orderForm.remark" maxlength="100" placeholder="订单备注（可选）" type="text"/>
                    </div>
                    <div class="order-actions">
                        <button
                            :disabled="cart.length === 0 || orderSubmitting"
                            class="btn-primary"
                            style="width:100%;"
                            @click="submitOrder('complete')"
                        >
                            {{ orderSubmitting ? '提交中...' : '完成订单' }}
                        </button>
                        <button
                            :disabled="cart.length === 0 || orderSubmitting"
                            class="btn-outline"
                            style="width:100%;"
                            @click="submitOrder('draft')"
                        >
                            保存草稿
                        </button>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <span class="card-title">订单列表</span>
                        <button class="btn-outline btn-sm" @click="fetchOrders">刷新</button>
                    </div>
                    <div v-if="orderLoading" class="loading-overlay">
                        <div class="loading-spinner"></div>
                    </div>
                    <div v-else-if="orderList.length === 0" class="empty-state">
                        <div class="empty-icon">📋</div>
                        <div class="empty-text">暂无订单</div>
                    </div>
                    <div v-else class="order-list">
                        <div
                            v-for="order in orderList"
                            :key="order.id || order.orderNo"
                            class="order-item"
                        >
                            <div class="order-head">
                                <span class="order-id">订单 #{{ order.orderNo }}</span>
                                <span :class="'order-' + (order.orderStatus || 'unknown')" class="order-status">
                                    {{ orderStatusLabels[order.orderStatus] || order.orderStatus || '未知' }}
                                </span>
                                <span class="order-meta">{{
                                        payMethodLabels[order.payMethod] || order.payMethod || '-'
                                    }} · 实付 ¥{{ order.actualPrice ?? '0.00' }}</span>
                            </div>
                            <div class="order-actions-row">
                                <button
                                    class="btn-success btn-sm"
                                    @click="handleRefund(order)"
                                >退款
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../stores/toastStore.js'
import productInterface from '../axios/interface/ProductInterface.js'
import productSkuInterface from '../axios/interface/ProductSkuInterface.js'
import orderInterface from '../axios/interface/OrderInterface.js'
import {PAY_METHOD_LABELS, PAY_METHOD_OPTIONS} from '../constants/payMethod.js'
import {STATUS} from '../constants/status.js'
import {ORDER_STATUS_LABELS} from '../constants/orderStatus.js'

const router = useRouter()
const payMethodOptions = PAY_METHOD_OPTIONS
const payMethodLabels = PAY_METHOD_LABELS
const orderStatusLabels = ORDER_STATUS_LABELS
const toast = useToastStore()

// 商品搜索
const productList = ref([])
const productQuery = ref('')
const productLoading = ref(true)

// 购物车
const cart = ref([])

// 订单
const orderList = ref([])
const orderLoading = ref(true)
const orderSubmitting = ref(false)
const orderForm = ref({
    payMethod: payMethodOptions[0].value,
    employeeId: '',
    wareHouseId: '',
    remark: ''
})
const showSkuPicker = ref(false)
const skuProduct = ref(null)
const skuList = ref([])
const skuLoading = ref(false)

onMounted(async () => {
    await Promise.all([searchProducts(), fetchOrders()])
})

async function searchProducts() {
    productLoading.value = true
    try {
        const list = (await productInterface.searchList()).filter(item => item.status === STATUS.ENABLE)
        const q = (productQuery.value || '').toLowerCase()
        productList.value = q
            ? list.filter(p => p.name?.toLowerCase().includes(q) || p.code?.toLowerCase().includes(q))
            : list
    } catch {
        productList.value = []
    } finally {
        productLoading.value = false
    }
}

// SKU 选择
async function openSkuPicker(product) {
    skuProduct.value = product
    skuList.value = []
    showSkuPicker.value = true
    skuLoading.value = true
    try {
        skuList.value = await productSkuInterface.searchListByProductId(product.id)
    } catch {
        skuList.value = []
    } finally {
        skuLoading.value = false
    }
}

// 购物车操作
function addToCart(item) {
    showSkuPicker.value = false
    const existing = cart.value.find(c => c.skuCode === item.code)
    if (existing) {
        existing.quantity++
    } else {
        cart.value.push({
            skuCode: item.code,
            name: `${skuProduct.value?.name || ''} / ${item.name}`,
            unitPrice: Number(skuProduct.value?.salePrice || 0),
            quantity: 1,
            discount: 1
        })
    }
    toast.success(`已添加「${item.name}」`)
}

function removeFromCart(idx) {
    cart.value.splice(idx, 1)
}

function recalcTotal() {
}

// 提交订单
async function submitOrder(type) {
    if (cart.value.length === 0) return
    orderSubmitting.value = true
    try {
        if (!orderForm.value.employeeId || !orderForm.value.wareHouseId) {
            toast.warning('请填写员工 ID 和仓库 ID')
            return
        }

        const orderItems = cart.value.map(c => ({
            skuCode: c.skuCode,
            discount: c.discount,
            quantity: c.quantity
        }))
        const totalAmount = cart.value.reduce((total, item) => total + item.unitPrice * item.quantity, 0)
        const actualAmount = cart.value.reduce((total, item) => total + item.unitPrice * item.quantity * item.discount, 0)
        const orderData = {
            payMethod: orderForm.value.payMethod,
            employeeId: Number(orderForm.value.employeeId),
            wareHouseId: Number(orderForm.value.wareHouseId),
            orderItems,
            actualAmount,
            totalAmount,
            remark: orderForm.value.remark || undefined
        }

        if (type === 'complete') {
            await orderInterface.complete(orderData)
            // 后端已返回提示
        } else {
            await orderInterface.draft(orderData)
            // 后端已返回提示
        }
        cart.value = []
        orderForm.value.remark = ''
        await fetchOrders()
    } catch {
        // 拦截器已处理
    } finally {
        orderSubmitting.value = false
    }
}

// 订单列表
async function fetchOrders() {
    orderLoading.value = true
    try {
        orderList.value = await orderInterface.searchList()
    } catch {
        orderList.value = []
    } finally {
        orderLoading.value = false
    }
}

// 退款
async function handleRefund() {
    toast.info('退款需要订单明细和员工、仓库信息，请从订单详情发起')
}

function goBack() {
    router.push('/manage')
}
</script>

<style scoped>
.checkout-page {
    height: 100%;
    max-width: 1280px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
}

.checkout-header {
    display: flex;
    align-items: flex-end;
    gap: 16px;
    margin-bottom: 28px;
}

.checkout-header .page-title {
    margin: 0;
    font-size: 27px;
    letter-spacing: -0.5px;
}

.page-title {
    font-size: var(--font-xl);
    font-weight: 700;
    color: var(--text-primary);
}

.checkout-body {
    display: flex;
    gap: 20px;
    flex: 1;
    overflow: hidden;
}

.left-panel {
    width: 52%;
    display: flex;
    flex-direction: column;
    gap: 16px;
    overflow-y: auto;
    flex-shrink: 0;
}

.right-panel {
    width: calc(48% - 20px);
    display: flex;
    flex-direction: column;
    gap: 16px;
    overflow-y: auto;
}

/* 商品搜索 */
.product-search {
    margin-bottom: 12px;
}

.product-search input {
    width: 100%;
}

.product-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.product-item {
    width: calc(50% - 4px);
    min-height: 78px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    padding: 14px;
    border: 1px solid #e3efed;
    border-radius: 12px;
    background: #fbfefd;
    cursor: pointer;
    transition: all 0.2s;
}

.product-item:hover {
    border-color: #73cfc5;
    background: #effbf9;
    transform: translateY(-2px);
    box-shadow: 0 8px 18px rgba(13, 148, 136, 0.1);
}

.product-name {
    font-weight: 700;
    font-size: var(--font-base);
    color: var(--text);
}

.product-id {
    font-size: var(--font-sm);
    color: var(--text-light);
    margin-top: 2px;
}

/* 购物车 */
.cart-count {
    font-size: var(--font-sm);
    color: var(--text-secondary);
    background: var(--bg-body);
    padding: 2px 10px;
    border-radius: 10px;
}

.qty-input {
    width: 60px;
    height: 30px;
    text-align: center;
}

.cart-total {
    padding: 12px 14px;
    border-top: 1px solid var(--border);
    text-align: right;
    font-size: var(--font-base);
    color: var(--text-secondary);
}

/* 订单操作 */
.order-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

/* 订单列表 */
.order-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.order-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px;
    border: 1px solid #e3efed;
    border-radius: 12px;
    background: #fbfefd;
    transition: background 0.2s, border-color 0.2s;
}

.order-item:hover {
    background: #f3fbf9;
    border-color: #bfe4df;
}

.order-head {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.order-id {
    font-weight: 600;
    font-size: var(--font-base);
}

.order-meta {
    color: var(--text-muted);
    font-size: var(--font-sm);
}

.order-status {
    font-size: var(--font-sm);
    padding: 3px 10px;
    border-radius: 999px;
    display: inline-block;
    width: fit-content;
}

.order-complete {
    background: var(--success-light);
    color: #16a34a;
}

.order-draft {
    background: var(--warning-light);
    color: #d97706;
}

.order-refund {
    background: var(--error-light);
    color: #dc2626;
}

.order-unknown {
    background: var(--bg-body);
    color: var(--text-light);
}

@media (max-width: 920px) {
    .checkout-body {
        flex-direction: column;
        overflow: visible;
    }

    .left-panel,
    .right-panel {
        width: 100%;
        overflow: visible;
    }
}
</style>
