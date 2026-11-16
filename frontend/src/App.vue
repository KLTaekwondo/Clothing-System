<template>
    <div class="main-view">
        <router-view/>
        <ToastNotification/>
        <ConfirmDialog/>
        <ErrorDialog/>
    </div>
</template>

<script setup>
import ToastNotification from './component/ToastNotification.vue'
import ConfirmDialog from './component/ConfirmDialog.vue'
import ErrorDialog from './component/ErrorDialog.vue'
</script>

<style scoped>
</style>
