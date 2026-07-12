<template>
    <div class="login-card">
        <div class="card-header">
            <h2>用户登录</h2>
            <p>欢迎回来，请登录您的账户</p>
        </div>
        
        <form class="login-form" @submit.prevent="handleLogin">
            <div class="form-group">
                <label for="username">
                用户名
                <input 
                    type="text" 
                    id="username" 
                    placeholder="请输入用户名"
                    class="form-input"
                    v-model="formData.username"
                    required
                >
                </label>
            </div>
            
            <div class="form-group">
                <label for="password">密码</label>
                <input 
                    type="password" 
                    id="password" 
                    placeholder="请输入密码"
                    class="form-input"
                    v-model="formData.password"
                    required
                >
            </div>
            
            <div class="form-options">
                <label class="remember-me">
                    <input type="checkbox" v-model="formData.rememberMe">
                    <span>记住我</span>
                </label>
                <a href="#" class="forgot-password">忘记密码？</a>
            </div>
            
            <button type="submit" class="login-btn" :disabled="loading">
                <span v-if="loading">登录中...</span>
                <span v-else>登录</span>
            </button>
        </form>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import {useAdminapi} from '../utils/axios/apiComposables/useAdminapi'

const router = useRouter()
const loading = ref(false)
// 表单数据
const formData = reactive({
    username: '',
    password: '',
})
const handleLogin = async () => {
    if (!formData.username || !formData.password) {
        alert('请输入用户名和密码')
        return
    }
    try {
        loading.value = true
        await useAdminapi().login(formData)
        router.push('/dashboard')
        loading.value = false
    } catch (error) {
        loading.value = false
        console.error('登录失败:', error)
        alert('登录失败，请重试')
    }
}
</script>

<style scoped>
.login-card {
    background: white;
    border-radius: 20px;
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
    padding: 40px;
    width: 100%;
    max-width: 400px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
    text-align: center;
    margin-bottom: 30px;
}

.card-header h2 {
    color: #333;
    font-size: 2rem;
    font-weight: 700;
    margin: 0 0 10px 0;
    background: linear-gradient(135deg, #00fbff 0%, #0077ff 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
}

.card-header p {
    color: #666;
    font-size: 0.9rem;
    margin: 0;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 8px;
    color: #333;
    font-weight: 600;
    font-size: 0.9rem;
}

.form-input {
    width: 100%;
    padding: 12px 16px;
    border: 2px solid #e1e5e9;
    border-radius: 10px;
    font-size: 1rem;
    transition: all 0.3s ease;
    background: #f8f9fa;
    box-sizing: border-box;
}

.form-input:focus {
    outline: none;
    border-color: #667eea;
    background: white;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 25px;
    font-size: 0.9rem;
}

.remember-me {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #666;
    cursor: pointer;
}

.remember-me input {
    margin: 0;
}

.forgot-password {
    color: #667eea;
    text-decoration: none;
    transition: color 0.3s ease;
}

.forgot-password:hover {
    color: #764ba2;
}

.login-btn {
    width: 100%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 14px;
    border-radius: 10px;
    font-size: 1.1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-bottom: 20px;
}

.login-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.login-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

.divider {
    position: relative;
    text-align: center;
    margin: 25px 0;
    color: #999;
}

.divider::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: #e1e5e9;
}

.divider span {
    background: white;
    padding: 0 15px;
    position: relative;
    z-index: 1;
}

.social-login {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}

.social-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px;
    border: 2px solid #e1e5e9;
    border-radius: 8px;
    background: white;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 0.9rem;
}

.social-btn:hover {
    border-color: #667eea;
    transform: translateY(-1px);
}

.social-btn.wechat:hover {
    border-color: #07c160;
}

.social-btn.qq:hover {
    border-color: #12b7f5;
}

.card-footer {
    text-align: center;
    padding-top: 20px;
    border-top: 1px solid #e1e5e9;
    color: #666;
    font-size: 0.9rem;
}

.register-link {
    color: #667eea;
    text-decoration: none;
    font-weight: 600;
    transition: color 0.3s ease;
}

.register-link:hover {
    color: #764ba2;
}

/* 响应式设计 */
@media (max-width: 480px) {
    .login-card {
        padding: 30px 20px;
    }
    
    .social-login {
        flex-direction: column;
    }
}
</style>