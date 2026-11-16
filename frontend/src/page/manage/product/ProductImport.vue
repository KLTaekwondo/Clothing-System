<template>
    <div class="product-import">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">商品导入</h2>
                <p class="page-label-desc">下载模板填写后上传，先检查再导入</p>
                <hr class="label-hr"/>
            </div>
        </div>

        <div class="import-layout">
            <div class="import-steps">
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">第一步：下载模板</span>
                    </div>
                    <p class="import-desc">
                        模板已带下拉选项。颜色和尺码用逗号分隔，例如：黑色,白色 / L,M
                    </p>
                    <div class="import-step-action">
                        <button
                            :disabled="downloading"
                            class="btn-primary"
                            type="button"
                            @click="handleDownloadTemplate"
                        >{{ downloading ? '下载中...' : '下载导入模板' }}</button>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <span class="card-title">第二步：上传文件</span>
                    </div>
                    <p class="import-desc">
                        只接受 .xlsx，必须用下载的模板填写，列顺序不要调整
                    </p>
                    <div class="import-file-row">
                        <button
                            class="btn-outline"
                            type="button"
                            @click="pickFile"
                        >选择文件</button>
                        <span class="import-file-name">{{ fileName || '未选择文件' }}</span>
                        <input
                            ref="fileInputRef"
                            class="import-file-input"
                            type="file"
                            accept=".xlsx"
                            @change="handleFileChange"
                        />
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <span class="card-title">第三步：检查并导入</span>
                    </div>
                    <label class="import-checkbox-row">
                        <input
                            v-model="autoCreateMissing"
                            class="import-checkbox"
                            type="checkbox"
                        />
                        <span class="import-checkbox-text">自动创建缺失的规格</span>
                    </label>
                    <div class="import-action-row">
                        <button
                            :disabled="!hasFile || checking"
                            class="btn-outline"
                            type="button"
                            @click="handleValidate"
                        >{{ checking ? '检查中...' : '仅检查' }}</button>
                        <button
                            :disabled="!hasFile || importing"
                            class="btn-primary"
                            type="button"
                            @click="handleImport"
                        >{{ importing ? '导入中...' : '导入' }}</button>
                    </div>
                </div>
            </div>

            <div class="import-result">
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">检查结果</span>
                        <span class="card-hint">{{ resultHint }}</span>
                    </div>

                    <p v-if="!result" class="import-placeholder">
                        还没有检查结果。上传文件后点「仅检查」，结果会显示在这里
                    </p>

                    <div v-else class="import-result-body">
                        <div v-if="noProblem" class="import-ok">
                            没有发现问题，可以直接导入
                        </div>

                        <div v-if="result.fatalErrors.length > 0" class="import-block">
                            <div class="import-subtitle">必须修正 {{ result.fatalErrors.length }} 项</div>
                            <ul class="import-list">
                                <li
                                    v-for="(message, index) in result.fatalErrors"
                                    :key="'fatal-' + index"
                                    class="import-item-fatal"
                                >{{ message }}</li>
                            </ul>
                        </div>

                        <div v-if="result.errors.length > 0" class="import-block">
                            <div class="import-subtitle">需要创建的规格 {{ result.errors.length }} 项</div>
                            <ul class="import-list">
                                <li
                                    v-for="(message, index) in result.errors"
                                    :key="'error-' + index"
                                    class="import-item-soft"
                                >{{ message }}</li>
                            </ul>
                            <p class="import-desc">
                                勾选「自动创建缺失的规格」后导入，这些值会自动加入选项表
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useToastStore} from '../../../stores/toastStore.js'
import productExcelInterface from '../../../axios/interface/ProductExcelInterface.js'

const toast = useToastStore()

const fileInputRef = ref(null)
const file = ref(null)
const fileName = ref('')
const autoCreateMissing = ref(false)
const downloading = ref(false)
const checking = ref(false)
const importing = ref(false)
const result = ref(null)

const hasFile = computed(() => file.value !== null)

const noProblem = computed(() => {
    if (!result.value) {
        return false
    }
    return result.value.fatalErrors.length === 0 && result.value.errors.length === 0
})

// 右上角的摘要，让人一眼知道能不能导
const resultHint = computed(() => {
    if (!result.value) {
        return '尚未检查'
    }
    if (noProblem.value) {
        return '检查通过'
    }
    const fatal = result.value.fatalErrors.length
    const soft = result.value.errors.length
    const parts = []
    if (fatal > 0) {
        parts.push('必须修正 ' + fatal + ' 项')
    }
    if (soft > 0) {
        parts.push('待创建规格 ' + soft + ' 项')
    }
    return parts.join('，')
})

function pickFile() {
    fileInputRef.value?.click()
}

function handleFileChange(event) {
    const selected = event.target.files?.[0]
    if (!selected) {
        return
    }
    file.value = selected
    fileName.value = selected.name
    result.value = null
}

async function handleDownloadTemplate() {
    downloading.value = true
    try {
        await productExcelInterface.downloadTemplate()
        toast.success('模板已开始下载')
    } finally {
        downloading.value = false
    }
}

async function handleValidate() {
    if (!file.value) {
        return
    }
    checking.value = true
    try {
        result.value = await productExcelInterface.validate(file.value)
    } catch {
        // 拦截器已提示（422/412 都会走中心弹窗或 toast）
    } finally {
        checking.value = false
    }
}

async function handleImport() {
    if (!file.value) {
        return
    }
    importing.value = true
    try {
        const response = await productExcelInterface.importProducts(file.value, autoCreateMissing.value)
        toast.success(response?.msg || '导入成功')
        result.value = null
        file.value = null
        fileName.value = ''
        if (fileInputRef.value) {
            fileInputRef.value.value = ''
        }
    } catch {
        // 拦截器已提示，文件状态保留，方便直接改完重传
    } finally {
        importing.value = false
    }
}
</script>

<style scoped>
.product-import {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.import-layout {
    display: flex;
    align-items: flex-start;
    gap: 22px;
}

.import-steps {
    width: 320px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    position: sticky;
    top: 24px;
}

.import-result {
    width: calc(100% - 342px);
    min-width: 0;
}

.import-result-body {
    max-height: calc(100vh - 300px);
    overflow-y: auto;
    padding-right: 4px;
}

.import-desc {
    margin: 8px 0 0 0;
    font-size: 13px;
    line-height: 1.6;
    color: var(--text-muted);
}

.import-step-action {
    display: flex;
    margin-top: 14px;
}

.import-file-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 12px;
}

.import-file-name {
    font-size: 13px;
    color: var(--text-secondary);
    word-break: break-all;
}

.import-file-input {
    display: none;
}

.import-checkbox-row {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
}

.import-checkbox {
    width: 16px;
    height: 16px;
    cursor: pointer;
}

.import-checkbox-text {
    font-size: 13px;
    color: var(--text-secondary);
}

.import-action-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 18px;
}

.import-placeholder {
    margin: 0;
    padding: 48px 0;
    text-align: center;
    font-size: 13px;
    color: var(--text-muted);
}

.import-ok {
    margin: 0;
    padding: 12px 14px;
    border-radius: 10px;
    font-size: 13px;
    color: var(--primary);
    background: var(--primary-light);
}

.import-block {
    margin-top: 18px;
}

.import-block:first-child {
    margin-top: 0;
}

.import-subtitle {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
}

.import-list {
    margin: 8px 0 0 0;
    padding: 0;
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.import-item-fatal {
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--error-dark);
    background: var(--error-light);
}

.import-item-soft {
    padding: 10px 14px;
    border-radius: 10px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--warning-dark);
    background: var(--warning-light);
}

@media (max-width: 960px) {
    .import-layout {
        flex-direction: column;
    }

    .import-steps {
        width: 100%;
        position: static;
    }

    .import-result {
        width: 100%;
    }

    .import-result-body {
        max-height: none;
    }
}
</style>
