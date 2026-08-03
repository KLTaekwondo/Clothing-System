export function parseSkuSpecs(skuList) {
    return skuList.map(sku => {
        let parsedSpec = sku.spec || '{}'
        try {
            parsedSpec = JSON.parse(parsedSpec)
        } catch {
            parsedSpec = {}
        }
        return {
            ...sku,
            parsedSpec
        }
    })
}

export function buildSkuMatrix(specs, singleColumnLabel, createCell) {
    const dimensionKeys = [...new Set(specs.flatMap(item => Object.keys(item.parsedSpec)))]
    const rowKey = dimensionKeys[0]
    const columnKey = dimensionKeys[1]
    let rowHeaders = []
    let colHeaders = []

    if (dimensionKeys.length <= 1) {
        rowHeaders = rowKey
            ? [...new Set(specs.map(item => item.parsedSpec[rowKey] || '未知'))]
            : specs.map(item => `${item.name} (${item.code})`)
        colHeaders = [singleColumnLabel]
    } else {
        rowHeaders = [...new Set(specs.map(item => item.parsedSpec[rowKey] || '未知'))]
        colHeaders = [...new Set(specs.map(item => item.parsedSpec[columnKey] || '未知'))]
    }

    const cells = rowHeaders.map((row, rowIndex) => {
        return colHeaders.map(column => {
            const sku = findMatrixSku(specs, dimensionKeys, row, column, rowIndex)
            return createCell(sku)
        })
    })

    return {
        specs,
        dimensionKeys,
        rowLabel: rowKey || 'SKU',
        rowHeaders,
        colHeaders,
        cells
    }
}

export function createStockBySpec(records, valueFactory) {
    const stockBySpec = new Map()
    records.forEach(record => {
        stockBySpec.set(
            JSON.stringify(record.spec || {}),
            valueFactory(record)
        )
    })
    return stockBySpec
}

export function fillMatrixFromStock(matrix, stockBySpec, applyStock) {
    matrix.cells.forEach(row => {
        row.forEach(cell => {
            if (!cell.skuSpec) return
            const stock = stockBySpec.get(JSON.stringify(cell.skuSpec))
            if (stock !== undefined) {
                applyStock(cell, stock)
            }
        })
    })
}

function findMatrixSku(specs, dimensionKeys, row, column, rowIndex) {
    if (dimensionKeys.length === 0) {
        return specs[rowIndex] || null
    }
    if (dimensionKeys.length === 1) {
        return specs.find(item => item.parsedSpec[dimensionKeys[0]] === row) || null
    }
    return specs.find(item => {
        return item.parsedSpec[dimensionKeys[0]] === row &&
            item.parsedSpec[dimensionKeys[1]] === column
    }) || null
}
