<template>
    <div class="auth-page">
        <div class="auth-card">
            <div class="brand-panel">
                <div class="brand-content">
                    <div class="brand-icon">👕</div>
                    <h1 class="brand-title">服装管理系统</h1>
                    <p class="brand-slogan">Clothing Management System</p>
                    <p class="brand-desc">高效管理商品、订单、库存和员工，让服装生意更简单。</p>
                </div>
            </div>

            <div class="form-panel">
                <div class="form-inner">
                    <h2 class="form-title">欢迎回来</h2>
                    <p class="form-subtitle">登录你的管理后台</p>

                    <div class="role-tabs">
                        <button
                            class="role-btn"
                            :class="{ active: role === 'admin' }"
                            @click="role = 'admin'"
                        >管理员</button>
                        <button
                            class="role-btn"
                            :class="{ active: role === 'warehouse' }"
                            @click="role = 'warehouse'"
                        >仓库员工</button>
                    </div>

                    <form class="auth-form" @submit.prevent="handleLogin">
                        <div class="field-group">
                            <label for="account">账号</label>
                            <input
                                id="account"
                                v-model="form.account"
                                type="text"
                                placeholder="请输入账号"
                                autocomplete="username"
                                required
                            />
                        </div>
                        <div class="field-group">
                            <label for="password">密码</label>
                            <input
                                id="password"
                                v-model="form.password"
                                type="password"
                                placeholder="请输入密码"
                                autocomplete="current-password"
                                required
                            />
                        </div>
                        <button type="submit" class="submit-btn" :disabled="loading">
                            {{ loading ? '登录中…' : '登 录' }}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import adminInterface from '../axios/interface/AdminInterface.js'
import wareHouseInterface from '../axios/interface/WareHouseInterface.js'

const router = useRouter()
const loading = ref(false)
const role = ref('admin')

const form = reactive({
    account: '',
    password: ''
})

async function handleLogin() {
    if (!form.account || !form.password) return
    loading.value = true
    try {
        let result
        if (role.value === 'admin') {
            result = await adminInterface.login({
                account: form.account,
                password: form.password
            })
        } else {
            result = await wareHouseInterface.login({
                account: form.account,
                password: form.password
            })
        }
        if (result) {
            router.push('/manage')
        }
    } catch {
        // 拦截器处理
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.auth-page {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
    background: #f5f5f0;
}

.auth-card {
    display: flex;
    width: 100%;
    max-width: 820px;
    min-height: 500px;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
    background: #fff;
    animation: cardIn 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes cardIn {
    from {
        opacity: 0;
        transform: translateY(20px) scale(0.97);
    }
    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

/* ── 品牌面板 ── */
.brand-panel {
    width: 45%;
    background: linear-gradient(135deg, #0d9488, #14b8a6);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48px 32px;
    position: relative;
    overflow: hidden;
}

.brand-panel::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle at 30% 50%, rgba(255,255,255,0.08) 0%, transparent 60%);
    pointer-events: none;
}

.brand-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: #fff;
}

.brand-icon {
    font-size: 48px;
    width: 72px;
    height: 72px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.15);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
    backdrop-filter: blur(4px);
}

.brand-title {
    font-size: 30px;
    font-weight: 800;
    letter-spacing: -1px;
    margin-bottom: 8px;
    line-height: 1.3;
}

.brand-slogan {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 12px;
    opacity: 0.9;
}

.brand-desc {
    font-size: 13px;
    line-height: 1.7;
    opacity: 0.7;
    max-width: 240px;
    margin: 0 auto;
}

/* ── 表单面板 ── */
.form-panel {
    width: 55%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 48px;
}

.form-inner {
    width: 100%;
    max-width: 320px;
}

.form-title {
    font-size: 24px;
    font-weight: 800;
    color: #2c3e50;
    margin-bottom: 4px;
}

.form-subtitle {
    font-size: 14px;
    color: #636e72;
    margin-bottom: 24px;
}

/* ── 角色切换 ── */
.role-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
}

.role-btn {
    flex: 1;
    padding: 9px 0;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    background: #fff;
    color: #636e72;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.role-btn:hover {
    border-color: #b2bec3;
    color: #2c3e50;
}

.role-btn.active {
    background: rgb(204 251 241 / 0.8);
    border-color: #0d9488;
    color: #0d9488;
}

/* ── 表单字段 ── */
.auth-form {
    display: flex;
    flex-direction: column;
    gap: 18px;
}

.field-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.field-group label {
    font-size: 13px;
    font-weight: 600;
    color: #636e72;
}

.field-group input {
    padding: 12px 14px;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    font-size: 14px;
    color: #2c3e50;
    background: #fff;
    outline: none;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.field-group input::placeholder {
    color: #b2bec3;
}

.field-group input:focus {
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgb(204 251 241 / 0.8);
}

.submit-btn {
    width: 100%;
    padding: 12px;
    border: none;
    border-radius: 8px;
    background: linear-gradient(135deg, #0d9488, #14b8a6);
    color: #fff;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 12px rgba(13, 148, 136, 0.3);
    letter-spacing: 3px;
    margin-top: 2px;
}

.submit-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(13, 148, 136, 0.4);
}

.submit-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}
</style>
