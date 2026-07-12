const currentYear = new Date().getFullYear()
const toSelectOptions = (items = []) => items.map(item => ({ value: item.optionValue, label: item.optionLabel }))

const baseSection = {
  key: 'base',
  title: '基础信息',
  description: '先确认商品的编码、名称和分类信息，方便后续库存和销售模块统一引用。',
}

const saleSection = {
  key: 'sale',
  title: '销售属性',
  description: '维护年份、单位、价格和上下架状态。',
}

const detailSection = {
  key: 'detail',
  title: '扩展信息',
  description: '补充特价标记和产品成分等商品说明。',
}

export const createProductFormColumns = (options = {}) => [
  { Id: 'productCode', Label: '商品编码', required: true, section: baseSection, helper: '建议使用唯一编码，创建后用于识别商品。' },
  { Id: 'productName', Label: '商品名称', required: true, section: baseSection },
  { Id: 'productShortName', Label: '商品简称', section: baseSection, helper: '用于列表和小票中快速识别，可不填。' },
  { Id: 'productCategory', Label: '类别', type: 'select', required: true, options: toSelectOptions(options.categories), section: baseSection },
  { Id: 'productSubCategory', Label: '子类', type: 'select', required: true, options: toSelectOptions(options.subCategories), section: baseSection },
  { Id: 'productSeason', Label: '季节', type: 'select', required: true, options: toSelectOptions(options.seasons), section: saleSection },
  { Id: 'productYear', Label: '年份', type: 'number', required: true, defaultValue: currentYear, min: currentYear - 10, max: currentYear + 2, step: 1, section: saleSection },
  { Id: 'productUnit', Label: '单位', type: 'select', required: true, defaultValue: options.units?.[0]?.optionValue || '', options: toSelectOptions(options.units), section: saleSection },
  { Id: 'productPurchasePrice', Label: '进货价', type: 'number', required: true, min: 0, step: 0.01, section: saleSection, helper: '用于计算毛利参考，不能小于 0。' },
  { Id: 'productSalePrice', Label: '销售价', type: 'number', required: true, min: 0, step: 0.01, section: saleSection, helper: '门店销售开单默认使用该价格。' },
  { Id: 'productStatus', Label: '商品状态', type: 'select', required: true, defaultValue: true, section: saleSection },
  { Id: 'isSpecialProduct', Label: '是否特价', type: 'select', required: true, defaultValue: false, options: [{ value: true, label: '是' }, { value: false, label: '否' }], section: detailSection },
  { Id: 'productComposition', Label: '产品成分', type: 'select', required: true, options: toSelectOptions(options.compositions), section: detailSection, wide: true },
]

export const productFormColumns = createProductFormColumns()
export const optionItemsToSelectOptions = toSelectOptions
