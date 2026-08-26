<template>
    <div class="detail-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">仓库详情</h2>
                <p class="page-label-desc">{{ warehouse ? warehouse.name + ' · ' + warehouse.code : '加载中...' }}</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            @click="goBack"
                        >← 返回仓库</button>
                        <button
                            v-if="!editing"
                            class="btn-primary search-button"
                            @click="startEdit"
                        >编辑仓库</button>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else-if="!warehouse" class="empty-state">
            <div class="empty-icon"><IconGraphic name="warehouse"/></div>
            <div class="empty-text">仓库不存在</div>
        </div>

        <template v-if="warehouse">
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
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useToastStore} from '../../../stores/toastStore.js';
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js';
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
const goBack = () => {
    router.push('/manage/warehouse')
}
</script>

<style scoped>
.detail-page {
    width: 100%;
    min-width: 0;
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
</style>
