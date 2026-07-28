<template>
    <img
        :src="icons[name]"
        alt=""
        aria-hidden="true"
    />
</template>

<script setup>
import productIcon from '../assets/icons/product.svg'
import userIcon from '../assets/icons/user.svg'
import warehouseIcon from '../assets/icons/warehouse.svg'
import orderIcon from '../assets/icons/order.svg'
import tagIcon from '../assets/icons/tag.svg'
import warningIcon from '../assets/icons/warning.svg'
import searchIcon from '../assets/icons/search.svg'
import transferIcon from '../assets/icons/transfer.svg'
import stockIcon from '../assets/icons/stock.svg'
import documentIcon from '../assets/icons/document.svg'
import saveIcon from '../assets/icons/save.svg'
import trashIcon from '../assets/icons/trash.svg'
import refundIcon from '../assets/icons/refund.svg'
import moneyIcon from '../assets/icons/money.svg'
import trendIcon from '../assets/icons/trend.svg'
import cartIcon from '../assets/icons/cart.svg'
import clockIcon from '../assets/icons/clock.svg'
import hourglassIcon from '../assets/icons/hourglass.svg'
import checkIcon from '../assets/icons/check.svg'
import receiptIcon from '../assets/icons/receipt.svg'
import supplierIcon from '../assets/icons/supplier.svg'

const icons = {
    product: productIcon,
    user: userIcon,
    warehouse: warehouseIcon,
    order: orderIcon,
    tag: tagIcon,
    warning: warningIcon,
    search: searchIcon,
    transfer: transferIcon,
    stock: stockIcon,
    document: documentIcon,
    save: saveIcon,
    trash: trashIcon,
    refund: refundIcon,
    money: moneyIcon,
    trend: trendIcon,
    cart: cartIcon,
    clock: clockIcon,
    hourglass: hourglassIcon,
    check: checkIcon,
    receipt: receiptIcon,
    supplier: supplierIcon
}

defineProps({
    name: {
        type: String,
        required: true
    }
})
</script>
