const ManageChildren = [
    {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../page/manage/dashboard/Dashboard.vue'),
        meta: {title: '仪表盘', icon: 'dashboard'}
    },
    {
        path: 'product',
        name: 'ProductManage',
        component: () => import('../page/manage/product/ProductManage.vue'),
        meta: {title: '商品管理', icon: 'product'}
    },
    {
        path: 'product/add',
        name: 'ProductAdd',
        component: () => import('../page/manage/product/ProductAdd.vue'),
        meta: {title: '添加商品', icon: 'product'}
    },
    {
        path: 'product/:id/sku/add',
        name: 'ProductSkuAdd',
        component: () => import('../page/manage/product/ProductSkuAdd.vue'),
        meta: {title: '添加 SKU', icon: 'product'}
    },
    {
        path: 'product/:productId/sku/:skuId/edit',
        name: 'ProductSkuEdit',
        component: () => import('../page/manage/product/ProductSkuEdit.vue'),
        meta: {title: '编辑 SKU', icon: 'product'}
    },
    {
        path: 'product/:productId/sku/:skuId',
        name: 'ProductSkuDetail',
        component: () => import('../page/manage/product/ProductSkuDetail.vue'),
        meta: {title: 'SKU 详情', icon: 'product'}
    },
    {
        path: 'product/:id/sku',
        name: 'ProductSkuManage',
        component: () => import('../page/manage/product/ProductSkuManage.vue'),
        meta: {title: 'SKU 管理', icon: 'product'}
    },
    {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../page/manage/product/ProductDetail.vue'),
        meta: {title: '商品详情', icon: 'product'}
    },
    {
        path: 'employee/add',
        name: 'EmployeeAdd',
        component: () => import('../page/manage/employee/EmployeeAdd.vue'),
        meta: {title: '添加员工', icon: 'employee'}
    },
    {
        path: 'employee/:id',
        name: 'EmployeeDetail',
        component: () => import('../page/manage/employee/EmployeeDetail.vue'),
        meta: {title: '员工详情', icon: 'employee'}
    },
    {
        path: 'employee',
        name: 'EmployeeManage',
        component: () => import('../page/manage/employee/EmployeeManage.vue'),
        meta: {title: '员工管理', icon: 'employee'}
    },
    {
        path: 'supplier/add',
        name: 'SupplierAdd',
        component: () => import('../page/manage/supplier/SupplierAdd.vue'),
        meta: {title: '添加供应商', icon: 'supplier'}
    },
    {
        path: 'supplier/:code',
        name: 'SupplierDetail',
        component: () => import('../page/manage/supplier/SupplierDetail.vue'),
        meta: {title: '供应商详情', icon: 'supplier'}
    },
    {
        path: 'supplier',
        name: 'SupplierManage',
        component: () => import('../page/manage/supplier/SupplierManage.vue'),
        meta: {title: '供应商管理', icon: 'supplier'}
    },
    {
        path: 'warehouse/add',
        name: 'WarehouseAdd',
        component: () => import('../page/manage/warehouse/WarehouseAdd.vue'),
        meta: {title: '添加仓库', icon: 'warehouse'}
    },
    {
        path: 'warehouse/:id',
        name: 'WarehouseDetail',
        component: () => import('../page/manage/warehouse/WarehouseDetail.vue'),
        meta: {title: '仓库详情', icon: 'warehouse'}
    },
    {
        path: 'warehouse/:id/stock',
        name: 'WarehouseStockManage',
        component: () => import('../page/manage/warehouse/WarehouseStockManage.vue'),
        meta: {title: '库存管理', icon: 'warehouse'}
    },
    {
        path: 'warehouse',
        name: 'WarehouseManage',
        component: () => import('../page/manage/warehouse/WarehouseManage.vue'),
        meta: {title: '仓库管理', icon: 'warehouse'}
    },
    {
        path: 'option/add',
        name: 'OptionAdd',
        component: () => import('../page/manage/option/OptionAdd.vue'),
        meta: {title: '添加选项值', icon: 'option'}
    },
    {
        path: 'stock/transfer',
        name: 'StockTransfer',
        component: () => import('../page/manage/stock/StockTransfer.vue'),
        meta: {title: '库存转移', icon: 'stock'}
    },
    {
        path: 'stock/record',
        name: 'StockRecordManage',
        component: () => import('../page/manage/stock/StockRecordManage.vue'),
        meta: {title: '库存记录', icon: 'stock'}
    },
    {
        path: 'stock-check',
        name: 'StockCheckManage',
        component: () => import('../page/manage/stockCheck/StockCheckManage.vue'),
        meta: {title: '库存盘点', icon: 'stock'}
    },
    {
        path: 'stock-check/add',
        name: 'StockCheckAdd',
        component: () => import('../page/manage/stockCheck/StockCheckAdd.vue'),
        meta: {title: '新建盘点单', icon: 'stock'}
    },
    {
        path: 'stock-check/:id/edit',
        name: 'StockCheckEdit',
        component: () => import('../page/manage/stockCheck/StockCheckEdit.vue'),
        meta: {title: '编辑盘点草稿', icon: 'stock'}
    },
    {
        path: 'stock-check/:id',
        name: 'StockCheckDetail',
        component: () => import('../page/manage/stockCheck/StockCheckDetail.vue'),
        meta: {title: '盘点单详情', icon: 'stock'}
    },
    {
        path: 'stock',
        name: 'StockManage',
        component: () => import('../page/manage/stock/StockManage.vue'),
        meta: {title: '人工库存调整', icon: 'stock'}
    },
    {
        path: 'import-order',
        name: 'ImportOrderManage',
        component: () => import('../page/manage/importOrder/ImportOrderManage.vue'),
        meta: {title: '采购订单', icon: 'order'}
    },
    {
        path: 'import-order/add',
        name: 'ImportOrderAdd',
        component: () => import('../page/manage/importOrder/ImportOrderAdd.vue'),
        meta: {title: '新建采购单', icon: 'order'}
    },
    {
        path: 'import-order/:id/edit',
        name: 'ImportOrderEdit',
        component: () => import('../page/manage/importOrder/ImportOrderEdit.vue'),
        meta: {title: '编辑采购草稿', icon: 'order'}
    },
    {
        path: 'import-order/:id',
        name: 'ImportOrderDetail',
        component: () => import('../page/manage/importOrder/ImportOrderDetail.vue'),
        meta: {title: '采购订单详情', icon: 'order'}
    },
    {
        path: 'transfer-order',
        name: 'TransferOrderManage',
        component: () => import('../page/manage/transferOrder/TransferOrderManage.vue'),
        meta: {title: '调拨订单', icon: 'stock'}
    },
    {
        path: 'transfer-order/:id',
        name: 'TransferOrderDetail',
        component: () => import('../page/manage/transferOrder/TransferOrderDetail.vue'),
        meta: {title: '调拨订单详情', icon: 'stock'}
    },
    {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('../page/manage/order/OrderDetail.vue'),
        meta: {title: '订单详情', icon: 'order'}
    },
    {
        path: 'order',
        name: 'OrderManage',
        component: () => import('../page/manage/order/OrderManage.vue'),
        meta: {title: '订单管理', icon: 'order'}
    },
    {
        path: 'option',
        name: 'OptionManage',
        component: () => import('../page/manage/option/OptionManage.vue'),
        meta: {title: '选项管理', icon: 'option'}
    }
]

export default ManageChildren
