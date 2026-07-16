<template>
    <div class="standalone-form-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回员工</button>
                <div><h2 class="page-title">添加员工</h2>
                    <p class="page-desc">创建员工账号并分配所属仓库</p></div>
            </div>
        </div>
        <div class="card standalone-form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-row">
                    <div class="form-group"><label>员工编码</label><input v-model="form.code" maxlength="10"
                                                                          minlength="2" placeholder="请输入员工编码"
                                                                          required/></div>
                    <div class="form-group"><label>员工姓名</label><input v-model="form.name" maxlength="10"
                                                                          minlength="2" placeholder="请输入员工姓名"
                                                                          required/></div>
                </div>
                <div class="form-group"><label>所属仓库</label><select v-model.number="form.wareHouseId" required>
                    <option disabled value="">请选择仓库</option>
                    <option v-for="item in warehouses" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select></div>
                <div class="form-actions">
                    <button class="btn-outline" type="button" @click="goBack">取消</button>
                    <button :disabled="submitting" class="btn-primary" type="submit">
                        {{ submitting ? '保存中...' : '保存员工' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>
<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'

const router = useRouter();
const toast = useToastStore();
const submitting = ref(false);
const warehouses = ref([]);
const form = ref({code: '', name: '', wareHouseId: ''})
onMounted(async () => {
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    }
})

async function handleSubmit() {
    submitting.value = true;
    const res = await employeeInterface.create({
        code: form.value.code,
        name: form.value.name,
        wareHouseId: Number(form.value.wareHouseId)
    });

    // 如果成功，则返回员工列表页
    if (res){
        goBack();
    }

    // 重置提交状态
    submitting.value = false;
}

const goBack = () => {
    router.push('/manage/employee')
}
</script>
<style scoped>
.standalone-form-page {
    width: 100%;
    min-width: 0;
}

.page-heading {
    margin-bottom: 28px;
}

.heading-left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.standalone-form-card {
    max-width: 780px;
    padding: 28px;
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, .06);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
    padding-top: 20px;
    border-top: 1px solid var(--border-light);
}
</style>
