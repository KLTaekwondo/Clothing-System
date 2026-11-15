<template>
    <div class="product-import">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">商品导入</h2>
                <p class="page-label-desc">下载模板填写后上传，可先检查再导入</p>
                <hr class="label-hr"/>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <span class="card-title">第一步：下载模板</span>
            </div>
            <p class="import-desc">
                模板已带下拉选项。颜色和尺码用逗号分隔，例如：黑色,白色 / L,M
            </p>
            <button
                :disabled="downloading"
                class="btn-primary import-button"
                type="button"
                @click="handleDownloadTemplate"
            >{{ downloading ? '下载中...' : '下载导入模板' }}</button>
        </div>

        <div class="card">
            <div class="card-header">
                <span class="card-title">第二步：上传文件</span>
            </div>

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

            <label class="import-checkbox-row">
                <input
                    v-model="autoCreateMissing"
                    class="import-checkbox"
                    type="checkbox"
                />
                <span class="import-checkbox-text">自动创建缺失的颜色 / 尺码</span>
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

        <div v-if="result" class="card">
            <div class="card-header">
                <span class="card-title">检查结果</span>
            </div>

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
                <p class="import-desc">勾选“自动创建缺失的颜色 / 尺码”后导入，这些值会自动加入选项表</p>
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
    if (!result.value) return false
    return result.value.fatalErrors.length === 0 && result.value.errors.length === 0
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

.import-desc {
    margin: 8px 0 0 0;
    font-size: 13px;
    color: var(--text-muted);
}

.import-button {
    margin-top: 12px;
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
}

.import-file-input {
    display: none;
}

.import-checkbox-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 16px;
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
    margin-top: 20px;
}

.import-ok {
    margin-top: 12px;
    font-size: 13px;
    color: var(--primary);
}

.import-block {
    margin-top: 16px;
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
    padding: 8px 12px;
    border-radius: 8px;
    font-size: 13px;
    background: var(--bg-subtle);
    color: var(--text-primary);
    border-left: 3px solid var(--error);
}

.import-item-soft {
    padding: 8px 12px;
    border-radius: 8px;
    font-size: 13px;
    background: var(--bg-subtle);
    color: var(--text-primary);
    border-left: 3px solid var(--warning);
}
</style>
