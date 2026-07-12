<template>
    <div class="dialog-content" v-if="show" :style="{'background': status === 'success' ? dialogSuccess : dialogError}">
        <div class="dialog-header">
            <h2 class="dialog-title">{{ title }}</h2>
        </div>
        <div class="dialog-body">
            <p>{{ message }}</p>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
const dialogSuccess = ref('linear-gradient(135deg, #1890ff 0%, #52c41a 100%)')
const dialogError = ref('linear-gradient(135deg, #ff4d4f 0%, #ff9966 100%)')

const props = defineProps({
    status: {
        type: String,
        default: 'success'
    },
    title: {
        type: String,
        default: '提示'
    },
    message: {
        type: String,
        default: ''
    },
    show:{
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(['close'])

const handleClose = () => emit('close')
</script>

<style scoped>
.dialog-content {
    position: fixed;
    bottom: 20px;
    right: 20px;
    z-index: 1000;
    background: #fff;
    color: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    max-width: 400px;
    animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(100%);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.dialog-header {
    margin-bottom: 20px;
}

.dialog-title {
    font-size: 18px;
    font-weight: bold;
}

.dialog-body {
    margin-bottom: 20px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
}
</style>