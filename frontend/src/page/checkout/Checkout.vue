<template>
    <div
        class="checkout-page"
        @keydown.esc="closeActiveModal"
    >
        <div class="left-container">
            <CheckoutCart
                :items="cart"
                @decrease="decreaseQty"
                @increase="increaseQty"
                @normalize-discount="normalizeDiscount"
                @normalize-quantity="normalizeQuantity"
                @remove="removeFromCart"
            />

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

            <Transition
                mode="out-in"
                name="total-card"
            >
                <div
                    v-if="!totalCardCollapsed"
                    key="expanded"
                    class="order-total"
                >
                    <button
                        class="total-collapse-button"
                        title="收起金额卡片"
                        @click="totalCardCollapsed = true"
                    >收起</button>
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
                <button
                    v-else
                    key="collapsed"
                    class="order-total-collapsed"
                    title="展开金额卡片"
                    @click="totalCardCollapsed = false"
                >
                    <span>应付</span>
                    <strong>¥{{ actualTotalAmount }}</strong>
                    <span class="expand-label">展开</span>
                </button>
            </Transition>
        </div>

        <SkuSelectModal
            :product-name="skuModalProductName"
            :results="skuModalResults"
            :visible="showSkuModal"
            @close="showSkuModal = false"
            @select="addToCart"
        />

        <EmployeeSelectModal
            :employees="employees"
            :selected-employee-id="selectedEmployeeId"
            :visible="showEmployeeModal"
            @close="showEmployeeModal = false"
            @select="selectEmployee"
        />

        <CheckoutConfirmModal
            :confirm-text="confirmButtonText"
            :details="confirmDetails"
            :icon="confirmIcon"
            :message="confirmMessage"
            :title="confirmTitle"
            :visible="showConfirmModal"
            @cancel="closeConfirmModal"
            @confirm="confirmPendingAction"
        />

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
                    :disabled="submitting"
                    class="tool-bar-item"
                    @click="openDraftPage"
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
                <div class="payment-heading">
                    <span class="payment-icon"><IconGraphic name="money"/></span>
                    <span class="payment-title">选择支付方式</span>
                    <strong class="payment-amount">应付 ¥{{ actualTotalAmount }}</strong>
                </div>
                <div class="pay-method-group">
                    <button
                        v-for="opt in payMethodOptions"
                        :key="opt.value"
                        :class="{ active: payMethod === opt.value }"
                        class="pay-method-btn"
                        @click="payMethod = opt.value"
                    >
                        <img
                            :src="payMethodIcons[opt.value]"
                            alt=""
                        />
                        <span>{{ opt.label }}</span>
                    </button>
                </div>
            </div>

            <div class="checkout-btn-group">
                <button
                    :disabled="submitting || cart.length === 0 || !selectedEmployeeId"
                    class="btn-checkout"
                    @click="requestCompleteOrder"
                >{{ submitting ? '提交中…' : '收银结账' }}</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref} from 'vue'
import {onBeforeRouteLeave, useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import {useUserStore} from '../../stores/userStore.js'
import productSkuInterface from '../../axios/interface/ProductSkuInterface.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import orderInterface from '../../axios/interface/OrderInterface.js'
import {PAY_METHOD_OPTIONS} from '../../constants/payMethod.js'
import cashIcon from '../../assets/icons/cash.svg'
import bankCardIcon from '../../assets/icons/bank-card.svg'
import alipayIcon from '../../assets/icons/alipay.svg'
import wechatIcon from '../../assets/icons/wechat.svg'
import IconGraphic from '../../component/IconGraphic.vue'
import CheckoutCart from './components/CheckoutCart.vue'
import CheckoutConfirmModal from './components/CheckoutConfirmModal.vue'
import EmployeeSelectModal from './components/EmployeeSelectModal.vue'
import SkuSelectModal from './components/SkuSelectModal.vue'

const toast = useToastStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const payMethodOptions = PAY_METHOD_OPTIONS
const payMethodIcons = {
    CASH: cashIcon,
    CARD: bankCardIcon,
    ALIPAY: alipayIcon,
    WECHAT: wechatIcon
}

// 搜索
const query = ref('')
const searchField = ref(null)
const inputDirection = ref('IN')

// SKU 选择弹窗
const showSkuModal = ref(false)
const skuModalResults = ref([])
const skuModalProductName = ref('')

// 当前仓库的员工
const employees = ref([])
const selectedEmployeeId = ref('')
const showEmployeeModal = ref(false)
const selectedEmployee = computed(() => {
    return employees.value.find(employee => employee.id === selectedEmployeeId.value) || null
})
// 购物车
const cart = ref([])
const totalCardCollapsed = ref(false)
// 支付方式
const payMethod = ref(payMethodOptions[0].value)
// 提交状态
const submitting = ref(false)

// 当前恢复的挂单
const currentDraftOrderId = ref(null)

// 页面确认弹窗
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmIcon = ref('')
const confirmDetails = ref([])
const confirmButtonText = ref('确定')
let pendingConfirmAction = null
let pendingConfirmCancel = null
let allowRouteLeave = false
let restoringDraft = false

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    try {
        const list = await employeeInterface.verifyList()
        employees.value = list
        if (list.length === 1) {
            selectedEmployeeId.value = list[0].id
        }
        if (route.query.draftId) {
            await restoreDraft(route.query.draftId)
        } else {
            focusSearchField()
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
        return sum + getDirectionMultiplier(item) * item.unitPrice * item.quantity * getItemDiscount(item)
    }, 0)
    return (raw - actual).toFixed(2)
})

const actualTotalAmount = computed(() => {
    return cart.value
        .reduce((sum, item) => {
            return sum + getDirectionMultiplier(item) * item.unitPrice * item.quantity * getItemDiscount(item)
        }, 0)
        .toFixed(2)
})

function getDirectionMultiplier(item) {
    return item.direction === 'OUT' ? -1 : 1
}

function getItemDiscount(item) {
    return item.special ? 1 : item.discount
}

function openEmployeeModal() {
    showEmployeeModal.value = true
}

function selectEmployee(employee) {
    selectedEmployeeId.value = employee.id
    showEmployeeModal.value = false
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
            productCode: sku.productCode || '',
            productName: sku.productName || '',
            skuCode: sku.code,
            skuName: sku.name || sku.code,
            unitPrice: Number(sku.salePrice || 0),
            quantity: 1,
            discount: 1,
            special: Boolean(sku.special),
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

function openConfirmModal(title, message, action, cancelAction = null, options = {}) {
    confirmTitle.value = title
    confirmMessage.value = message
    confirmIcon.value = options.icon || ''
    confirmDetails.value = options.details || []
    confirmButtonText.value = options.confirmText || '确定'
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
    const item = cart.value[index]
    if (item.special) {
        item.discount = 1
        return
    }
    const discount = Number(item.discount)
    if (!Number.isFinite(discount) || discount <= 0) {
        item.discount = 1
        return
    }
    item.discount = Math.min(Number(discount.toFixed(2)), 1)
}

function buildOrderData() {
    const toOrderItem = item => ({
        skuCode: item.skuCode,
        discount: item.special ? 1 : item.discount,
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

// 确认并提交订单
function requestCompleteOrder() {
    if (submitting.value || cart.value.length === 0) return
    if (!selectedEmployeeId.value) {
        toast.warning('请选择本次收银员工')
        return
    }
    const methodLabel = payMethodOptions.find(option => option.value === payMethod.value)?.label || payMethod.value
    openConfirmModal(
        '确认完成收款',
        '',
        completeOrder,
        null,
        {
            icon: 'money',
            confirmText: '确认收款',
            details: [
                {
                    label: '收款方式',
                    value: methodLabel
                },
                {
                    label: '收款金额',
                    value: `¥${actualTotalAmount.value}`,
                    emphasis: true
                },
                {
                    label: '购买数目',
                    value: `${totalQty.value} 件`
                }
            ]
        }
    )
}

async function completeOrder() {
    if (submitting.value || cart.value.length === 0) return
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

// 进入挂单查询页
function openDraftPage() {
    if (cart.value.length === 0) {
        router.push('/checkout/drafts')
        return
    }
    openConfirmModal(
        '打开挂单查询',
        '当前购物车尚未保存，离开后内容会丢失。确定打开挂单查询吗？',
        () => {
            allowRouteLeave = true
            router.push('/checkout/drafts')
        }
    )
}

async function restoreDraft(draftId) {
    restoringDraft = true
    try {
        const detail = await orderInterface.search(draftId)
        cart.value = (detail.items || []).map(item => ({
            productCode: item.productCode || '',
            productName: item.productName || '',
            skuCode: item.skuCode,
            skuName: item.skuName || item.skuCode,
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
        inputDirection.value = 'IN'
        await router.replace({path: '/checkout'})
        focusSearchField()
        if (!draftEmployee) {
            toast.warning('原挂单员工不可用，请重新选择收银员工')
        }
    } catch {
        // 拦截器已处理
    } finally {
        restoringDraft = false
    }
}

onBeforeRouteLeave((to, from, next) => {
    if (restoringDraft || cart.value.length === 0 || allowRouteLeave) {
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
    position: relative;
    width: 80%;
    display: flex;
    flex-direction: column;
    padding: 20px 20px 0 20px;
    overflow: hidden;
    background: #f5faf9;
}

.left-container > .order-item-list {
    min-height: 0;
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
    min-height: 42px;
    padding: 4px 5px 4px 12px;
    background: rgba(255, 255, 255, 0.82);
    border: 1px solid #c9e5e0;
    border-radius: 12px;
    box-shadow: 0 4px 14px rgba(22, 83, 78, 0.08);
}

.employee-label {
    position: relative;
    padding-left: 10px;
    color: #64807e;
    font-size: 12px;
    font-weight: 700;
    white-space: nowrap;
}

.employee-label::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    width: 5px;
    height: 5px;
    background: #14b8a6;
    border-radius: 50%;
    transform: translateY(-50%);
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.selected-employee {
    padding: 6px 10px;
    border: 1px solid #b9ded7;
    border-radius: 8px;
    background: #e9f8f5;
    color: #0f766e;
    font-size: 13px;
    font-weight: 800;
    white-space: nowrap;
}

.employee-empty {
    padding: 6px 10px;
    color: #94a3b8;
    background: #f8fbfa;
    border: 1px dashed #cbdedb;
    border-radius: 8px;
    font-size: 12px;
    white-space: nowrap;
}

.employee-add-button {
    height: 32px;
    padding: 0 12px;
    border: 1px solid #0d9488;
    border-radius: 8px;
    background: #0d9488;
    color: #fff;
    font-size: 12px;
    font-weight: 700;
    box-shadow: 0 3px 9px rgba(13, 148, 136, 0.2);
}

.employee-add-button:hover {
    background: #0f766e;
    border-color: #0f766e;
    transform: translateY(-1px);
    box-shadow: 0 5px 12px rgba(13, 148, 136, 0.28);
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
    padding: 8px;
    background: #ffffff;
    border: 1px solid #b9ded7;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.14);
}

.total-collapse-button {
    position: absolute;
    top: -30px;
    right: 14px;
    z-index: 12;
    min-width: 68px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
    padding: 0 10px;
    color: #fff;
    background: #0f766e;
    border: 1px solid #b9ded7;
    border-bottom: none;
    border-radius: 10px 10px 0 0;
    box-shadow: 0 -3px 10px rgba(15, 118, 110, 0.14);
}

.total-collapse-button span {
    font-size: 12px;
    font-weight: 700;
}

.total-collapse-button:hover {
    color: #fff;
    background: #115e59;
    border-color: #9fd8cf;
}

.order-total-collapsed {
    position: absolute;
    right: 20px;
    bottom: 20px;
    z-index: 10;
    min-width: 190px;
    height: 52px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 0 14px 0 18px;
    color: #fff;
    background: linear-gradient(135deg, #fbbf24, #d97706);
    border: 1px solid #f5b536;
    border-radius: 26px;
    box-shadow: 0 6px 18px rgba(217, 119, 6, 0.25);
}

.order-total-collapsed:hover {
    color: #fff;
    background: linear-gradient(135deg, #f59e0b, #b45309);
    border-color: #d97706;
    transform: translateY(-1px);
    box-shadow: 0 8px 22px rgba(217, 119, 6, 0.32);
}

.order-total-collapsed span {
    font-size: 12px;
    font-weight: 700;
}

.order-total-collapsed strong {
    font-size: 20px;
    font-variant-numeric: tabular-nums;
}

.order-total-collapsed .expand-label {
    padding: 4px 8px;
    color: #fff;
    background: rgba(255, 255, 255, 0.18);
    border-radius: 10px;
    font-size: 11px;
}

.order-total-collapsed:hover .expand-label {
    color: #fff;
    background: rgba(255, 255, 255, 0.16);
}

.total-card-enter-active,
.total-card-leave-active {
    transition: opacity 0.2s ease, transform 0.25s cubic-bezier(0.22, 1, 0.36, 1);
    transform-origin: right bottom;
}

.total-card-enter-from,
.total-card-leave-to {
    opacity: 0;
    transform: translateY(12px) scale(0.88);
}

.total-card-enter-to,
.total-card-leave-from {
    opacity: 1;
    transform: translateY(0) scale(1);
}

.order-info {
    display: flex;
    gap: 8px;
}

.info-card {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 12px 18px;
    border: 1px solid #e1efec;
    border-radius: 10px;
    min-width: 100px;
    background: #f7fbfa;
}

.info-card .info-label {
    font-size: 11px;
    color: #64807e;
    font-weight: 600;
}

.info-card .info-value {
    font-size: 20px;
    font-weight: 800;
    color: #285f5a;
}

.info-value-discount {
    color: #dc2626 !important;
}

.info-value-total {
    color: #d97706 !important;
}

.actual-total {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 13px 16px;
    background: linear-gradient(135deg, #fbbf24, #d97706);
    border: 1px solid #f5b536;
    border-radius: 10px;
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
    padding: 13px;
    margin-top: auto;
    margin-bottom: 8px;
    background: #f7fbfa;
    border: 1px solid #dceae7;
    border-radius: 10px;
}

.payment-heading {
    display: flex;
    align-items: center;
    gap: 7px;
    margin-bottom: 10px;
}

.payment-icon {
    width: 26px;
    height: 26px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #e6f7f4;
    border-radius: 7px;
}

.payment-icon :deep(img) {
    width: 17px;
    height: 17px;
}

.payment-title {
    color: #475569;
    font-size: 12px;
    font-weight: 700;
}

.payment-amount {
    margin-left: auto;
    color: #d97706;
    font-size: 13px;
    font-variant-numeric: tabular-nums;
}

.pay-method-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.pay-method-btn {
    width: calc(50% - 4px);
    min-height: 42px;
    display: flex;
    align-items: center;
    justify-content: flex-start;
    gap: 8px;
    padding: 7px 10px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fff;
    font-size: 13px;
    font-weight: 600;
    color: #475569;
    cursor: pointer;
    transition: all 0.2s;
}

.pay-method-btn img {
    width: 24px;
    height: 24px;
    flex-shrink: 0;
    object-fit: contain;
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
    padding-top: 4px;
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

</style>
