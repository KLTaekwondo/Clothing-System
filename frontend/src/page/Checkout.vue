<template>
    <div
        class="checkout-page"
        @keydown.esc="closeActiveModal"
    >
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
                        <span
                            :class="item.direction === 'OUT' ? 'direction-refund' : 'direction-sale'"
                            class="direction-badge"
                        >{{ item.direction === 'OUT' ? '退货' : '销售' }}</span>
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
                    <div class="empty-icon"><IconGraphic name="receipt"/></div>
                    <div class="empty-text">扫描或搜索商品开始结账</div>
                </div>
            </div>

            <div class="product-search-input">
                <button
                    :class="inputDirection === 'OUT' ? 'mode-refund' : 'mode-sale'"
                    class="search-mode"
                    @click="toggleInputDirection"
                >{{ inputDirection === 'OUT' ? '− 退货' : '+ 销售' }}</button>
                <div class="search-input">
                    <input
                        ref="searchField"
                        v-model="query"
                        class="search-field"
                        :placeholder="inputDirection === 'OUT' ? '退货模式：输入货号后回车，按 + 切换销售' : '销售模式：输入货号后回车，按 - 切换退货'"
                        type="text"
                        @keydown="handleSearchKeydown"
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
                        @click="openEmployeeModal"
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
                    <button
                        v-for="sku in skuModalResults"
                        :key="sku.id"
                        ref="skuOptionFields"
                        class="sku-modal-item"
                        type="button"
                        @click="addToCart(sku)"
                    >
                        <span class="sku-modal-name">{{ sku.name || sku.code }}</span>
                        <span class="sku-modal-code">{{ sku.code }}</span>
                    </button>
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
                        ref="employeeSearchField"
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
                        @click="requestRestoreDraft(order)"
                    >
                        <span class="draft-order-main">
                            <strong>{{ order.orderNo }}</strong>
                            <span>{{ order.employeeName || '未指定员工' }}</span>
                        </span>
                        <span class="draft-order-price">¥{{ order.actualPrice ?? '0.00' }}</span>
                    </button>
                    <div
                        v-if="draftOrders.length === 0"
                        class="modal-empty"
                    >
                        <div class="empty-text">暂无挂单</div>
                    </div>
                </div>
            </div>
        </div>

        <div
            v-if="showConfirmModal"
            class="modal-overlay"
            @click.self="closeConfirmModal"
        >
            <div class="modal-content confirm-modal">
                <div class="modal-header">
                    <span class="modal-title">{{ confirmTitle }}</span>
                    <button
                        class="modal-close"
                        @click="closeConfirmModal"
                    >&times;</button>
                </div>
                <div class="confirm-body">{{ confirmMessage }}</div>
                <div class="confirm-actions">
                    <button
                        class="confirm-cancel"
                        @click="closeConfirmModal"
                    >取消</button>
                    <button
                        class="confirm-submit"
                        @click="confirmPendingAction"
                    >确定</button>
                </div>
            </div>
        </div>

        <div class="right-container">
            <div class="tool-bar">
                <button
                    :disabled="submitting"
                    class="tool-bar-item"
                    @click="draftOrder"
                >
                    <span class="tool-icon"><IconGraphic name="save"/></span>
                    <span class="tool-label">挂单</span>
                </button>
                <button
                    :disabled="submitting || draftLoading"
                    class="tool-bar-item"
                    @click="recallOrders"
                >
                    <span class="tool-icon"><IconGraphic name="order"/></span>
                    <span class="tool-label">挂单查询</span>
                </button>
                <button
                    :class="inputDirection === 'OUT' ? 'tool-bar-item-active' : 'tool-bar-item'"
                    @click="setInputDirection(inputDirection === 'OUT' ? 'IN' : 'OUT')"
                >
                    <span class="tool-icon"><IconGraphic name="refund"/></span>
                    <span class="tool-label">{{ inputDirection === 'OUT' ? '返回销售' : '退货模式' }}</span>
                </button>
                <button
                    class="tool-bar-item tool-bar-item-danger"
                    @click="clearCart"
                >
                    <span class="tool-icon"><IconGraphic name="trash"/></span>
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
import {computed, nextTick, onBeforeUnmount, onMounted, ref} from 'vue'
import {onBeforeRouteLeave} from 'vue-router'
import {useToastStore} from '../stores/toastStore.js'
import {useUserStore} from '../stores/userStore.js'
import productSkuInterface from '../axios/interface/ProductSkuInterface.js'
import employeeInterface from '../axios/interface/EmployeeInterface.js'
import orderInterface from '../axios/interface/OrderInterface.js'
import {PAY_METHOD_OPTIONS} from '../constants/payMethod.js'
import IconGraphic from '../component/IconGraphic.vue'

const toast = useToastStore()
const userStore = useUserStore()

const payMethodOptions = PAY_METHOD_OPTIONS

// 搜索
const query = ref('')
const searchField = ref(null)
const inputDirection = ref('IN')

// SKU 选择弹窗
const showSkuModal = ref(false)
const skuModalResults = ref([])
const skuModalProductName = ref('')
const skuOptionFields = ref([])

// 当前仓库的员工
const employees = ref([])
const selectedEmployeeId = ref('')
const showEmployeeModal = ref(false)
const employeeQuery = ref('')
const employeeSearchField = ref(null)
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

// 当前恢复的挂单
const currentDraftOrderId = ref(null)

// 挂单列表（用于取单）
const draftOrders = ref([])
const showDraftModal = ref(false)
const draftLoading = ref(false)

// 页面确认弹窗
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
let pendingConfirmAction = null
let pendingConfirmCancel = null
let allowRouteLeave = false

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    try {
        const list = await employeeInterface.verifyList()
        employees.value = list
        if (list.length === 1) {
            selectedEmployeeId.value = list[0].id
        }
        focusSearchField()
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
        .reduce((sum, item) => sum + getDirectionMultiplier(item) * item.unitPrice * item.quantity, 0)
        .toFixed(2)
})

const totalQty = computed(() => {
    return cart.value.reduce((sum, item) => sum + item.quantity, 0)
})

const discountAmount = computed(() => {
    const raw = cart.value.reduce((sum, item) => {
        return sum + getDirectionMultiplier(item) * item.unitPrice * item.quantity
    }, 0)
    const actual = cart.value.reduce((sum, item) => {
        return sum + getDirectionMultiplier(item) * item.unitPrice * item.quantity * item.discount
    }, 0)
    return (raw - actual).toFixed(2)
})

const actualTotalAmount = computed(() => {
    return cart.value
        .reduce((sum, item) => {
            return sum + getDirectionMultiplier(item) * item.unitPrice * item.quantity * item.discount
        }, 0)
        .toFixed(2)
})

function getDirectionMultiplier(item) {
    return item.direction === 'OUT' ? -1 : 1
}

function openEmployeeModal() {
    employeeQuery.value = ''
    showEmployeeModal.value = true
    nextTick(() => {
        employeeSearchField.value?.focus()
    })
}

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
function handleSearchKeydown(event) {
    if (!query.value && (event.key === '+' || event.key === '-')) {
        event.preventDefault()
        setInputDirection(event.key === '-' ? 'OUT' : 'IN')
        return
    }
    if (event.key === 'Enter') {
        event.preventDefault()
        onSearchEnter()
    }
}

function setInputDirection(direction) {
    inputDirection.value = direction
    query.value = ''
    focusSearchField()
}

function toggleInputDirection() {
    setInputDirection(inputDirection.value === 'OUT' ? 'IN' : 'OUT')
}

function focusSearchField() {
    nextTick(() => {
        searchField.value?.focus()
    })
}

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
            } else {
                query.value = ''
                focusSearchField()
            }
            return
        }
        skuModalResults.value = results
        skuModalProductName.value = results[0].productName || q
        showSkuModal.value = true
        nextTick(() => {
            skuOptionFields.value[0]?.focus()
        })
    } catch {
        // 拦截器已处理
    }
}

// 加入购物车
function addToCart(sku) {
    const direction = inputDirection.value
    const existing = cart.value.find(c => {
        return c.skuCode === sku.code && c.direction === direction
    })
    if (existing) {
        existing.quantity++
    } else {
        cart.value.push({
            skuCode: sku.code,
            name: sku.productName || sku.name || sku.code,
            unitPrice: Number(sku.salePrice || 0),
            quantity: 1,
            discount: 1,
            direction
        })
    }
    showSkuModal.value = false
    query.value = ''
    toast.success(`已添加${direction === 'OUT' ? '退货' : '销售'}商品「${sku.name || sku.code}」`)
    focusSearchField()
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
    openConfirmModal(
        '清空购物车',
        '确定要清空当前购物车吗？此操作不会保存当前内容。',
        resetCurrentOrder
    )
}

function resetCurrentOrder() {
    cart.value = []
    currentDraftOrderId.value = null
    query.value = ''
    inputDirection.value = 'IN'
    focusSearchField()
}

function openConfirmModal(title, message, action, cancelAction = null) {
    confirmTitle.value = title
    confirmMessage.value = message
    pendingConfirmAction = action
    pendingConfirmCancel = cancelAction
    showConfirmModal.value = true
}

function closeConfirmModal() {
    const cancelAction = pendingConfirmCancel
    showConfirmModal.value = false
    pendingConfirmAction = null
    pendingConfirmCancel = null
    cancelAction?.()
    focusSearchField()
}

function confirmPendingAction() {
    const action = pendingConfirmAction
    showConfirmModal.value = false
    pendingConfirmAction = null
    pendingConfirmCancel = null
    action?.()
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

function buildOrderData() {
    const toOrderItem = item => ({
        skuCode: item.skuCode,
        discount: item.discount,
        quantity: item.quantity
    })
    const saleItems = cart.value
        .filter(item => item.direction !== 'OUT')
        .map(toOrderItem)
    const refundItems = cart.value
        .filter(item => item.direction === 'OUT')
        .map(toOrderItem)

    return {
        orderId: currentDraftOrderId.value,
        payMethod: payMethod.value,
        employeeId: Number(selectedEmployeeId.value),
        wareHouseId: Number(userStore.userInfo?.id || 0),
        saleItems,
        refundItems,
        actualAmount: Number(actualTotalAmount.value),
        totalAmount: Number(totalAmount.value),
        remark: ''
    }
}

// 提交订单
async function completeOrder() {
    if (submitting.value || cart.value.length === 0) return
    if (!selectedEmployeeId.value) {
        toast.warning('请选择本次收银员工')
        return
    }
    submitting.value = true
    try {
        await orderInterface.complete(buildOrderData())
        resetCurrentOrder()
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

// 挂单
async function draftOrder() {
    if (submitting.value || cart.value.length === 0) return
    if (!selectedEmployeeId.value) {
        toast.warning('请选择本次收银员工')
        return
    }
    submitting.value = true
    try {
        if (currentDraftOrderId.value) {
            await orderInterface.update(buildOrderData())
        } else {
            await orderInterface.draft(buildOrderData())
        }
        resetCurrentOrder()
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

// 取单
async function recallOrders() {
    if (draftLoading.value || submitting.value) return
    draftLoading.value = true
    try {
        const data = await orderInterface.searchCurrentWareHousePage(0, 50)
        draftOrders.value = data.content || []
        if (draftOrders.value.length === 0) {
            toast.info('暂无挂单')
            return
        }
        showDraftModal.value = true
    } catch {
        // 拦截器已处理
    } finally {
        draftLoading.value = false
    }
}

function requestRestoreDraft(order) {
    if (cart.value.length === 0) {
        restoreDraft(order)
        return
    }
    openConfirmModal(
        '恢复挂单',
        '恢复挂单会覆盖当前购物车，确定继续吗？',
        () => restoreDraft(order)
    )
}

async function restoreDraft(order) {
    try {
        const detail = await orderInterface.search(order.id)
        cart.value = (detail.items || []).map(item => ({
            skuCode: item.skuCode,
            name: item.productName || item.skuName || item.skuCode,
            unitPrice: Number(item.unitPrice || 0),
            quantity: Number(item.quantity || 1),
            discount: Number(item.discount ?? 1),
            direction: item.direction === 'OUT' ? 'OUT' : 'IN'
        }))
        currentDraftOrderId.value = detail.id
        payMethod.value = detail.payMethod || payMethodOptions[0].value
        const draftEmployee = employees.value.find(employee => {
            return employee.name === detail.employeeName
        })
        selectedEmployeeId.value = draftEmployee?.id || ''
        showDraftModal.value = false
        inputDirection.value = 'IN'
        focusSearchField()
        if (!draftEmployee) {
            toast.warning('原挂单员工不可用，请重新选择收银员工')
        }
    } catch {
        // 拦截器已处理
    }
}

onBeforeRouteLeave((to, from, next) => {
    if (cart.value.length === 0 || allowRouteLeave) {
        allowRouteLeave = false
        next()
        return
    }
    openConfirmModal(
        '离开收银页面',
        '当前购物车尚未保存，确定离开收银页面吗？',
        () => {
            allowRouteLeave = true
            next()
        },
        () => next(false)
    )
})

function closeActiveModal() {
    if (showConfirmModal.value) {
        closeConfirmModal()
        return
    }
    showSkuModal.value = false
    showEmployeeModal.value = false
    showDraftModal.value = false
    focusSearchField()
}

function handleBeforeUnload(event) {
    if (cart.value.length === 0) return
    event.preventDefault()
    event.returnValue = ''
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
    position: relative;
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

.search-mode {
    flex-shrink: 0;
    min-width: 78px;
    height: 36px;
    padding: 0 12px;
    border: none;
    border-radius: 18px;
    color: #fff;
    font-size: 13px;
    font-weight: 800;
    cursor: pointer;
}

.mode-sale {
    background: #0d9488;
}

.mode-refund {
    background: #dc2626;
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

.tool-bar-item-active {
    width: calc(50% - 4px);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 16px 8px;
    border: 1px solid #ef4444;
    border-radius: 12px;
    background: #fee2e2;
    cursor: pointer;
}

.tool-bar-item:disabled {
    opacity: 0.5;
    cursor: not-allowed;
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

.tool-bar-item-active .tool-label {
    color: #b91c1c;
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

.confirm-modal {
    width: 380px;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.18);
    overflow: hidden;
}

.confirm-body {
    padding: 24px 20px;
    color: #475569;
    font-size: 14px;
    line-height: 1.7;
}

.confirm-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 12px 20px 18px;
}

.confirm-cancel,
.confirm-submit {
    min-width: 82px;
    height: 36px;
    border-radius: 8px;
    font-weight: 700;
    cursor: pointer;
}

.confirm-cancel {
    border: 1px solid #dceae7;
    background: #fff;
    color: #475569;
}

.confirm-submit {
    border: none;
    background: #dc2626;
    color: #fff;
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
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 14px;
    border: none;
    border-radius: 10px;
    background: #fff;
    cursor: pointer;
    transition: background 0.15s;
    text-align: left;
}

.sku-modal-item:hover,
.sku-modal-item:focus {
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
