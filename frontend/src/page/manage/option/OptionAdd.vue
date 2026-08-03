<template>
    <div class="standalone-form-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回选项</button>
                <div><h2 class="page-title">添加选项值</h2>
                    <p class="page-desc">创建颜色、尺码、类型等商品选项</p></div>
            </div>
        </div>
        <div class="card standalone-form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-group"><label>选项类型</label><select v-model="form.optionType" required>
                    <option disabled value="">请选择选项类型</option>
                    <option v-for="item in typeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select></div>
                <div class="form-group"><label>选项值</label><input v-model="form.optionValue" maxlength="8"
                                                                    placeholder="例如：红色、XL、棉" required/></div>
                <div class="form-actions">
                    <button class="btn-outline" type="button" @click="goBack">取消</button>
                    <button :disabled="submitting" class="btn-primary" type="submit">
                        {{ submitting ? '保存中...' : '保存选项值' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import {useToastStore} from '../../../stores/toastStore.js';
import optionValueInterface from '../../../axios/interface/OptionValueInterface.js';
import {OPTION_TYPE_OPTIONS} from '../../../constants/optionType.js';

const router = useRouter();
const toast = useToastStore();
const typeOptions = OPTION_TYPE_OPTIONS;
const submitting = ref(false);
const form = ref({optionType: '', optionValue: ''});

async function handleSubmit() {
    submitting.value = true;
    const res = await optionValueInterface.create({
        optionType: form.value.optionType,
        optionValue: form.value.optionValue
    });

    // 如果成功，则返回选项列表页
    if (res){
        goBack();
    }

    // 重置提交状态
    submitting.value = false;
}

function goBack() {
    router.push('/manage/option')
}
</script>
<style scoped>
.standalone-form-page {
    width: 100%;
    min-width: 0
}

.page-heading {
    margin-bottom: 28px
}

.heading-left {
    display: flex;
    align-items: center;
    gap: 12px
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm)
}

.standalone-form-card {
    max-width: 700px;
    padding: 28px;
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, .06)
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
    padding-top: 20px;
    border-top: 1px solid var(--border-light)
}
</style>
