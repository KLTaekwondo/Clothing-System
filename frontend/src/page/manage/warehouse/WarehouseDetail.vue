<template>
    <div class="detail-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回仓库</button>
                <div><h2 class="page-title">仓库详情</h2>
                    <p v-if="warehouse" class="page-desc">{{ warehouse.name }} · {{ warehouse.code }}</p></div>
            </div>
            <button v-if="!editing" class="btn-primary" @click="startEdit">编辑仓库</button>
        </div>

        <div class="detail-tabs">
            <button :class="{ active: activeTab === 'info' }" class="detail-tab" @click="activeTab = 'info'">基本信息
            </button>
            <button :class="{ active: activeTab === 'stock' }" class="detail-tab" @click="activeTab = 'stock'">
                库存管理
            </button>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else-if="!warehouse" class="empty-state">
            <div class="empty-icon"><IconGraphic name="warehouse"/></div>
            <div class="empty-text">仓库不存在</div>
        </div>

        <!-- ═══ 基本信息 Tab ═══ -->
        <template v-if="activeTab === 'info' && warehouse">
            <div class="info-grid">
                <div class="info-card"><span>仓库编码</span><input v-if="editing" v-model="form.code" class="card-input"
                                                                   maxlength="10" minlength="2"
                                                                   type="text"/><strong v-else
                                                                                               class="code-value">{{
                        warehouse.code
                    }}</strong></div>
                <div class="info-card"><span>仓库名称</span><input v-if="editing" v-model="form.name" class="card-input"
                                                                   maxlength="10" minlength="2"
                                                                   type="text"/><strong v-else>{{
                        warehouse.name
                    }}</strong></div>
                <div class="info-card"><span>状态</span><select v-if="editing" v-model="form.status"
                                                                class="card-select">
                    <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
                </select><span v-else :class="warehouse.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                               class="status-badge">{{
                        statusLabels[warehouse.status] || warehouse.status
                    }}</span></div>
                <div class="info-card"><span>盘点状态</span><span
                    :class="warehouse.checkStatus === CHECK_STATUS.UNDER_CHECK ? 'status-warn' : 'status-ok'"
                    class="status-badge"
                >{{ checkStatusLabels[warehouse.checkStatus] || warehouse.checkStatus || '-' }}</span></div>
                <div v-if="editing" class="info-card"><span>新密码</span><input v-model="form.password" class="card-input"
                                                                                maxlength="12" minlength="6"
                                                                                placeholder="留空则不修改"
                                                                                type="password"/></div>
                <div class="info-card"><span>创建时间</span><strong>{{ warehouse.createTime || '-' }}</strong></div>
                <div class="info-card"><span>更新时间</span><strong>{{ warehouse.updateTime || '-' }}</strong></div>
            </div>
            <div v-if="editing" class="edit-actions">
                <button class="btn-outline" @click="cancelEdit">取消</button>
                <button :disabled="saving" class="btn-primary" @click="saveEdit">{{
                        saving ? '保存中...' : '保存修改'
                    }}
                </button>
            </div>
        </template>

        <!-- ═══ 库存管理 Tab ═══ -->
        <template v-if="activeTab === 'stock' && warehouse">
            <div class="stock-search-bar">
                <input v-model="productId" min="1" placeholder="输入商品 ID 查询库存" type="number"/>
                <button class="btn-primary btn-sm" @click="fetchStock">查询</button>
            </div>
            <div class="card stock-table-card">
                <div v-if="stockLoading" class="loading-overlay">
                    <div class="loading-spinner"></div>
                </div>
                <div v-else-if="stockList.length === 0" class="empty-state">
                    <div class="empty-icon"><IconGraphic name="stock"/></div>
                    <div class="empty-text">输入商品 ID 查询该仓库的库存</div>
                </div>
                <table v-else class="data-table">
                    <thead>
                    <tr>
                        <th>规格</th>
                        <th>库存数量</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="(s, i) in stockList" :key="s.id || i">
                        <td>{{ formatSpec(s.spec) }}</td>
                        <td><strong :class="{'stock-low': s.stock <= 10}">{{ s.stock ?? 0 }}</strong></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </template>

    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useToastStore} from '../../../stores/toastStore.js';
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js';
import wareHouseStockInterface from '../../../axios/interface/WareHouseStockInterface.js';
import {STATUS, STATUS_LABELS, STATUS_OPTIONS} from '../../../constants/status.js'
import {CHECK_STATUS, CHECK_STATUS_LABELS} from '../../../constants/checkStatus.js'

const route = useRoute();
const router = useRouter();
const toast = useToastStore();
const statusLabels = STATUS_LABELS;
const checkStatusLabels = CHECK_STATUS_LABELS;
const statusOptions = STATUS_OPTIONS
const warehouse = ref(null);
const loading = ref(true);
const editing = ref(false);
const saving = ref(false);
const form = ref({});
const formBackup = ref(null);
const activeTab = ref('info')
const stockList = ref([]);
const stockLoading = ref(false);
const productId = ref('')

onMounted(async () => {
    try {
        warehouse.value = await wareHouseInterface.search(route.params.id)
    } catch {
        warehouse.value = null
    } finally {
        loading.value = false
    }
})

function startEdit() {
    const w = warehouse.value;
    form.value = {code: w.code || '', name: w.name || '', status: w.status || STATUS.ENABLE, password: ''};
    formBackup.value = {...form.value};
    editing.value = true
}

function cancelEdit() {
    form.value = {...formBackup.value};
    editing.value = false;
    formBackup.value = null
}

async function saveEdit() {
    if (!form.value.code || !form.value.name) {
        toast.warning('请填写完整信息');
        return
    }
    saving.value = true;
    const body = {
        code: form.value.code,
        name: form.value.name,
        status: form.value.status
    };
    // 密码留空表示不修改，不提交该字段
    if (form.value.password) {
        body.password = form.value.password;
    }
    const res = await wareHouseInterface.update(warehouse.value.id, body);

    // 如果成功，则返回仓库列表页
    if(res){
        goBack();
    }

    saving.value = false;
}

// 获取仓库库存
async function fetchStock() {
    if (!productId.value) {
        toast.warning('请输入商品 ID');
        return
    }
    stockLoading.value = true;
    try {
        stockList.value = await wareHouseStockInterface.searchStock(route.params.id, Number(productId.value))
    } catch {
        stockList.value = []
    } finally {
        stockLoading.value = false
    }
}

function formatSpec(s) {
    if (!s) return '-';
    return Object.entries(s).map(([k, v]) => `${k}:${v}`).join(' / ')
}

const goBack = () => {
    router.push('/manage/warehouse')
}
</script>

<style scoped>
.detail-page {
    width: 100%;
    min-width: 0;
}

.page-heading,
.heading-left {
    display: flex;
    align-items: center;
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 20px;
}

.heading-left {
    gap: 12px;
}

.page-title {
    margin-bottom: 4px;
    font-size: 26px;
    letter-spacing: -0.5px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.detail-tabs {
    display: flex;
    gap: 4px;
    margin-bottom: 22px;
    border-bottom: 1px solid #dfece9;
}

.detail-tab {
    padding: 10px 20px;
    border: none;
    border-bottom: 2px solid transparent;
    background: none;
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: -1px;
}

.detail-tab:hover {
    color: var(--primary);
}

.detail-tab.active {
    color: var(--primary);
    border-bottom-color: var(--primary);
}

.info-card {
    width: calc(33.33% - 10px);
    min-width: 210px;
    min-height: 112px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 12px;
    padding: 20px;
    background: linear-gradient(145deg, var(--bg-card), var(--bg-subtle));
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.info-card > span:first-child {
    color: var(--text-muted);
    font-size: var(--font-sm);
}

.edit-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
    padding-top: 18px;
    border-top: 1px solid var(--border-light);
}

.stock-search-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;
}

.stock-search-bar input {
    width: 280px;
    height: 40px;
    border-radius: 10px;
    padding: 0 12px;
    border: 1px solid var(--border);
    background: var(--bg-subtle);
}

.stock-table-card {
    padding: 8px 20px 20px;
    border-radius: 16px;
}

.stock-low {
    color: var(--error);
    font-weight: 800;
}</style>
