<template>
    <TestTable :table-data="tableData" :columns="columns" />
</template>

<script setup>
import TestTable from '../components/TestTable.vue'
import {useWareHouseapi} from '../utils/axios/apiComposables/useWareHouseapi'
import { onMounted, ref } from 'vue';

const tableData = ref([])
const columns = ref([
    {Id:'wareHouseId',Label:'仓库ID'},
    {Id:'wareHouseName',Label:'仓库名称'},
    {Id:'wareHouseStatus',Label:'仓库状态'}
])

onMounted(async () => {
    try{
        console.log('开始获取仓库数据...')
        const response = await useWareHouseapi().getall()
        console.log('API响应数据:', response)
        
        // 确保数据是数组格式
        tableData.value = Array.isArray(response) ? response : 
                         Array.isArray(response?.data) ? response.data : 
                         Array.isArray(response?.result) ? response.result : []
        
    } catch (error) {
        console.error('获取测试数据失败:', error)
        alert('获取测试数据失败，请重试')
    }
})
</script>