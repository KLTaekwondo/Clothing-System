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

        <ReceiptPreview
            ref="receiptRef"
            :visible="showReceipt"
            :order-no="receiptOrderNo"
            :items="receiptItems"
            :total-amount="receiptTotalAmount"
            :actual-amount="receiptActualAmount"
            :discount-amount="receiptDiscountAmount"
            :employee-name="receiptEmployeeName"
            :warehouse-name="receiptWarehouseName"
            :pay-method="receiptPayMethod"
            :member-phone="receiptMemberPhone"
            :remark="receiptRemark"
            :shop-name="receiptShopName"
            @close="closeReceipt"
        />

        <div class="right-container">
            <div class="member-panel">
                <div class="member-panel-heading">
                    <span class="member-panel-icon"><IconGraphic name="user"/></span>
                    <span class="member-panel-title">当前会员</span>
                    <button
                        v-if="selectedMember"
                        class="member-clear-button"
                        type="button"
                        @click="clearMember"
                    >清除</button>
                </div>
                <div v-if="selectedMember" class="member-selected">
                    <div class="member-selected-main">
                        <strong>{{ selectedMember.memberName }}</strong>
                        <span>{{ memberLevelLabels[selectedMember.memberLevel] || selectedMember.memberLevel }}</span>
                    </div>
                    <code>{{ selectedMember.memberPhone }}</code>
                    <div class="member-selected-meta">
                        <span class="member-discount-value">{{ formatMemberDiscount(selectedMember.memberDiscount) }}</span>
                        <span>{{ selectedMember.memberPoints ?? 0 }} 积分</span>
                    </div>
                    <button
                        class="member-change-button"
                        type="button"
                        @click="focusMemberSearch"
                    >更换会员</button>
                </div>
                <div v-else class="member-search-control">
                    <span class="member-empty-text">暂未记录会员</span>
                    <div class="member-search-row">
                        <input
                            ref="memberSearchField"
                            v-model="memberPhoneQuery"
                            :disabled="memberSearching"
                            maxlength="11"
                            placeholder="输入手机号"
                            type="tel"
                            @keydown.enter.prevent="searchMember"
                        />
                        <button
                            :disabled="memberSearching || !memberPhoneQuery.trim()"
                            type="button"
                            @click="searchMember"
                        >{{ memberSearching ? '查询中' : '查询' }}</button>
                    </div>
                </div>
            </div>

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
                    :disabled="submitting"
                    class="tool-bar-item"
                    @click="openOrderPage"
                >
                    <span class="tool-icon"><IconGraphic name="order"/></span>
                    <span class="tool-label">订单查询</span>
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
                <button
                    class="tool-bar-item tool-bar-item-danger"
                    @click="handleShiftChange"
                >
                    <span class="tool-icon"><IconGraphic name="clock"/></span>
                    <span class="tool-label">交班</span>
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
import memberInterface from '../../axios/interface/MemberInterface.js'
import orderInterface from '../../axios/interface/OrderInterface.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import {MEMBER_LEVEL_LABELS} from '../../constants/memberLevel.js'
import {PAY_METHOD_OPTIONS} from '../../constants/payMethod.js'
import cashIcon from '../../assets/icons/cash.svg'
import bankCardIcon from '../../assets/icons/bank-card.svg'
import alipayIcon from '../../assets/icons/alipay.svg'
import wechatIcon from '../../assets/icons/wechat.svg'
import IconGraphic from '../../component/IconGraphic.vue'
import CheckoutCart from '../../component/checkout/CheckoutCart.vue'
import CheckoutConfirmModal from '../../component/checkout/CheckoutConfirmModal.vue'
import EmployeeSelectModal from '../../component/checkout/EmployeeSelectModal.vue'
import SkuSelectModal from '../../component/checkout/SkuSelectModal.vue'
import ReceiptPreview from '../../component/checkout/ReceiptPreview.vue'

const toast = useToastStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const payMethodOptions = PAY_METHOD_OPTIONS
const memberLevelLabels = MEMBER_LEVEL_LABELS
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

// 当前订单会员
const selectedMember = ref(null)
const memberPhoneQuery = ref('')
const memberSearching = ref(false)
const memberSearchField = ref(null)

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

// 小票打印
const showReceipt = ref(false)
const receiptOrderNo = ref('')
const receiptItems = ref([])
const receiptTotalAmount = ref(0)
const receiptActualAmount = ref(0)
const receiptDiscountAmount = ref(0)
const receiptEmployeeName = ref('')
const receiptWarehouseName = ref('')
const receiptPayMethod = ref('')
const receiptMemberPhone = ref('')
const receiptRemark = ref('')
const receiptShopName = ref('')
const receiptShopSlogan = ref('')
const receiptRef = ref(null)

function closeReceipt() {
    showReceipt.value = false
    resetCurrentOrder()
}

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

function getMemberDiscount() {
    const discount = Number(selectedMember.value?.memberDiscount)
    if (!Number.isFinite(discount) || discount <= 0) return 1
    return Math.min(Number(discount.toFixed(2)), 1)
}

function applyMemberDiscount() {
    const discount = getMemberDiscount()
    cart.value.forEach(item => {
        if (!item.special && !item.manualDiscount) {
            item.discount = discount
        }
    })
}

async function searchMember() {
    const phone = memberPhoneQuery.value.trim()
    if (memberSearching.value || !/^1[3456789]\d{9}$/.test(phone)) {
        toast.warning('请输入正确的会员手机号')
        return
    }
    memberSearching.value = true
    try {
        const member = await memberInterface.searchMember(phone)
        if (!member?.id) {
            toast.warning('未找到该会员')
            return
        }
        selectedMember.value = member
        memberPhoneQuery.value = member.memberPhone || phone
        applyMemberDiscount()
        toast.success(`已记录会员「${member.memberName || phone}」`)
    } catch {
        // 请求错误由 Axios 拦截器统一提示
    } finally {
        memberSearching.value = false
    }
}

function clearMember() {
    selectedMember.value = null
    memberPhoneQuery.value = ''
    applyMemberDiscount()
    focusSearchField()
}

function focusMemberSearch() {
    selectedMember.value = null
    memberPhoneQuery.value = ''
    applyMemberDiscount()
    nextTick(() => memberSearchField.value?.focus())
}

function formatMemberDiscount(value) {
    const discount = Number(value)
    if (!Number.isFinite(discount)) return '-'
    return `${(discount * 10).toFixed(1)} 折`
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
        const special = Boolean(sku.special)
        cart.value.push({
            productCode: sku.productCode || '',
            productName: sku.productName || '',
            skuCode: sku.code,
            skuName: sku.name || sku.code,
            unitPrice: Number(sku.salePrice || 0),
            quantity: 1,
            discount: special ? 1 : getMemberDiscount(),
            special,
            manualDiscount: false,
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

// 交班：形式上结算本班，实质为退出收银登录
function handleShiftChange() {
    const cartQty = cart.value.reduce((sum, item) => sum + item.quantity, 0)
    openConfirmModal(
        '交班',
        cartQty > 0
            ? `当前购物车还有 ${cartQty} 件商品未结账，交班后将丢失。确定交班吗？`
            : '确定结束当班并退出收银吗？',
        async () => {
            allowRouteLeave = true
            try {
                await wareHouseInterface.logout()
            } catch {
                // 后端会话可能已失效，继续清理本地状态
            }
            userStore.logout()
            router.push('/')
        },
        null,
        {confirmText: '确认交班'}
    )
}

function resetCurrentOrder() {
    cart.value = []
    currentDraftOrderId.value = null
    selectedMember.value = null
    memberPhoneQuery.value = ''
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
        item.manualDiscount = true
        return
    }
    item.discount = Math.min(Number(discount.toFixed(2)), 1)
    item.manualDiscount = true
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
        memberPhone: selectedMember.value?.memberPhone || null,
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
        const orderNo = await orderInterface.complete(buildOrderData())
        // 保存小票数据（在清空前保存当前状态）
        const employeeName = employees.value.find(e => e.id === selectedEmployeeId.value)?.name || ''
        const warehouseName = userStore.userInfo?.name || ''
        const payMethodLabel = payMethodOptions.find(o => o.value === payMethod.value)?.label || payMethod.value
        receiptOrderNo.value = orderNo || ''
        receiptItems.value = cart.value.map(item => ({...item}))
        receiptTotalAmount.value = Number(totalAmount.value)
        receiptActualAmount.value = Number(actualTotalAmount.value)
        receiptDiscountAmount.value = Number(discountAmount.value)
        receiptEmployeeName.value = employeeName
        receiptWarehouseName.value = warehouseName
        receiptShopName.value = userStore.userInfo?.name || ''
        receiptPayMethod.value = payMethodLabel
        receiptMemberPhone.value = selectedMember.value?.memberPhone || null
        receiptRemark.value = ''
        // 先让小票渲染出来
        showReceipt.value = true
        await nextTick()
        // 自动调出打印对话框（使用微任务保持执行上下文）
        receiptRef.value?.print()
        // 监听打印结束，自动关闭小票并重置
        window.addEventListener('afterprint', autoResetAfterPrint, { once: true })
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

function autoResetAfterPrint() {
    closeReceipt()
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

// 进入订单查询页
function openOrderPage() {
    if (cart.value.length === 0) {
        router.push('/checkout/orders')
        return
    }
    openConfirmModal(
        '打开订单查询',
        '当前购物车尚未保存，离开后内容会丢失。确定打开订单查询吗？',
        () => {
            allowRouteLeave = true
            router.push('/checkout/orders')
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
            discount: item.special ? 1 : Number(item.discount ?? 1),
            special: Boolean(item.special),
            manualDiscount: true,
            direction: item.direction === 'OUT' ? 'OUT' : 'IN'
        }))
        currentDraftOrderId.value = detail.id
        payMethod.value = detail.payMethod || payMethodOptions[0].value
        const draftEmployee = employees.value.find(employee => {
            return employee.name === detail.employeeName
        })
        selectedEmployeeId.value = draftEmployee?.id || ''
        if (detail.memberPhone) {
            try {
                const restoredMember = await memberInterface.searchMember(detail.memberPhone)
                if (!restoredMember?.id) throw new Error('member-not-found')
                selectedMember.value = restoredMember
                memberPhoneQuery.value = detail.memberPhone
                applyMemberDiscount()
            } catch {
                selectedMember.value = null
                memberPhoneQuery.value = ''
                toast.warning('原挂单会员不可用，请重新选择会员')
            }
        } else {
            selectedMember.value = null
            memberPhoneQuery.value = ''
        }
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
    background: var(--bg-body);
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
    color: var(--text-invert);
    font-size: 13px;
    font-weight: 800;
    cursor: pointer;
}

.mode-sale {
    background: var(--primary);
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
    border: 1px solid var(--border);
    border-radius: 10px;
    background: var(--bg-card);
    font-size: 15px;
    outline: none;
}

.search-field:focus {
    border-color: var(--primary);
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
    border: 1px solid var(--border-hover);
    border-radius: 12px;
    box-shadow: 0 4px 14px rgba(22, 83, 78, 0.08);
}

.employee-label {
    position: relative;
    padding-left: 10px;
    color: var(--text-secondary);
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
    background: var(--primary-hover);
    border-radius: 50%;
    transform: translateY(-50%);
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.selected-employee {
    padding: 6px 10px;
    border: 1px solid var(--border-hover);
    border-radius: 8px;
    background: var(--bg-hover);
    color: var(--primary-dark);
    font-size: 13px;
    font-weight: 800;
    white-space: nowrap;
}

.employee-empty {
    padding: 6px 10px;
    color: var(--text-muted);
    background: var(--bg-subtle);
    border: 1px dashed #cbdedb;
    border-radius: 8px;
    font-size: 12px;
    white-space: nowrap;
}

.employee-add-button {
    height: 32px;
    padding: 0 12px;
    border: 1px solid var(--primary);
    border-radius: 8px;
    background: var(--primary);
    color: var(--text-invert);
    font-size: 12px;
    font-weight: 700;
    box-shadow: 0 3px 9px rgba(13, 148, 136, 0.2);
}

.employee-add-button:hover {
    background: var(--primary-dark);
    border-color: var(--primary-dark);
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
    background: var(--bg-card);
    border: 1px solid var(--border-hover);
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
    color: var(--text-invert);
    background: var(--primary-dark);
    border: 1px solid var(--border-hover);
    border-bottom: none;
    border-radius: 10px 10px 0 0;
    box-shadow: 0 -3px 10px rgba(15, 118, 110, 0.14);
}

.total-collapse-button span {
    font-size: 12px;
    font-weight: 700;
}

.total-collapse-button:hover {
    color: var(--text-invert);
    background: var(--primary-dark);
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
    color: var(--text-invert);
    background: linear-gradient(135deg, #fbbf24, #d97706);
    border: 1px solid #f5b536;
    border-radius: 26px;
    box-shadow: 0 6px 18px rgba(217, 119, 6, 0.25);
}

.order-total-collapsed:hover {
    color: var(--text-invert);
    background: linear-gradient(135deg, #f59e0b, #b45309);
    border-color: var(--warning-dark);
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
    color: var(--text-invert);
    background: rgba(255, 255, 255, 0.18);
    border-radius: 10px;
    font-size: 11px;
}

.order-total-collapsed:hover .expand-label {
    color: var(--text-invert);
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
    background: var(--bg-subtle);
}

.info-card .info-label {
    font-size: 11px;
    color: var(--text-secondary);
    font-weight: 600;
}

.info-card .info-value {
    font-size: 20px;
    font-weight: 800;
    color: var(--primary-dark);
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
    color: var(--text-invert);
}

/* ── 右侧容器 ── */
.right-container {
    width: 20%;
    display: flex;
    flex-direction: column;
    padding: 20px;
    background: var(--bg-card);
    border-left: 1px solid #e3efed;
}

.member-panel {
    width: 100%;
    margin-bottom: 16px;
    padding: 14px;
    color: var(--text-invert);
    background: linear-gradient(145deg, #0f766e, #14b8a6);
    border: 1px solid var(--primary);
    border-radius: 12px;
    box-shadow: 0 8px 22px rgba(13, 148, 136, 0.2);
}

.member-panel-heading {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
}

.member-panel-icon {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 5px;
    background: rgba(255, 255, 255, 0.18);
    border-radius: 8px;
}

.member-panel-icon :deep(img) {
    width: 100%;
    height: 100%;
    filter: brightness(0) invert(1);
}

.member-panel-title {
    font-size: 15px;
    font-weight: 800;
}

.member-clear-button {
    margin-left: auto;
    padding: 5px 9px;
    color: var(--text-invert);
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.55);
    border-radius: 6px;
    font-size: 13px;
    font-weight: 700;
}

.member-selected {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.member-selected-main,
.member-selected-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
}

.member-selected-main strong {
    overflow: hidden;
    font-size: 16px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.member-selected-main span {
    flex-shrink: 0;
    padding: 3px 7px;
    color: var(--primary-dark);
    background: var(--bg-active);
    border-radius: 6px;
    font-size: 13px;
    font-weight: 800;
}

.member-selected code {
    color: var(--text-invert);
    font-size: 14px;
    font-weight: 700;
}

.member-selected-meta {
    color: var(--text-invert);
    font-size: 14px;
    font-weight: 600;
}

.member-discount-value {
    color: #facc15;
    font-weight: 800;
}

.member-change-button {
    width: 100%;
    height: 36px;
    margin-top: 2px;
    color: var(--primary-dark);
    background: var(--bg-card);
    border: none;
    border-radius: 7px;
    font-size: 14px;
    font-weight: 700;
}

.member-search-control {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.member-empty-text {
    color: var(--text-invert);
    font-size: 14px;
    font-weight: 600;
}

.member-search-row {
    display: flex;
    align-items: stretch;
}

.member-search-row input {
    width: calc(100% - 66px);
    height: 40px;
    padding: 0 10px;
    color: #134e4a;
    background: var(--bg-card);
    border: none;
    border-radius: 8px 0 0 8px;
    font-size: 14px;
}

.member-search-row button {
    width: 66px;
    border: none;
    color: var(--text-invert);
    background: #134e4a;
    border-radius: 0 8px 8px 0;
    font-size: 14px;
    font-weight: 700;
}

.member-search-row button:disabled {
    cursor: not-allowed;
    opacity: 0.65;
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
    border: 1px solid var(--border-light);
    border-radius: 12px;
    background: var(--bg-subtle);
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
    background: var(--error-light);
    cursor: pointer;
}

.tool-bar-item:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.tool-bar-item:hover {
    background: var(--bg-hover);
    border-color: var(--primary);
}

.tool-bar-item-danger:hover {
    background: var(--error-light);
    border-color: var(--error);
}

.tool-icon {
    font-size: 24px;
}

.tool-label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
}

.tool-bar-item-active .tool-label {
    color: #b91c1c;
}

/* 支付方式 */
.payment-panel {
    padding: 13px;
    margin-top: auto;
    margin-bottom: 8px;
    background: var(--bg-subtle);
    border: 1px solid var(--border);
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
    color: var(--text-secondary);
    font-size: 12px;
    font-weight: 700;
}

.payment-amount {
    margin-left: auto;
    color: var(--warning-dark);
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
    border: 1px solid var(--border);
    border-radius: 8px;
    background: var(--bg-card);
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
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
    border-color: var(--primary);
    color: var(--primary);
}

.pay-method-btn.active {
    background: var(--bg-active);
    border-color: var(--primary);
    color: var(--primary);
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
    color: var(--text-invert);
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
