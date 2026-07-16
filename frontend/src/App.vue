<template>
    <div class="main-view">
        <router-view/>
        <ToastNotification/>
    </div>
</template>

<script setup>
import ToastNotification from './component/ToastNotification.vue'
</script>

<style scoped>

</style>