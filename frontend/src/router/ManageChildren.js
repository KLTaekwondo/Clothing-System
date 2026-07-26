const ManageChildren = [
    {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../page/manage/Dashboard.vue'),
        meta: {title: '仪表盘', icon: 'dashboard'}
    },
    {
        path: 'product',
        name: 'ProductManage',
        component: () => import('../page/manage/ProductManage.vue'),
        meta: {title: '商品管理', icon: 'product'}
    },
    {
        path: 'product/add',
        name: 'ProductAdd',
        component: () => import('../page/manage/ProductAdd.vue'),
        meta: {title: '添加商品', icon: 'product'}
    },
    {
        path: 'product/:id/sku/add',
        name: 'ProductSkuAdd',
        component: () => import('../page/manage/ProductSkuAdd.vue'),
        meta: {title: '添加 SKU', icon: 'product'}
    },
    {
        path: 'product/:productId/sku/:skuId/edit',
        name: 'ProductSkuEdit',
        component: () => import('../page/manage/ProductSkuEdit.vue'),
        meta: {title: '编辑 SKU', icon: 'product'}
    },
    {
        path: 'product/:productId/sku/:skuId',
        name: 'ProductSkuDetail',
        component: () => import('../page/manage/ProductSkuDetail.vue'),
        meta: {title: 'SKU 详情', icon: 'product'}
    },
    {
        path: 'product/:id/sku',
        name: 'ProductSkuManage',
        component: () => import('../page/manage/ProductSkuManage.vue'),
        meta: {title: 'SKU 管理', icon: 'product'}
    },
    {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../page/manage/ProductDetail.vue'),
        meta: {title: '商品详情', icon: 'product'}
    },
    {
        path: 'employee/add',
        name: 'EmployeeAdd',
        component: () => import('../page/manage/EmployeeAdd.vue'),
        meta: {title: '添加员工', icon: 'employee'}
    },
    {
        path: 'employee/:id',
        name: 'EmployeeDetail',
        component: () => import('../page/manage/EmployeeDetail.vue'),
        meta: {title: '员工详情', icon: 'employee'}
    },
    {
        path: 'employee',
        name: 'EmployeeManage',
        component: () => import('../page/manage/EmployeeManage.vue'),
        meta: {title: '员工管理', icon: 'employee'}
    },
    {
        path: 'warehouse/add',
        name: 'WarehouseAdd',
        component: () => import('../page/manage/WarehouseAdd.vue'),
        meta: {title: '添加仓库', icon: 'warehouse'}
    },
    {
        path: 'warehouse/:id',
        name: 'WarehouseDetail',
        component: () => import('../page/manage/WarehouseDetail.vue'),
        meta: {title: '仓库详情', icon: 'warehouse'}
    },
    {
        path: 'warehouse/:id/stock',
        name: 'WarehouseStockManage',
        component: () => import('../page/manage/WarehouseStockManage.vue'),
        meta: {title: '库存管理', icon: 'warehouse'}
    },
    {
        path: 'warehouse',
        name: 'WarehouseManage',
        component: () => import('../page/manage/WarehouseManage.vue'),
        meta: {title: '仓库管理', icon: 'warehouse'}
    },
    {
        path: 'option/add',
        name: 'OptionAdd',
        component: () => import('../page/manage/OptionAdd.vue'),
        meta: {title: '添加选项值', icon: 'option'}
    },
    {
        path: 'stock/transfer',
        name: 'StockTransfer',
        component: () => import('../page/manage/StockTransfer.vue'),
        meta: {title: '库存转移', icon: 'stock'}
    },
    {
        path: 'stock/record',
        name: 'StockRecordManage',
        component: () => import('../page/manage/StockRecordManage.vue'),
        meta: {title: '库存记录', icon: 'stock'}
    },
    {
        path: 'stock',
        name: 'StockManage',
        component: () => import('../page/manage/StockManage.vue'),
        meta: {title: '库存管理', icon: 'stock'}
    },
    {
        path: 'import-order',
        name: 'ImportOrderManage',
        component: () => import('../page/manage/ImportOrderManage.vue'),
        meta: {title: '采购订单', icon: 'order'}
    },
    {
        path: 'import-order/add',
        name: 'ImportOrderAdd',
        component: () => import('../page/manage/ImportOrderAdd.vue'),
        meta: {title: '新建采购单', icon: 'order'}
    },
    {
        path: 'import-order/:id',
        name: 'ImportOrderDetail',
        component: () => import('../page/manage/ImportOrderDetail.vue'),
        meta: {title: '采购订单详情', icon: 'order'}
    },
    {
        path: 'transfer-order',
        name: 'TransferOrderManage',
        component: () => import('../page/manage/TransferOrderManage.vue'),
        meta: {title: '调拨订单', icon: 'stock'}
    },
    {
        path: 'transfer-order/:id',
        name: 'TransferOrderDetail',
        component: () => import('../page/manage/TransferOrderDetail.vue'),
        meta: {title: '调拨订单详情', icon: 'stock'}
    },
    {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('../page/manage/OrderDetail.vue'),
        meta: {title: '订单详情', icon: 'order'}
    },
    {
        path: 'order',
        name: 'OrderManage',
        component: () => import('../page/manage/OrderManage.vue'),
        meta: {title: '订单管理', icon: 'order'}
    },
    {
        path: 'option',
        name: 'OptionManage',
        component: () => import('../page/manage/OptionManage.vue'),
        meta: {title: '选项管理', icon: 'option'}
    }
]

export default ManageChildren
