<template>
    <div class="checkout-page">
        <div class="left-container">
            <div class="order-item-list">
                <div
                    v-if="cart.length > 0"
                    class="cart-header"
                >
                    <span class="header-product">商品信息</span>
                    <span class="header-price">销售单价</span>
                    <span class="header-quantity">数量</span>
                    <span class="header-discount">折扣</span>
                    <span class="header-price">实付单价</span>
                    <span class="header-subtotal">小计</span>
                    <span class="header-action">操作</span>
                </div>
                <div
                    v-for="(item, index) in cart"
                    :key="item.skuCode"
                    class="cart-row"
                >
                    <div class="cart-row-info">
                        <strong class="cart-row-name">{{ item.name }}</strong>
                        <span class="cart-row-code">{{ item.skuCode }}</span>
                    </div>
                    <span class="cart-unit-price">¥{{ item.unitPrice.toFixed(2) }}</span>
                    <div class="cart-row-qty">
                        <button
                            class="qty-btn"
                            @click="decreaseQty(index)"
                        >−</button>
                        <input
                            v-model.number="item.quantity"
                            class="qty-input"
                            min="1"
                            type="number"
                            @change="normalizeQuantity(index)"
                        />
                        <button
                            class="qty-btn"
                            @click="increaseQty(index)"
                        >+</button>
                    </div>
                    <div class="cart-row-discount">
                        <input
                            v-model.number="item.discount"
                            class="discount-input"
                            max="1"
                            min="0.01"
                            step="0.01"
                            type="number"
                            @change="normalizeDiscount(index)"
                        />
                    </div>
                    <span class="cart-actual-price">
                        ¥{{ (item.unitPrice * item.discount).toFixed(2) }}
                    </span>
                    <strong class="line-total">
                        ¥{{ (item.unitPrice * item.discount * item.quantity).toFixed(2) }}
                    </strong>
                    <div class="cart-row-action">
                        <button
                            class="cart-row-remove"
                            title="移除商品"
                            @click="removeFromCart(index)"
                        >&times;</button>
                    </div>
                </div>
                <div
                    v-if="cart.length === 0"
                    class="cart-empty"
                >
                    <div class="empty-icon">🧾</div>
                    <div class="empty-text">扫描或搜索商品开始结账</div>
                </div>
            </div>

            <div class="product-search-input">
                <div class="search-icon">🔍货号</div>
                <div class="search-input">
                    <input
                        v-model="query"
                        class="search-field"
                        placeholder="扫描条码 / 输入商品编码或名称，回车搜索"
                        type="text"
                        @keydown.enter="onSearchEnter"
                    />
                </div>
                <div class="employee-name">
                    <span class="employee-label">收银员工</span>
                    <span
                        v-if="selectedEmployee"
                        class="selected-employee"
                    >
                        {{ selectedEmployee.name }}（{{ selectedEmployee.code }}）
                    </span>
                    <span
                        v-else
                        class="employee-empty"
                    >尚未添加</span>
                    <button
                        class="employee-add-button"
                        @click="showEmployeeModal = true"
                    >
                        {{ selectedEmployee ? '更换员工' : '+ 添加员工' }}
                    </button>
                </div>
            </div>

            <div class="order-total">
                <div class="order-info">
                    <div class="info-card">
                        <span class="info-label">总金额</span>
                        <strong class="info-value info-value-total">¥{{ totalAmount }}</strong>
                    </div>
                    <div class="info-card">
                        <span class="info-label">总数量</span>
                        <strong class="info-value ">{{ totalQty }}</strong>
                    </div>
                    <div class="info-card">
                        <span class="info-label">优惠</span>
                        <strong class="info-value info-value-discount">-¥{{ discountAmount }}</strong>
                    </div>
                </div>
                <div class="actual-total">
                    <span class="info-label">实际支付金额</span>
                    <strong class="info-value">¥{{ actualTotalAmount }}</strong>
                </div>
            </div>
        </div>

        <div
            v-if="showSkuModal"
            class="modal-overlay"
            @click.self="showSkuModal = false"
        >
            <div class="modal-content sku-modal">
                <div class="modal-header">
                    <span class="modal-title">选择 SKU - {{ skuModalProductName }}</span>
                    <button
                        class="modal-close"
                        @click="showSkuModal = false"
                    >&times;</button>
                </div>
                <div class="modal-body">
                    <div
                        v-for="sku in skuModalResults"
                        :key="sku.id"
                        class="sku-modal-item"
                        @click="addToCart(sku)"
                    >
                        <div class="sku-modal-name">{{ sku.name || sku.code }}</div>
                        <div class="sku-modal-code">{{ sku.code }}</div>
                    </div>
                    <div
                        v-if="skuModalResults.length === 0"
                        class="modal-empty"
                    >
                        <div class="empty-text">未找到匹配的商品</div>
                    </div>
                </div>
            </div>
        </div>

        <div
            v-if="showEmployeeModal"
            class="modal-overlay"
            @click.self="showEmployeeModal = false"
        >
            <div class="modal-content employee-modal">
                <div class="modal-header">
                    <span class="modal-title">选择收银员工</span>
                    <button
                        class="modal-close"
                        @click="showEmployeeModal = false"
                    >&times;</button>
                </div>
                <div class="modal-body employee-list">
                    <input
                        v-model="employeeQuery"
                        class="employee-search"
                        placeholder="输入员工姓名或编码"
                        type="text"
                        @keydown.enter="selectOnlyEmployee"
                    />
                    <button
                        v-for="employee in filteredEmployees"
                        :key="employee.id"
                        :class="{ active: employee.id === selectedEmployeeId }"
                        class="employee-option"
                        @click="selectEmployee(employee)"
                    >
                        <span class="employee-option-name">{{ employee.name }}</span>
                        <span class="employee-option-code">{{ employee.code }}</span>
                    </button>
                    <div
                        v-if="filteredEmployees.length === 0"
                        class="modal-empty"
                    >
                        <div class="empty-text">
                            {{ employees.length === 0 ? '暂无可选员工' : '没有匹配的员工' }}
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div
            v-if="showDraftModal"
            class="modal-overlay"
            @click.self="showDraftModal = false"
        >
            <div class="modal-content draft-modal">
                <div class="modal-header">
                    <span class="modal-title">选择挂单</span>
                    <button
                        class="modal-close"
                        @click="showDraftModal = false"
                    >&times;</button>
                </div>
                <div class="modal-body">
                    <button
                        v-for="order in draftOrders"
                        :key="order.id"
                        class="draft-order-item"
                        @click="restoreDraft(order)"
                    >
                        <span class="draft-order-main">
                            <strong>{{ order.orderNo }}</strong>
                            <span>{{ order.employeeName || '未指定员工' }}</span>
                        </span>
                        <span class="draft-order-price">¥{{ order.actualPrice ?? '0.00' }}</span>
                    </button>
                </div>
            </div>
        </div>

        <div class="right-container">
            <div class="tool-bar">
                <button
                    class="tool-bar-item"
                    @click="draftOrder"
                >
                    <span class="tool-icon">💾</span>
                    <span class="tool-label">挂单</span>
                </button>
                <button
                    class="tool-bar-item"
                    @click="recallOrders"
                >
                    <span class="tool-icon">📋</span>
                    <span class="tool-label">挂单查询</span>
                </button>
                <button
                    class="tool-bar-item"
                    @click="handleRefund"
                >
                    <span class="tool-icon">↩️</span>
                    <span class="tool-label">退款</span>
                </button>
                <button
                    class="tool-bar-item tool-bar-item-danger"
                    @click="clearCart"
                >
                    <span class="tool-icon">🗑️</span>
                    <span class="tool-label">清空</span>
                </button>
            </div>

            <div class="payment-panel">
                <div class="pay-method-group">
                    <button
                        v-for="opt in payMethodOptions"
                        :key="opt.value"
                        :class="{ active: payMethod === opt.value }"
                        class="pay-method-btn"
                        @click="payMethod = opt.value"
                    >{{ opt.label }}</button>
                </div>
            </div>

            <div class="checkout-btn-group">
                <button
                    :disabled="submitting || cart.length === 0 || !selectedEmployeeId"
                    class="btn-checkout"
                    @click="completeOrder"
                >{{ submitting ? '提交中…' : '收银结账' }}</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {useToastStore} from '../stores/toastStore.js'
import {useUserStore} from '../stores/userStore.js'
import productSkuInterface from '../axios/interface/ProductSkuInterface.js'
import employeeInterface from '../axios/interface/EmployeeInterface.js'
import orderInterface from '../axios/interface/OrderInterface.js'
import {PAY_METHOD_OPTIONS} from '../constants/payMethod.js'

const toast = useToastStore()
const userStore = useUserStore()

const payMethodOptions = PAY_METHOD_OPTIONS

// 搜索
const query = ref('')

// SKU 选择弹窗
const showSkuModal = ref(false)
const skuModalResults = ref([])
const skuModalProductName = ref('')

// 当前仓库的员工
const employees = ref([])
const selectedEmployeeId = ref('')
const showEmployeeModal = ref(false)
const employeeQuery = ref('')
const selectedEmployee = computed(() => {
    return employees.value.find(employee => employee.id === selectedEmployeeId.value) || null
})
const filteredEmployees = computed(() => {
    const query = employeeQuery.value.trim().toLowerCase()
    if (!query) return employees.value
    return employees.value.filter(employee => {
        return employee.name?.toLowerCase().includes(query) ||
            employee.code?.toLowerCase().includes(query)
    })
})
// 购物车
const cart = ref([])
// 支付方式
const payMethod = ref(payMethodOptions[0].value)
// 提交状态
const submitting = ref(false)

// 挂单列表（用于取单）
const draftOrders = ref([])
const showDraftModal = ref(false)

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    try {
        const list = await employeeInterface.verifyList()
        employees.value = list
        if (list.length === 1) {
            selectedEmployeeId.value = list[0].id
        }
    } catch {
        employees.value = []
    }
})

onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

// 计算属性
const totalAmount = computed(() => {
    return cart.value
        .reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
        .toFixed(2)
})

const totalQty = computed(() => {
    return cart.value.reduce((sum, item) => sum + item.quantity, 0)
})

const discountAmount = computed(() => {
    const raw = cart.value.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
    const actual = cart.value.reduce((sum, item) => sum + item.unitPrice * item.quantity * item.discount, 0)
    return (raw - actual).toFixed(2)
})

const actualTotalAmount = computed(() => {
    return cart.value
        .reduce((sum, item) => sum + item.unitPrice * item.quantity * item.discount, 0)
        .toFixed(2)
})

function selectEmployee(employee) {
    selectedEmployeeId.value = employee.id
    employeeQuery.value = ''
    showEmployeeModal.value = false
}

function selectOnlyEmployee() {
    if (filteredEmployees.value.length === 1) {
        selectEmployee(filteredEmployees.value[0])
    }
}

// 搜索
async function onSearchEnter() {
    const q = query.value.trim()
    if (!q) return
    try {
        const data = await productSkuInterface.scan(q)
        let results = []
        if (Array.isArray(data)) {
            results = data
        } else if (data) {
            results = [data]
        }
        if (results.length <= 1) {
            if (results.length === 1) {
                addToCart(results[0])
            }
            // 没命中时后端已返回提示
            return
        }
        skuModalResults.value = results
        skuModalProductName.value = results[0].name || q
        showSkuModal.value = true
    } catch {
        // 拦截器已处理
    }
}

// 加入购物车
function addToCart(sku) {
    const existing = cart.value.find(c => c.skuCode === sku.code)
    if (existing) {
        existing.quantity++
    } else {
        cart.value.push({
            skuCode: sku.code,
            name: sku.productName || sku.name || sku.code,
            unitPrice: Number(sku.salePrice || 0),
            quantity: 1,
            discount: 1
        })
    }
    showSkuModal.value = false
    query.value = ''
    toast.success(`已添加「${sku.name || sku.code}」`)
}

function increaseQty(index) {
    cart.value[index].quantity++
}

function decreaseQty(index) {
    const item = cart.value[index]
    if (item.quantity > 1) {
        item.quantity--
    } else {
        removeFromCart(index)
    }
}

function removeFromCart(index) {
    cart.value.splice(index, 1)
}

function clearCart() {
    if (cart.value.length === 0) return
    if (!window.confirm('确定要清空当前购物车吗？')) return
    cart.value = []
    toast.info('购物车已清空')
}

function normalizeQuantity(index) {
    const quantity = Number(cart.value[index].quantity)
    cart.value[index].quantity = Number.isInteger(quantity) && quantity > 0 ? quantity : 1
}

function normalizeDiscount(index) {
    const discount = Number(cart.value[index].discount)
    if (!Number.isFinite(discount) || discount <= 0) {
        cart.value[index].discount = 1
        return
    }
    cart.value[index].discount = Math.min(Number(discount.toFixed(2)), 1)
}

// 提交订单
async function completeOrder() {
    if (cart.value.length === 0) return
    if (!selectedEmployeeId.value) {
        toast.warning('请选择本次收银员工')
        return
    }
    submitting.value = true
    try {
        const saleItems = cart.value.map(c => ({
            skuCode: c.skuCode,
            discount: c.discount,
            quantity: c.quantity
        }))
        const rawTotal = cart.value.reduce((s, c) => s + c.unitPrice * c.quantity, 0)
        const orderData = {
            payMethod: payMethod.value,
            employeeId: Number(selectedEmployeeId.value),
            wareHouseId: Number(userStore.userInfo?.id || 0),
            saleItems,
            refundItems: [],
            actualAmount: Number(actualTotalAmount.value),
            totalAmount: Number(rawTotal.toFixed(2)),
            remark: ''
        }
        await orderInterface.complete(orderData)
        cart.value = []
        toast.success('订单已完成')
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

// 挂单
async function draftOrder() {
    if (cart.value.length === 0) return
    if (!selectedEmployeeId.value) {
        toast.warning('请选择本次收银员工')
        return
    }
    submitting.value = true
    try {
        const saleItems = cart.value.map(c => ({
            skuCode: c.skuCode,
            discount: c.discount,
            quantity: c.quantity
        }))
        const rawTotal = cart.value.reduce((s, c) => s + c.unitPrice * c.quantity, 0)
        const orderData = {
            payMethod: payMethod.value,
            employeeId: Number(selectedEmployeeId.value),
            wareHouseId: Number(userStore.userInfo?.id || 0),
            saleItems,
            refundItems: [],
            actualAmount: Number(actualTotalAmount.value),
            totalAmount: Number(rawTotal.toFixed(2)),
            remark: ''
        }
        await orderInterface.draft(orderData)
        cart.value = []
        toast.success('已挂单')
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

// 取单
async function recallOrders() {
    try {
        const data = await orderInterface.searchCurrentWareHousePage()
        draftOrders.value = data.content || []
        if (draftOrders.value.length === 0) {
            toast.info('暂无挂单')
            return
        }
        showDraftModal.value = true
    } catch {
        // 拦截器已处理
    }
}

async function restoreDraft(order) {
    toast.info(`挂单「${order.orderNo}」暂不支持恢复商品明细`)
}

function handleBeforeUnload(event) {
    if (cart.value.length === 0) return
    event.preventDefault()
    event.returnValue = ''
}

// 退款
async function handleRefund() {
    toast.info('请在订单详情中发起退款')
}
</script>

<style scoped>
.checkout-page {
    height: 100vh;
    display: flex;
}

.left-container {
    width: 80%;
    display: flex;
    flex-direction: column;
    padding: 20px 20px 0 20px;
    background: #f5faf9;
    position: relative;
}

.order-item-list {
    height: 95%;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.cart-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    height: 100%;
    color: #94a3b8;
}

.cart-header {
    position: sticky;
    top: 0;
    z-index: 5;
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 950px;
    padding: 13px 14px;
    border: 1px solid #bfe4dd;
    border-radius: 10px;
    background: #dff3ef;
    color: #285f5a;
    font-size: 14px;
    font-weight: 800;
    box-shadow: 0 3px 10px rgba(13, 148, 136, 0.08);
}

.header-product {
    width: 220px;
    text-align: center;
}

.header-price,
.header-quantity,
.header-discount,
.header-subtotal,
.header-action {
    border-left: 1px solid rgba(40, 95, 90, 0.2);
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
    min-width: 950px;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 14px;
    background: #fff;
    border-radius: 10px;
    border: 1px solid #e3efed;
}

.cart-row-info {
    width: 220px;
    min-width: 0;
    text-align: center;
}

.cart-row-name {
    display: block;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.cart-row-code {
    font-size: 11px;
    color: #94a3b8;
    font-family: monospace;
}

.cart-unit-price,
.cart-row-qty,
.cart-row-discount,
.cart-actual-price,
.line-total,
.cart-row-action {
    border-left: 1px solid #e3efed;
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

/* 搜索区 */
.product-search-input {
    height: 6%;
    width: calc(100% + 40px);
    margin-left: -20px;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
    background: rgba(13, 148, 136, 0.08);
}

.search-icon {
    flex-shrink: 0;
    font-size: 14px;
    font-weight: 600;
    color: #475569;
}

.search-input {
    position: relative;
    width: 35%;
}

.search-field {
    width: 100%;
    height: 42px;
    padding: 0 14px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fff;
    font-size: 15px;
    outline: none;
}

.search-field:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.search-dropdown {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    z-index: 100;
    margin-top: 4px;
    background: #fff;
    border: 1px solid #e3efed;
    border-radius: 10px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.1);
    max-height: 260px;
    overflow-y: auto;
}

.search-result-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    cursor: pointer;
    border-bottom: 1px solid #f0f6f4;
}

.search-result-item:last-child {
    border-bottom: none;
}

.search-result-item:hover {
    background: #f0fdfb;
}

.result-name {
    font-weight: 600;
    font-size: 14px;
}

.result-code {
    font-size: 12px;
    color: #94a3b8;
    font-family: monospace;
}

.employee-name {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 8px;
}

.employee-label {
    color: #475569;
    font-size: 13px;
    font-weight: 700;
    white-space: nowrap;
}

.selected-employee {
    padding: 7px 11px;
    border: 1px solid #bfe4dd;
    border-radius: 9px;
    background: #eef9f7;
    color: #0f766e;
    font-size: 13px;
    font-weight: 700;
    white-space: nowrap;
}

.employee-empty {
    color: #94a3b8;
    font-size: 13px;
    white-space: nowrap;
}

.employee-add-button {
    height: 36px;
    padding: 0 13px;
    border: 1px solid #0d9488;
    border-radius: 9px;
    background: #fff;
    color: #0d9488;
    font-size: 13px;
    font-weight: 700;
}

.employee-add-button:hover {
    background: #ccfbf1;
}

.employee-modal {
    width: 440px;
    max-height: 520px;
}

.employee-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.employee-search {
    width: 100%;
    height: 40px;
    margin-bottom: 6px;
    background: #fbfefd;
}

.employee-option {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border: 1px solid #e3efed;
    border-radius: 10px;
    background: #fff;
    color: #0f172a;
}

.employee-option:hover {
    border-color: #8bd5ca;
    background: #f0fdfb;
}

.employee-option.active {
    border-color: #0d9488;
    background: #ccfbf1;
    color: #0f766e;
}

.employee-option-name {
    font-size: 14px;
    font-weight: 700;
}

.employee-option-code {
    color: #94a3b8;
    font-family: monospace;
    font-size: 12px;
}

/* 底部合计 */
.order-total {
    position: absolute;
    bottom: 20px;
    right: 20px;
    z-index: 10;
    display: flex;
    flex-direction: column;
    gap: 8px;
    align-items: flex-end;
    background: linear-gradient(135deg, #0d9488, rgb(13 148 136 / 0.64));
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.order-info {
    display: flex;
    gap: 8px;
    padding-top: 14px;
    padding-left: 14px;
    padding-right: 14px;
}

.info-card {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px 18px;
    border-radius: 12px;
    min-width: 100px;
    background: whitesmoke;
}

.info-card .info-label {
    font-size: 11px;
    color: black;
    font-weight: 600;
}

.info-card .info-value {
    font-size: 20px;
    font-weight: 800;
    color: black;
}

.info-value-discount {
    color: #fb3030 !important;
}

.info-value-total {
    color: #f59e0b !important;
}

.actual-total {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px 16px;
    background: linear-gradient(135deg, #f59e0b, #d97706);
    border-radius: 12px;
    align-self: stretch;
}

.actual-total .info-label {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.75);
    font-weight: 600;
}

.actual-total .info-value {
    font-size: 24px;
    font-weight: 800;
    color: #fff;
}

/* ── 右侧容器 ── */
.right-container {
    width: 20%;
    display: flex;
    flex-direction: column;
    padding: 20px;
    background: #fff;
    border-left: 1px solid #e3efed;
}

.tool-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 20px;
}

.tool-bar-item {
    width: calc(50% - 4px);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 16px 8px;
    border: 1px solid #e3efed;
    border-radius: 12px;
    background: #fbfefd;
    cursor: pointer;
    transition: all 0.2s;
}

.tool-bar-item:hover {
    background: #f0fdfb;
    border-color: #14b8a6;
}

.tool-bar-item-danger:hover {
    background: #fef2f2;
    border-color: #ef4444;
}

.tool-icon {
    font-size: 24px;
}

.tool-label {
    font-size: 13px;
    font-weight: 600;
    color: #475569;
}

/* 支付方式 */
.payment-panel {
    margin-bottom: auto;
}

.pay-method-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.pay-method-btn {
    width: calc(50% - 4px);
    padding: 10px 0;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fbfefd;
    font-size: 13px;
    font-weight: 600;
    color: #475569;
    cursor: pointer;
    transition: all 0.2s;
}

.pay-method-btn:hover {
    border-color: #14b8a6;
    color: #0d9488;
}

.pay-method-btn.active {
    background: #ccfbf1;
    border-color: #0d9488;
    color: #0d9488;
}

/* 结账按钮 */
.checkout-btn-group {
    flex-shrink: 0;
    padding-top: 16px;
}

.btn-checkout {
    width: 100%;
    height: 52px;
    padding: 0;
    border: none;
    border-radius: 12px;
    background: linear-gradient(135deg, #0d9488, #14b8a6);
    color: #fff;
    font-size: 18px;
    font-weight: 800;
    letter-spacing: 2px;
    cursor: pointer;
    box-shadow: 0 4px 16px rgba(13, 148, 136, 0.3);
    transition: all 0.25s;
}

.btn-checkout:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 24px rgba(13, 148, 136, 0.4);
}

.btn-checkout:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
}

/* ── SKU 选择弹窗 ── */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.35);
}

.sku-modal {
    width: 400px;
    max-height: 500px;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e3efed;
}

.modal-title {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
}

.modal-close {
    padding: 0;
    border: none;
    background: none;
    font-size: 24px;
    color: #94a3b8;
    cursor: pointer;
    line-height: 1;
}

.modal-close:hover {
    color: #ef4444;
}

.modal-body {
    flex-grow: 1;
    overflow-y: auto;
    padding: 8px;
}

.sku-modal-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 14px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.15s;
}

.sku-modal-item:hover {
    background: #f0fdfb;
}

.sku-modal-name {
    font-weight: 600;
    font-size: 14px;
    color: #0f172a;
}

.sku-modal-code {
    font-size: 13px;
    color: #94a3b8;
    font-family: monospace;
}

.draft-modal {
    width: 520px;
    max-height: 560px;
}

.draft-order-item {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 13px 14px;
    border-bottom: 1px solid #edf4f2;
    border-radius: 0;
    background: #fff;
    color: #0f172a;
}

.draft-order-item:hover {
    background: #f0fdfb;
}

.draft-order-main {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 3px;
}

.draft-order-main span {
    color: #94a3b8;
    font-size: 12px;
}

.draft-order-price {
    color: #0d9488;
    font-size: 16px;
    font-weight: 800;
}

.modal-empty {
    display: flex;
    justify-content: center;
    padding: 40px 0;
}
</style>
