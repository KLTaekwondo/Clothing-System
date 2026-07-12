<template>
    <div class="page-container">
        <!-- 页面头部 -->
        <div class="page-header">
            <div class="header-content">
                <div class="title-section">
                    <h1 class="page-title">销售员管理</h1>
                    <p class="page-subtitle">管理{{ selectedWareHouseName }}中的所有销售员信息</p>
                </div>
                <div class="action-buttons">
                    <!-- 替换原生 select 为 SelectBox -->
                    <SelectBox 
                        v-model="SelectedWareHouseId"
                        :options="warehouseOptions"
                        placeholder="请选择仓铺"
                        label="选择仓铺"
                        @change="handleWareHouseChange"
                    />
                    
                    <SmallOption icon="tianjia" title="添加销售员" @click="setBoardStatus('create')" />
                    <SmallOption icon="bianji" title="编辑销售员" @click="setBoardStatus('edit')" />
                    <SmallOption icon="shanchu" title="删除销售员" @click="handleDeleteClick" />
                </div>
            </div>
        </div>

        <!-- 主要内容区域 -->
        <div class="main-content">
            <div class="content-card">
                <!-- 销售员表格 -->
                <transition name="slide-left">
                    <CommonFormBoard
                        v-if = "showCreateBoard"
                        mode = "create"
                        :columns="createcolumns"
                        entityName = "销售员"
                        @close = "showCreateBoard = false"
                        @success = "handleAddSuccess"
                    />
                </transition>

                <!-- 销售员编辑表单 -->
                <transition name="slide-left">
                    <CommonFormBoard
                        v-if = "showEditBoard"
                        mode = "edit"
                        :columns="createcolumns"
                        entityName = "销售员"
                        @close = "showEditBoard = false"
                        @success = "handleEditSuccess"
                    />
                </transition>

                <!-- 销售员表格 -->
                <div class="table-area" :class="{ 'compressed': showCreateBoard || showEditBoard }">
                    <CommonTable :table-data="SalesManTableData" :columns="columns" @rowClick="setSelectedRow" />
                </div>
            </div>
        </div>

        <transition name="slide-up">
            <Dialog :show="dialogVisible" :message="dialogMessage" :status="dialogStatus" :title="dialogTitle" />
        </transition>
    </div>
</template>

<script setup>
//导入组件
import CommonTable from '../Boards/CommonTable.vue'
import SmallOption from '../components/SmallOption.vue'
import CommonFormBoard from '../Boards/CommonFormBoard.vue'
import Dialog from '../components/Dialog.vue'
import SelectBox from '../components/SmallSelectBox.vue'

//导入路由和api
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { useSalesManapi } from '../utils/axios/apiComposables/useSalesManapi'
import { onMounted, ref, computed } from 'vue'

//初始化变量
const tableData = ref([])
const SalesManTableData = ref([])
const SelectedWareHouseId = ref('')
const selectedWareHouseName = ref('')  // 改为小驼峰命名

// 计算属性：将仓库数据转换为 SelectBox 需要的格式
const warehouseOptions = computed(() => {
    return tableData.value.map(warehouse => ({
        value: warehouse.wareHouseId,
        label: `${warehouse.wareHouseId} ${warehouse.wareHouseName}`
    }))
})

const handleWareHouseChange = async (value) => {
    if (value) {
        const selectedWarehouse = tableData.value.find(warehouse =>
            warehouse.wareHouseId === value
        )
        if (selectedWarehouse) {
            selectedWareHouseName.value = selectedWarehouse.wareHouseName
            sessionStorage.setItem('SelectedWareHouseId', value)
            // 加载对应仓铺的销售员数据
            SalesManTableData.value = await useSalesManapi().getall()
        }
    } else {
        selectedWareHouseName.value = ''
        SalesManTableData.value = []  // 清空销售员数据
    }
}

//定义表格列
const columns = ref([
    { Id: 'salesManId', Label: '销售员ID' },
    { Id: 'salesManName', Label: '销售员名称' },
])

//定义创建/修改仓库表格列
const createcolumns = ref([
    { Id: 'salesManId', Label: '销售员ID' },
    { Id: 'salesManName', Label: '销售员名称' },
])

//定义表格状态
const showEditBoard = ref(false)
const showCreateBoard = ref(false)
const showBoardStatus = ref('')

//定义表格状态切换函数
const setBoardStatus = (status) => {
    showBoardStatus.value = status
    if (status === 'create') {
        if (!SelectedWareHouseId.value) {
            handleDialogProps('error', '失败', '请选择要添加的仓铺')
            setTimeout(() => {
                dialogVisible.value = false
            }, 1200)
            return
        } else {
            if (showEditBoard.value) {
                showEditBoard.value = false
            }
            showCreateBoard.value = !showCreateBoard.value
        }
    } else if (status === 'edit') {
        if (showCreateBoard.value) {
            showCreateBoard.value = false
        }

        if (!SelectedRowId.value) {
            handleDialogProps('error', '失败', '请选择要编辑的销售员')
            setTimeout(() => {
                dialogVisible.value = false
            }, 1200)
            return
        } else {
            showEditBoard.value = !showEditBoard.value
        }
    }
}

//定义选中行变量
const SelectedRowId = ref('')
const setSelectedRow = (row) => {
    SelectedRowId.value = row || ''
    console.log('选中', SelectedRowId.value?.salesManId)
}

//定义弹窗变量
const dialogVisible = ref(false)
const dialogStatus = ref('')
const dialogTitle = ref('')
const dialogMessage = ref('')

//处理弹窗函数
const handleDialogProps = (status, title, message) => {
    dialogVisible.value = true
    dialogStatus.value = status
    dialogTitle.value = title
    dialogMessage.value = message
}

const refreshSalesManTableData = async () => {
    try {
        if (!SelectedWareHouseId.value) {
            SalesManTableData.value = []
            return
        }
        const response = await useSalesManapi().getallByWareHouse(SelectedWareHouseId.value)
        SalesManTableData.value = Array.isArray(response) ? response :
            Array.isArray(response?.data) ? response.data : []
    } catch (error) {
        console.error('刷新销售员列表失败:', error)
    }
}

//处理创建销售员成功函数
const handleAddSuccess = async (formData) => {
    try {
        // 添加仓铺ID到表单数据
        const formDataWithWareHouse = {
            ...formData,
            wareHouseId: SelectedWareHouseId.value
        }
        await useSalesManapi().create(formDataWithWareHouse)
        handleDialogProps('success', '成功', '销售员创建成功')
        setTimeout(async() => {
            dialogVisible.value = false
            await refreshSalesManTableData()
        }, 1000)
    } catch (error) {
        handleDialogProps('error', '失败', '销售员创建失败')
        setTimeout(() => {
            dialogVisible.value = false
        }, 1000)
        console.error('创建销售员失败:', error)
    }
}

//处理删除销售员点击函数
const handleDeleteClick = async () => {
    if (!SelectedRowId.value) {
        handleDialogProps('error', '失败', '请选择要删除的销售员')
        setTimeout(() => {
            dialogVisible.value = false
        }, 1000)
        return
    } else {
        try {
            await useSalesManapi().datadelete(SelectedRowId.value.salesManId)
            handleDialogProps('success', '成功', '销售员删除成功')
            setTimeout(async() => {
                dialogVisible.value = false
                await refreshSalesManTableData()
            }, 1000)
        } catch (error) {
            handleDialogProps('error', '失败', '销售员删除失败')
            setTimeout(() => {
                dialogVisible.value = false
            }, 1200)
            console.error('删除销售员失败:', error)
        }
    }
}

//处理编辑销售员成功函数
const handleEditSuccess = async (formData) => {
    try {
        await useSalesManapi().edit(formData)
        handleDialogProps('success', '成功', '销售员编辑成功')
        setTimeout(async() => {
            dialogVisible.value = false
            await refreshSalesManTableData()
        }, 1000)
    }
    catch (error) {
        handleDialogProps('error', '失败', '销售员编辑失败')
        setTimeout(() => {
            dialogVisible.value = false
        }, 1200)
        console.error('编辑销售员失败:', error)
    }
}

//处理获取仓库数据函数
onMounted(async () => {
    try {
        tableData.value = await useWareHouseapi().getall()
        
        // 检查是否有缓存的仓铺选择
        const cachedWareHouseId = sessionStorage.getItem('SelectedWareHouseId')
        if (cachedWareHouseId) {
            SelectedWareHouseId.value = cachedWareHouseId
            // 触发仓库变更
            await handleWareHouseChange(cachedWareHouseId)
        }
    } catch (error) {
        console.error('获取所有仓库失败:', error)
    }
})
</script>

<style scoped>
/* 原有的样式保持不变，只需调整 action-buttons 中的间距 */
.page-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 24px;
}

.page-header {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    margin-bottom: 24px;
}

.header-content {
    padding: 24px 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
}

.title-section {
    flex: 1;
    min-width: 200px;
}

.page-title {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 8px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.page-subtitle {
    font-size: 16px;
    color: #718096;
    margin: 0;
}

.action-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    align-items: center;
}

/* 为 SelectBox 添加特定样式，使其与按钮对齐 */
.action-buttons .select-container {
    margin: 0;
    min-width: 200px;
}

.main-content {
    flex: 1;
}

.content-card {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    position: relative;
    min-height: 500px;
}

.table-area {
    transition: all 0.3s ease;
    padding: 20px;
}

.table-area.compressed {
    margin-right: 400px;
}

/* 动画 */
.slide-left-enter-active,
.slide-left-leave-active {
    transition: all 0.3s ease;
}

.slide-left-enter-from {
    transform: translateX(100%);
    opacity: 0;
}

.slide-left-leave-to {
    transform: translateX(100%);
    opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
    transition: all 0.4s ease-out;
}

.slide-up-enter-from {
    opacity: 0;
    transform: translateY(100%);
}

.slide-up-leave-to {
    opacity: 0;
    transform: translateY(100%);
}

/* 响应式设计 */
@media (max-width: 1024px) {
    .table-area.compressed {
        margin-right: 350px;
    }
}

@media (max-width: 768px) {
    .page-container {
        padding: 16px;
    }
    .header-content {
        padding: 20px;
        flex-direction: column;
        text-align: center;
    }
    .table-area.compressed {
        margin-right: 0;
        margin-bottom: 400px;
    }
}

@media (max-width: 480px) {
    .page-container { padding: 12px; }
    .header-content { padding: 16px; }
    .action-buttons {
        flex-direction: column;
        width: 100%;
    }
    .page-title { font-size: 24px; }
}
</style>