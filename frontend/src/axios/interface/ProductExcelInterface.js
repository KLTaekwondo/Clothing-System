import productExcelAPI from "../api/ProductExcelAPI.js";

// ── 模块级工具（与业务方法分离） ──

// 解析后端返回的 JSON 文本
function parseResult(text) {
    try {
        return JSON.parse(text)
    } catch {
        return null
    }
}

// 解析 Content-Disposition 里的文件名
function parseFileName(disposition, fallback) {
    if (!disposition) return fallback;
    const star = disposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (star) {
        try {
            return decodeURIComponent(star[1]);
        } catch {
            return star[1];
        }
    }
    return fallback;
}

// 保存文件：data 是 responseType:blob 的原始字节，直接存
function saveBlob(data, fileName, type) {
    const blob = data instanceof Blob ? data : new Blob([data], {type: type});
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

// ── 导入业务接口 ──

function productExcelInterface() {

    // 下载模板
    const downloadTemplate = async () => {
        const response = await productExcelAPI.downloadTemplate();
        const fileName = parseFileName(
            response.headers["content-disposition"],
            "商品导入模板.xlsx"
        );
        saveBlob(response.data, fileName,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return true;
    }

    // 只校验：返回 { fatalErrors, errors }
    const validate = async (file) => {
        const response = await productExcelAPI.validate(file);
        const result = parseResult(await response.data.text());
        if (!result || result.code !== 200) {
            throw new Error(result?.msg || "检查失败");
        }
        return result.data;
    }

    // 导入：成功返回后端提示，失败由 fileService 拦截器统一 toast
    const importProducts = async (file, autoCreateMissing) => {
        const response = await productExcelAPI.importProducts(file, autoCreateMissing);
        const result = parseResult(await response.data.text());
        if (!result || result.code !== 200) {
            throw new Error(result?.msg || "导入失败");
        }
        return result;
    }

    return {
        downloadTemplate,
        validate,
        importProducts,
    }
}

export default productExcelInterface();
