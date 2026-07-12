export const productOptionTypes = {
  PRODUCT_CATEGORY: 'PRODUCT_CATEGORY',
  PRODUCT_SUB_CATEGORY: 'PRODUCT_SUB_CATEGORY',
  PRODUCT_SEASON: 'PRODUCT_SEASON',
  PRODUCT_UNIT: 'PRODUCT_UNIT',
  PRODUCT_COMPOSITION: 'PRODUCT_COMPOSITION',
  SKU_COLOR: 'SKU_COLOR',
  SKU_SIZE: 'SKU_SIZE',
}

export const productOptionTypeOptions = [
  { value: productOptionTypes.PRODUCT_CATEGORY, label: '产品类别' },
  { value: productOptionTypes.PRODUCT_SUB_CATEGORY, label: '产品具体类别' },
  { value: productOptionTypes.PRODUCT_SEASON, label: '产品季节' },
  { value: productOptionTypes.PRODUCT_UNIT, label: '产品单位' },
  { value: productOptionTypes.PRODUCT_COMPOSITION, label: '产品成分' },
  { value: productOptionTypes.SKU_COLOR, label: 'SKU 颜色' },
  { value: productOptionTypes.SKU_SIZE, label: 'SKU 尺码' },
]

export const productOptionTypeLabels = productOptionTypeOptions.reduce((labels, item) => {
  labels[item.value] = item.label
  return labels
}, {})
