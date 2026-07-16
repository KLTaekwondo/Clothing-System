<template>
    <div class="stock-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回仓库</button>
                <div><h2 class="page-title">库存管理</h2>
                    <p class="page-desc">{{ warehouse?.name || '仓库' }} 的 SKU 库存</p></div>
            </div>
            <button class="btn-outline" @click="showTransfer=true">库存转移</button>
        </div>
        <div class="card search-card">
            <div class="stock-search"><input v-model="productId" min="1" placeholder="输入商品 ID 查询库存"
                                             type="number"/>
                <button class="btn-primary" @click="fetchStock">查询库存</button>
            </div>
        </div>
        <div class="card table-card">
            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="stockList.length===0" class="empty-state">
                <div class="empty-icon">📊</div>
                <div class="empty-text">暂无库存数据</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>规格</th>
                    <th>库存数量</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(stock,index) in stockList" :key="stock.id||index">
                    <td>{{ formatSpec(stock.spec) }}</td>
                    <td><strong :class="{'stock-low':stock.stock<=10}">{{ stock.stock ?? 0 }}</strong></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div v-if="showTransfer" class="modal-overlay" @click.self="showTransfer=false">
            <div class="modal-content">
                <div class="modal-header"><span class="modal-title">库存转移</span>
                    <button class="modal-close" @click="showTransfer=false">&times;</button>
                </div>
                <form class="modal-body" @submit.prevent="handleTransfer">
                    <div class="form-group"><label>源仓库</label><select v-model="transferForm.sourceWarehouseId"
                                                                         required>
                        <option disabled value="">请选择源仓库</option>
                        <option v-for="item in warehouseList" :key="item.id" :value="item.id">{{ item.name }}</option>
                    </select></div>
                    <div class="form-group"><label>目标仓库</label><select v-model="transferForm.targetWarehouseId"
                                                                           required>
                        <option disabled value="">请选择目标仓库</option>
                        <option v-for="item in warehouseList" :key="item.id" :value="item.id">{{ item.name }}</option>
                    </select></div>
                    <div class="form-group"><label>SKU ID</label><input v-model.number="transferForm.skuId"
                                                                        min="1" required type="number"/></div>
                    <div class="form-group"><label>转移数量</label><input v-model.number="transferForm.stock"
                                                                          min="1" required type="number"/></div>
                </form>
                <div class="modal-footer">
                    <button class="btn-outline" @click="showTransfer=false">取消</button>
                    <button class="btn-primary" @click="handleTransfer">确认转移</button>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useToastStore} from '../../stores/toastStore.js';
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js';
import wareHouseStockInterface from '../../axios/interface/WareHouseStockInterface.js';

const route = useRoute();
const router = useRouter();
const toast = useToastStore();
const warehouse = ref(null);
const warehouseList = ref([]);
const stockList = ref([]);
const productId = ref('');
const loading = ref(false);
const showTransfer = ref(false);
const transferForm = ref({sourceWarehouseId: '', targetWarehouseId: '', skuId: '', stock: ''});
onMounted(async () => {
    try {
        const [current, list] = await Promise.all([wareHouseInterface.search(route.params.id), wareHouseInterface.searchList()]);
        warehouse.value = current;
        warehouseList.value = list
    } catch {
    }
});

async function fetchStock() {
    if (!productId.value) {
        toast.warning('请输入商品 ID');
        return
    }
    loading.value = true;
    try {
        stockList.value = await wareHouseStockInterface.searchStock(route.params.id, Number(productId.value))
    } catch {
        stockList.value = []
    } finally {
        loading.value = false
    }
}

function formatSpec(spec) {
    if (!spec) return '-';
    return Object.entries(spec).map(([key, value]) => `${key}:${value}`).join(' / ')
}

async function handleTransfer() {
    const form = transferForm.value;
    if (!form.sourceWarehouseId || !form.targetWarehouseId || !form.skuId || !form.stock) {
        toast.warning('请填写完整的转移信息');
        return
    }
    if (Number(form.sourceWarehouseId) === Number(form.targetWarehouseId)) {
        toast.warning('源仓库和目标仓库不能相同');
        return
    }
    try {
        await wareHouseStockInterface.transferStock({
            sourceWarehouseId: Number(form.sourceWarehouseId),
            targetWarehouseId: Number(form.targetWarehouseId),
            skuId: Number(form.skuId),
            stock: Number(form.stock)
        })
        showTransfer.value = false
    } catch {
    }
}

function goBack() {
    router.push('/manage/warehouse')
}
</script>
<style scoped>
.stock-page {
    width: 100%;
    min-width: 0
}

.page-heading, .heading-left {
    display: flex;
    align-items: center
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 28px
}

.heading-left {
    gap: 12px
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm)
}

.search-card {
    margin-bottom: 16px;
    border-radius: 16px
}

.stock-search {
    display: flex;
    gap: 10px
}

.stock-search input {
    width: 280px
}

.table-card {
    padding: 8px 20px 20px;
    border-radius: 16px
}

.stock-low {
    color: var(--error);
    font-weight: 800
}
</style>
