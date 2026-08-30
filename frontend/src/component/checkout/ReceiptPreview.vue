<template>
    <!-- Teleport 到 body：打印时 #app 整体隐藏，小票作为唯一内容独立成页 -->
    <Teleport to="body">
        <div
            v-if="visible"
            class="receipt-overlay"
            @click.self="handleOverlayClick"
        >
        <div class="receipt-controls">
            <button
                :disabled="printing"
                class="controls-btn controls-print"
                @click="printReceipt"
            >{{ printing ? '打印中...' : '🖨️ 打印小票' }}</button>
            <button
                class="controls-btn controls-close"
                @click="$emit('close')"
            >✕ 关闭</button>
        </div>

        <div
            id="receipt-content"
            class="receipt-paper"
        >
            <!-- 店头 -->
            <div class="receipt-header">
                <div class="shop-name">{{ shopName }}</div>
                <div
                    v-if="shopSlogan"
                    class="shop-slogan"
                >{{ shopSlogan }}</div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 订单信息 -->
            <div class="order-meta">
                <div class="meta-row">
                    <span class="meta-label">订单编号</span>
                    <span class="meta-value code">{{ orderNo }}</span>
                </div>
                <div class="meta-row">
                    <span class="meta-label">收银员</span>
                    <span class="meta-value">{{ employeeName }}</span>
                </div>
                <div class="meta-row">
                    <span class="meta-label">门店</span>
                    <span class="meta-value">{{ warehouseName }}</span>
                </div>
                <div class="meta-row">
                    <span class="meta-label">时间</span>
                    <span class="meta-value">{{ printTime }}</span>
                </div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 商品明细表头 -->
            <div class="items-header">
                <span class="col-name">商品</span>
                <span class="col-price">单价</span>
                <span class="col-qty">数量</span>
                <span class="col-discount">折扣</span>
                <span class="col-total">小计</span>
            </div>

            <div class="receipt-divider-light">─────────────────────────</div>

            <!-- 销售商品 -->
            <div
                v-for="(item, index) in saleItems"
                :key="'sale-' + index"
                class="item-row"
            >
                <div class="item-name-row">
                    <span class="item-direction sale">售</span>
                    <span class="item-name">{{ item.productName || item.skuName }}</span>
                </div>
                <div class="item-numbers">
                    <span class="col-name"></span>
                    <span class="col-price">¥{{ formatPrice(item.unitPrice) }}</span>
                    <span class="col-qty">{{ item.quantity }}</span>
                    <span class="col-discount">{{ item.special ? '特价' : (item.discount * 100).toFixed(0) + '%' }}</span>
                    <span class="col-total">¥{{ formatPrice(item.unitPrice * item.quantity * getItemDiscount(item)) }}</span>
                </div>
                <div
                    v-if="item.skuCode"
                    class="item-sku-code"
                >货号: {{ item.skuCode }}</div>
            </div>

            <!-- 退货商品 -->
            <div
                v-for="(item, index) in refundItems"
                :key="'refund-' + index"
                class="item-row item-refund"
            >
                <div class="item-name-row">
                    <span class="item-direction refund">退</span>
                    <span class="item-name">{{ item.productName || item.skuName }}</span>
                </div>
                <div class="item-numbers">
                    <span class="col-name"></span>
                    <span class="col-price">¥{{ formatPrice(item.unitPrice) }}</span>
                    <span class="col-qty">-{{ item.quantity }}</span>
                    <span class="col-discount">{{ item.special ? '特价' : (item.discount * 100).toFixed(0) + '%' }}</span>
                    <span class="col-total">-¥{{ formatPrice(item.unitPrice * item.quantity * getItemDiscount(item)) }}</span>
                </div>
                <div
                    v-if="item.skuCode"
                    class="item-sku-code"
                >货号: {{ item.skuCode }}</div>
            </div>
<!-- 数量汇总 -->
            <div class="receipt-divider-light">─────────────────────────</div>
            <div class="qty-summary">
                销售 {{ saleQty }} 件
                <template v-if="refundQty > 0">
                    ，退货 {{ refundQty }} 件
                </template>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 金额汇总 -->
            <div class="amount-section">
                <div class="amount-row">
                    <span>总金额</span>
                    <span>¥{{ formatPrice(totalAmount) }}</span>
                </div>
                <div class="amount-row">
                    <span>优惠</span>
                    <span>-¥{{ formatPrice(discountAmount) }}</span>
                </div>
                <div class="amount-row amount-row-final">
                    <span>实付金额</span>
                    <span class="final-price">¥{{ formatPrice(actualAmount) }}</span>
                </div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 支付和会员 -->
            <div class="payment-row">
                <span>支付方式</span>
                <span>{{ payMethodLabel }}</span>
            </div>
            <div
                v-if="memberPhone"
                class="payment-row"
            >
                <span>会员</span>
                <span>{{ memberPhone }}</span>
            </div>

            <div
                v-if="remark"
                class="remark-row"
            >
                <div class="remark-label">备注</div>
                <div>{{ remark }}</div>
            </div>

            <div class="receipt-divider">━━━━━━━━━━━━━━━━━━━━━━━━━</div>

            <!-- 结尾 -->
            <div class="receipt-footer">
                <div class="footer-thanks">感谢您的光临！</div>
                <div class="footer-note">退换货请保留此小票</div>
            </div>
        </div>
        </div>
    </Teleport>
</template>
<script setup>
import {computed, onMounted, ref} from 'vue'
import {useToastStore} from '../../stores/toastStore.js'
import {PAY_METHOD_LABELS} from '../../constants/payMethod.js'

const toast = useToastStore()

const props = defineProps({
    visible: Boolean,
    orderNo: {type: String, default: ''},
    items: {type: Array, default: () => []},
    totalAmount: {type: Number, default: 0},
    actualAmount: {type: Number, default: 0},
    discountAmount: {type: Number, default: 0},
    employeeName: {type: String, default: ''},
    warehouseName: {type: String, default: ''},
    payMethod: {type: String, default: ''},
    memberPhone: {type: String, default: ''},
    remark: {type: String, default: ''},
    shopName: {type: String, default: ''},
    shopSlogan: {type: String, default: ''},
})

defineEmits(['close', 'print-done'])

const payMethodLabels = PAY_METHOD_LABELS
const printTime = ref('')

onMounted(() => {
    const now = new Date()
    const pad = n => String(n).padStart(2, '0')
    printTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
})

const payMethodLabel = computed(() => {
    return payMethodLabels[props.payMethod] || props.payMethod || '-'
})

const saleItems = computed(() => {
    return props.items.filter(item => item.direction !== 'OUT')
})

const refundItems = computed(() => {
    return props.items.filter(item => item.direction === 'OUT')
})

const saleQty = computed(() => {
    return saleItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const refundQty = computed(() => {
    return refundItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

function formatPrice(val) {
    return Number(val || 0).toFixed(2)
}

function getItemDiscount(item) {
    return item.special ? 1 : Number(item.discount || 1)
}

const printing = ref(false)

// 小票打印：C-Lodop SEND_PRINT_RAWDATA 裸流直发（最终通道）
// 不走 GDI（无 210mm 前置空白）；GBK 编码由本机 C-Lodop 服务完成（前端零编码）
function printReceipt() {
    console.log('[lodop] printReceipt 触发')
    lodopPrint(buildLines())
}

// 获取 C-Lodop 打印对象：兼容入口链
// C-Lodop 6.6.x 的官方脚本只定义 getCLodop / window.CLODOP（getLodop 是旧版 Lodop 的入口）
function getLodopObj() {
    if (typeof getLodop === 'function') return getLodop()
    if (typeof getCLodop === 'function') return getCLodop()
    if (window.CLODOP) return window.CLODOP
    if (window.LODOP) return window.LODOP
    return null
}

// 任意一个 C-Lodop 入口已就绪
function lodopReady() {
    return typeof getLodop === 'function'
        || typeof getCLodop === 'function'
        || !!window.CLODOP
        || !!window.LODOP
}

// 加载本机 C-Lodop 接口：自动多地址尝试 + 等待入口就绪（最多 3 秒）
function loadClodopFuncs() {
    return new Promise((resolve, reject) => {
        const waitLodop = (maxWait, done) => {
            console.log('[lodop] 探测全局: getLodop=', typeof getLodop,
                'getCLodop=', typeof getCLodop,
                'CLODOP=', typeof window.CLODOP,
                'LODOP=', typeof window.LODOP)
            if (lodopReady()) {
                done(true)
                return
            }
            const t0 = Date.now()
            const timer = setInterval(() => {
                if (lodopReady()) {
                    clearInterval(timer)
                    console.log('[lodop] 轮询等待到位')
                    done(true)
                    return
                }
                if (Date.now() - t0 > maxWait) {
                    clearInterval(timer)
                    done(false)
                }
            }, 200)
        }
        if (lodopReady()) {
            console.log('[lodop] C-Lodop 入口已存在，跳过加载')
            resolve()
            return
        }
        // 官方集成多候选地址：按顺序尝试
        const candidates = [
            'http://localhost:8000/CLodopfuncs.js',
            'http://127.0.0.1:8000/CLodopfuncs.js',
            'https://localhost:8000/CLodopfuncs.js'
        ]
        let index = 0
        const tryNext = () => {
            if (index >= candidates.length) {
                console.log('[lodop] 全部候选地址加载失败')
                reject(new Error('CLODOP_LOAD_FAIL'))
                return
            }
            const script = document.createElement('script')
            script.src = candidates[index]
            index += 1
            script.onload = () => {
                console.log('[lodop] 接口脚本加载成功:', script.src)
                // 脚本就绪后等待入口可用
                waitLodop(3000, ok => {
                    console.log('[lodop] 等待结果:', ok ? 'C-Lodop 就绪' : '3秒后仍未就绪')
                    if (ok) {
                        resolve()
                    } else {
                        script.remove()
                        tryNext()
                    }
                })
            }
            script.onerror = () => {
                console.log('[lodop] 接口脚本加载失败:', script.src)
                script.remove()
                tryNext()
            }
            document.head.appendChild(script)
        }
        tryNext()
    })
}

async function lodopPrint(lines) {
    console.log('[lodop] lodopPrint 开始, 行数:', lines.length)
    try {
        await loadClodopFuncs()
    } catch {
        console.log('[lodop] loadClodopFuncs 抛出异常')
        toast.error('无法加载 C-Lodop 接口：请确认本机已安装并运行 C-Lodop（端口 8000）')
        return
    }
    const LODOP = getLodopObj()
    if (!LODOP) {
        console.log('[lodop] 打印对象获取失败')
        toast.error('C-Lodop 连接失败，请确认本地服务已启动')
        return
    }
    console.log('[lodop] 打印对象获取成功, 默认打印机:', LODOP.GET_PRINTER_NAME(-1))
    // 拼装 ESC/POS 裸流（行文本已手动对齐；纯文本最小指令集）
    let buf = ''
    buf += '\x1B\x40' // 打印机初始化
    buf += '\x1C\x26' // 芯烨开启中文模式（FS &）
    for (const line of lines) {
        buf += (line.text || '') + '\r\n'
    }
    // 结尾仅留 1 行换行（手撕余量）。
    // ⚠️ 不加 GS V 切纸指令：无刀手动撕纸机型上，GS V 会"走纸到切刀位置"，
    // 即使没刀也会强制送出一大截白纸（小票内容被送到刀口位置）——这是空白纸的根源之一
    buf += '\r\n'
    console.log('[lodop] buf 长度:', buf.length, '前60字符:', JSON.stringify(buf.slice(0, 60)))
    LODOP.PRINT_INIT('小票打印')
    // 编码由本机 C-Lodop 转 GBK（浏览器不参与编码）
    LODOP.SET_PRINT_MODE('SEND_RAW_DATA_ENCODE', 'GBK')
    console.log('[lodop] SET_PRINT_MODE 完成')
    LODOP.SEND_PRINT_RAWDATA(buf)
    console.log('[lodop] SEND_PRINT_RAWDATA 完成')
    LODOP.PRINT()
    console.log('[lodop] PRINT() 已调用')
    window.dispatchEvent(new Event('afterprint'))
}

// 小票内容 → 打印行（57mm 纸：一行最多 32 个半角字符，超宽会被打印机硬换行错位）
// 对齐用手动空格填充（打印机对齐命令在部分固件上失效，空格任何固件都认）
function buildLines() {
    const lines = []
    const row = (text, options = {}) => lines.push({text: text || '', align: 'left', ...options})
    // 分隔线用 *（ASCII 减号 - 笔画太细，热敏打印几乎看不清；* 点阵最实）
    const rule = () => '*'.repeat(32)

    // 店头
    row(center(fitText(props.shopName, 30), 32), {bold: true})
    if (props.shopSlogan) row(center(fitText(props.shopSlogan, 32), 32))
    row('')
    row(rule())
    // 订单信息（"标签: " 占 12 半角，值最多 20）
    row(`订单编号: ${fitText(props.orderNo, 20)}`)
    row(`收银员: ${fitText(props.employeeName, 20)}`)
    row(`门店: ${fitText(props.warehouseName, 20)}`)
    row(`时间: ${printTime.value}`)
    row(rule())
    // 商品表头
    row(pad('品名', 12) + pad('数量', 6) + pad('折扣', 4) + rpad('金额', 8), {bold: true})
    // 商品明细（一行一商品：品名/数量/折扣/金额，列宽与表头对齐）
    saleItems.value.forEach(item => {
        const discount = item.special ? '特价' : `${Math.round(Number(item.discount || 1) * 100)}%`
        row(pad(fitText(item.productName || item.skuName, 12), 12)
            + pad(`x${item.quantity}件`, 6)
            + pad(discount, 4)
            + rpad(`${formatPrice(itemSubtotal(item))}元`, 8))
        if (item.skuCode) row(fitText(`  货号 ${item.skuCode}`, 26))
    })
    refundItems.value.forEach(item => {
        row(pad(fitText(`退${item.productName || item.skuName}`, 12), 12)
            + pad(`x${item.quantity}件`, 6)
            + pad('退货', 4)
            + rpad(`${formatPrice(itemSubtotal(item))}元`, 8))
        if (item.skuCode) row(fitText(`  货号 ${item.skuCode}`, 26))
    })
    row(rule())
    row(center(fitText(`销售 ${saleQty.value} 件${refundQty.value > 0 ? `，退货 ${refundQty.value} 件` : ''}`, 32), 32))
    row(rule())
    // 金额（用"元"：小票固件字库无 ¥ 符号）
    row(`总金额    ${formatPrice(props.totalAmount)}元`)
    if (Number(props.discountAmount) > 0) {
        row(`优惠      -${formatPrice(props.discountAmount)}元`)
    }
    row(`实付金额  ${formatPrice(props.actualAmount)}元`, {bold: true})
    row(rule())
    // 支付/会员/备注
    row(`支付方式: ${fitText(payMethodLabel.value, 20)}`)
    if (props.memberPhone) row(`会员: ${fitText(props.memberPhone, 20)}`)
    if (props.remark) row(fitText(`备注: ${props.remark}`, 32))
    row(rule())
    // 结尾
    row(center('感谢您的光临！', 32), {bold: true})
    row(center('退换货请保留此小票', 32))
    return lines
}

// 左侧补空格到指定显示宽度（中文按 2 列）
function pad(text, width) {
    text = String(text || '')
    return text + ' '.repeat(Math.max(0, width - displayWidth(text)))
}

// 右侧补空格到指定显示宽度
function rpad(text, width) {
    text = String(text || '')
    return ' '.repeat(Math.max(0, width - displayWidth(text))) + text
}

// 居中：两侧补空格
function center(text, width) {
    text = String(text || '')
    const left = Math.max(0, Math.floor((width - displayWidth(text)) / 2))
    return ' '.repeat(left) + text
}

// 显示宽度（中文/全角按 2 列）
function displayWidth(text) {
    let width = 0
    for (const ch of String(text || '')) {
        width += ch.charCodeAt(0) > 255 ? 2 : 1
    }
    return width
}

function itemSubtotal(item) {
    return Number(item.unitPrice || 0) * Number(item.quantity || 0) * getItemDiscount(item)
}

// 按显示宽度截断（中文按 2 个半角字符）
function fitText(text, maxWidth) {
    let width = 0
    let out = ''
    for (const ch of String(text || '')) {
        const w = ch.charCodeAt(0) > 255 ? 2 : 1
        if (width + w > maxWidth) break
        out += ch
        width += w
    }
    return out
}

defineExpose({ print: printReceipt })

function handleOverlayClick() {
    // 防止误点击关闭
}
</script>

<style scoped>
/* ============ 屏幕预览样式 ============ */
.receipt-overlay {
    position: fixed;
    inset: 0;
    z-index: 2000;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding-top: 24px;
    background: rgba(0, 0, 0, 0.45);
    overflow-y: auto;
}

.receipt-controls {
    position: sticky;
    top: 0;
    z-index: 10;
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
    padding: 10px 20px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.controls-btn {
    min-width: 120px;
    height: 42px;
    padding: 0 18px;
    border: none;
    border-radius: 8px;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s;
}

.controls-print {
    background: #0d9488;
    color: #fff;
}

.controls-print:hover {
    background: #0f766e;
}

.controls-close {
    background: #f1f5f9;
    color: #475569;
}

.controls-close:hover {
    background: #e2e8f0;
}

/* ============ 小票纸张 ============ */
.receipt-paper {
    width: 300px;
    padding: 16px 18px;
    margin-bottom: 40px;
    background: #fff;
    font-family: 'Courier New', Courier, monospace;
    font-size: 12px;
    line-height: 1.5;
    color: #1e293b;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
    border-radius: 4px;
}

.receipt-header {
    text-align: center;
    margin-bottom: 8px;
}

.shop-name {
    font-size: 18px;
    font-weight: 800;
    letter-spacing: 2px;
    color: #0f172a;
}

.shop-slogan {
    font-size: 11px;
    color: #64748b;
    margin-top: 2px;
}

.receipt-divider {
    text-align: center;
    font-size: 10px;
    color: #94a3b8;
    letter-spacing: 1px;
    margin: 6px 0;
}

.receipt-divider-light {
    text-align: center;
    font-size: 10px;
    color: #cbd5e1;
    letter-spacing: 1px;
    margin: 4px 0;
}

/* 订单信息 */
.order-meta {
    margin: 4px 0;
}

.meta-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 22px;
}

.meta-label {
    color: #64748b;
}

.meta-value {
    font-weight: 600;
    text-align: right;
}

.meta-value.code {
    font-size: 11px;
    max-width: 170px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 商品表头 */
.items-header {
    display: flex;
    align-items: center;
    font-weight: 700;
    font-size: 11px;
    color: #475569;
    padding: 2px 0;
}

.col-name {
    flex: 1;
    text-align: left;
}

.col-price {
    width: 52px;
    text-align: right;
}

.col-qty {
    width: 36px;
    text-align: center;
}

.col-discount {
    width: 42px;
    text-align: center;
}

.col-total {
    width: 60px;
    text-align: right;
}

/* 商品行 */
.item-row {
    padding: 4px 0;
    border-bottom: 1px dashed #e2e8f0;
}

.item-row.item-refund {
    background: #fef2f2;
    margin: 0 -18px;
    padding: 4px 18px;
}

.item-name-row {
    display: flex;
    align-items: center;
    gap: 4px;
}

.item-direction {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    border-radius: 3px;
    font-size: 10px;
    font-weight: 800;
    flex-shrink: 0;
}

.item-direction.sale {
    background: #dcfce7;
    color: #16a34a;
}

.item-direction.refund {
    background: #fee2e2;
    color: #dc2626;
}

.item-name {
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
}

.item-numbers {
    display: flex;
    align-items: center;
    margin-top: 2px;
    font-size: 11px;
}

.item-numbers .col-price,
.item-numbers .col-qty,
.item-numbers .col-discount,
.item-numbers .col-total {
    font-weight: 600;
}

.item-sku-code {
    font-size: 10px;
    color: #94a3b8;
    padding-left: 22px;
}

/* 数量汇总 */
.qty-summary {
    text-align: center;
    color: #64748b;
    font-size: 11px;
    padding: 2px 0;
}

/* 金额 */
.amount-section {
    padding: 4px 0;
}

.amount-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 24px;
    font-size: 13px;
}

.amount-row-final {
    border-top: 2px solid #1e293b;
    margin-top: 4px;
    padding-top: 6px;
    font-size: 15px;
    font-weight: 800;
}

.final-price {
    color: #d97706;
    font-size: 18px;
}

/* 支付 */
.payment-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 22px;
    font-size: 12px;
}

.payment-row span:last-child {
    font-weight: 700;
}

/* 备注 */
.remark-row {
    font-size: 11px;
    color: #64748b;
    margin-top: 4px;
}

.remark-label {
    font-weight: 700;
    color: #475569;
}

/* 结尾 */
.receipt-footer {
    text-align: center;
    padding: 8px 0 4px;
}

.footer-thanks {
    font-size: 14px;
    font-weight: 800;
    color: #0f766e;
    letter-spacing: 1px;
}

.footer-note {
    font-size: 10px;
    color: #94a3b8;
    margin-top: 4px;
}
</style>

<style>
/* ============ 打印样式（全局生效） ============ */
@media print {
    /* 应用外壳整体不占布局：小票（Teleport 到 body）成为唯一打印内容 */
    #app {
        display: none !important;
    }
    html, body {
        height: auto !important;
        margin: 0 !important;
        padding: 0 !important;
        background: #fff !important;
    }
    .receipt-overlay {
        position: static !important;
        display: block !important;
        background: none !important;
        padding: 0 !important;
        margin: 0 !important;
        overflow: visible !important;
        height: auto !important;
        min-height: 0 !important;
    }
    .receipt-controls {
        display: none !important;
    }
    .receipt-paper,
    .receipt-paper * {
        visibility: visible !important;
        /* 热敏单色纸：所有文字强制纯黑加粗，浅灰/彩色在热敏纸上浓度太低打不出 */
        color: #000 !important;
        font-weight: 700 !important;
    }
    .receipt-paper {
        position: static !important;
        width: 57mm !important;
        max-width: none !important;
        padding: 0 !important;
        margin: 0 auto !important;
        box-shadow: none !important;
        border-radius: 0 !important;
        font-size: 12px !important;
        line-height: 1.4 !important;
        color: #000 !important;
        font-weight: 600 !important;
        page-break-inside: avoid !important;
        break-inside: avoid !important;
        -webkit-print-color-adjust: exact !important;
        print-color-adjust: exact !important;
    }
    /* 57mm 窄纸下列宽压缩，避免金额列溢出 */
    .receipt-paper .col-price {
        width: 40px !important;
    }
    .receipt-paper .col-qty {
        width: 26px !important;
    }
    .receipt-paper .col-discount {
        width: 30px !important;
    }
    .receipt-paper .col-total {
        width: 46px !important;
    }
    .receipt-paper .shop-name {
        font-size: 16px !important;
    }
    .receipt-paper .item-sku-code {
        font-size: 11px !important;
    }
    .receipt-paper .items-header {
        font-size: 11px !important;
    }
    .receipt-paper .amount-row {
        font-size: 12px !important;
    }
    .receipt-paper .amount-row-final {
        font-size: 13px !important;
    }
    .receipt-paper .final-price {
        font-size: 16px !important;
    }
}

/* 默认 @page 回退（动态 JS 会覆盖此值） */
@page {
    size: 57mm 120mm;
    margin: 0;
}
</style>