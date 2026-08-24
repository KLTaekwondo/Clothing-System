<template>
    <div class="main-view">
        <router-view/>
        <ToastNotification/>
        <ConfirmDialog/>
    </div>
</template>

<script setup>
import ToastNotification from './component/ToastNotification.vue'
import ConfirmDialog from './component/ConfirmDialog.vue'
</script>

<style scoped>
</style>