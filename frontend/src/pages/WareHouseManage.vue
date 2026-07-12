<template>
    <div class="page-container">
        <!-- 页面头部 -->
        <div class="page-header">
            <div class="header-content">
                <div class="title-section">
                    <h1 class="page-title">仓库管理</h1>
                    <p class="page-subtitle">管理{{ currentusername }}账户中的所有仓库信息</p>
                </div>
                <div class="action-buttons">
                    <SmallOption icon="tianjia" title="添加仓库" @click="setBoardStatus('create')" />
                    <SmallOption icon="bianji" title="编辑选中" @click="setBoardStatus('edit')" />
                    <SmallOption icon="shanchu" title="删除选中" @click="handleDeleteClick" />
                </div>
            </div>
        </div>

        <!-- 主要内容区域 -->
        <div class="main-content">
            <div class="content-card">
                <!-- 创建仓库表单 -->
                <transition name="slide-left">
                    <CommonFormBoard
                        v-if = "showCreateBoard"
                        mode = "create"
                        :columns="createcolumns"
                        entityName = "仓库"
                        @close = "showCreateBoard = false"
                        @success = "handleAddSuccess"
                    />
                </transition>
                <!-- 编辑仓库表单 -->
                <transition name="slide-left">
                    <CommonFormBoard
                        v-if = "showEditBoard"
                        mode = "edit"
                        :columns="createcolumns"
                        entityName = "仓库"
                        @close = "showEditBoard = false"
                        @success = "handleEditSuccess"
                    />
                </transition>

                <!-- 仓库表格 -->
                <div class="table-area" :class="{ 'compressed': showCreateBoard || showEditBoard }">
                    <CommonTable :table-data="tableData" :columns="columns" @rowClick="setSelectedRow" />
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

//导入路由和api
import { useRouter } from 'vue-router'
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { onMounted, ref } from 'vue'

//初始化变量
const currentusername = ref(sessionStorage.getItem('currentAdmin') || '')
const tableData = ref([])
const router = useRouter()

//定义表格列
const columns = ref([
    { Id: 'wareHouseId', Label: '仓库ID' },
    { Id: 'wareHouseName', Label: '仓库名称' },
    { Id: 'wareHouseStatus', Label: '仓库状态' }
])

//定义创建/修改仓库表格列
const createcolumns = ref([
    { Id: 'wareHouseId', Label: '仓库ID' },
    { Id: 'wareHouseName', Label: '仓库名称' },
    { Id: 'wareHouseStatus', Label: '仓库状态' },
    { Id: 'wareHousePassword', Label: '仓库密码' }
])

//定义表格状态
const showEditBoard = ref(false)
const showCreateBoard = ref(false)
const showBoardStatus = ref('')

//定义表格状态切换函数
const setBoardStatus = (status) => {
    showBoardStatus.value = status
    if (status === 'create') {
        if(showEditBoard.value){
            showEditBoard.value = false
        }
        showCreateBoard.value = !showCreateBoard.value
    } else if (status === 'edit') {
        if(showCreateBoard.value ){
            showCreateBoard.value = false
        }
        if(SelectedRowId.value === ''){
            handleDialogProps('error', '失败', '请选择要编辑的仓库')
            setTimeout(() => {
                dialogVisible.value = false
            }, 1200)
            return
        }else{
            showEditBoard.value = !showEditBoard.value
        }       
    }
}

//定义选中行变量
const SelectedRowId = ref('')
const setSelectedRow = (row) => {
    if(row === null){
        SelectedRowId.value = ''
        return
    }else{
        SelectedRowId.value = row
    }
    console.log('选中',SelectedRowId.value.wareHouseId)
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

// 添加刷新表格数据的函数
const refreshTableData = async () => {
    try {
        const response = await useWareHouseapi().getall()
        tableData.value = Array.isArray(response) ? response :
            Array.isArray(response?.data) ? response.data : []
    } catch (error) {
        console.error('刷新数据失败:', error)
    }
}

//处理创建仓库成功函数
const handleAddSuccess = async (formData) => {
    try {
        await useWareHouseapi().create(formData)
        handleDialogProps('success', '成功', '仓库创建成功')
        setTimeout(async () => {
            dialogVisible.value = false
            await refreshTableData() 
        }, 1000)

    } catch (error) {
        handleDialogProps('error', '失败', '仓库创建失败')
        setTimeout(() => {
            dialogVisible.value = false
        }, 1000)
        console.error('创建仓库失败:', error)
    }
}

//处理删除仓库点击函数
const handleDeleteClick = async() => {
    if (SelectedRowId.value === '') {
        handleDialogProps('error', '失败', '请选择要删除的仓库')
        setTimeout(() => {
            dialogVisible.value = false
        }, 1000)
        return
    }else{
        try {
            await useWareHouseapi().datadelete(SelectedRowId.value.wareHouseId)
            handleDialogProps('success', '成功', '仓库删除成功')
            SelectedRowId.value = ''
            setTimeout(async () => {
                dialogVisible.value = false
                await refreshTableData() // 只刷新表格数据，不刷新整个页面
            }, 1000)
        } catch (error) {
            handleDialogProps('error', '失败', '仓库删除失败')
            setTimeout(() => {
                dialogVisible.value = false
            }, 1000)
            console.error('删除仓库失败:', error)
        }
    }
}

//处理编辑仓库成功函数
const handleEditSuccess = async (formData) => {
        try{
            await useWareHouseapi().update(formData)
            handleDialogProps('success', '成功', '仓库编辑成功')
            setTimeout(async () => {
                dialogVisible.value = false
                await refreshTableData() // 只刷新表格数据，不刷新整个页面
            }, 1000)
        }
        catch (error) {
            handleDialogProps('error', '失败', '仓库编辑失败')
            setTimeout(() => {
                dialogVisible.value = false
            }, 1000)
            console.error('编辑仓库失败:', error)
        }
}

//处理获取仓库数据函数
onMounted(async () => {
    try {
        const response = await useWareHouseapi().getall()
        tableData.value = Array.isArray(response) ? response :
            Array.isArray(response?.data) ? response.data : []
    } catch (error) {
        console.error('获取仓库数据失败:', error)
    }
})
</script>

<style scoped>
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