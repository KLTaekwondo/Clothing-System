<template>
    <div class="standalone-form-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回仓库</button>
                <div><h2 class="page-title">添加仓库</h2>
                    <p class="page-desc">创建仓库并绑定管理员</p></div>
            </div>
        </div>
        <div class="card standalone-form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-row">
                    <div class="form-group"><label>仓库编码</label><input v-model="form.code" maxlength="10"
                                                                          minlength="2" placeholder="请输入仓库编码"
                                                                          required/></div>
                    <div class="form-group"><label>仓库名称</label><input v-model="form.name" maxlength="10"
                                                                          minlength="2" placeholder="请输入仓库名称"
                                                                          required/></div>
                </div>
                <div class="form-row">
                    <div class="form-group"><label>仓库密码</label><input v-model="form.password" maxlength="12"
                                                                          minlength="6"
                                                                          placeholder="6-12位字母、数字或下划线"
                                                                          required
                                                                          type="password"/></div>
                    <div class="form-group"><label>管理员 ID</label><input v-model.number="form.adminId" min="1"
                                                                           placeholder="请输入管理员 ID" required
                                                                           type="number"/></div>
                </div>
                <div class="form-actions">
                    <button class="btn-outline" type="button" @click="goBack">取消</button>
                    <button :disabled="submitting" class="btn-primary" type="submit">
                        {{ submitting ? '保存中...' : '保存仓库' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import {useToastStore} from '../../stores/toastStore.js';
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js';

const router = useRouter();
const toast = useToastStore();
const submitting = ref(false);
const form = ref({code: '', name: '', password: '', adminId: ''});

async function handleSubmit() {
    submitting.value = true;
    const res = await wareHouseInterface.create({
            code: form.value.code,
            name: form.value.name,
            password: form.value.password,
            adminId: Number(form.value.adminId)
        });

    if(res){
        goBack();
    }

    submitting.value = false;
}

const goBack = () => {
    router.push('/manage/warehouse');
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
    max-width: 840px;
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
