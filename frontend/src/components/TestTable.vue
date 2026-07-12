<template>
    <div>
        <table>
            <thead>
                <tr>
                    <th>仓库ID</th>
                    <th>仓库名称</th>
                    <th>仓库状态</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in warehouseList" :key="item.warehouseId">
                    <td>{{ item.wareHouseId }}</td>
                    <td>{{ item.wareHouseName }}</td>
                    <td>{{ item.wareHouseStatus }}</td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script setup>
import {useWareHouseapi} from '../utils/axios/apiComposables/useWareHouseapi'
import {onMounted, ref} from 'vue'

const warehouseList = ref([])
const warehouseApi = useWareHouseapi()

onMounted(async () => {
    try{
        const response = await warehouseApi.getall()
        console.log('API响应:', response)
        warehouseList.value = response.data || response
        console.log('处理后的数据:', warehouseList.value)
        
    }catch(error){
        console.error('获取所有仓库失败:', error)
    }
})
</script>