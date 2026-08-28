import exportAPI from "../api/ExportAPI.js";

// ── 模块级工具（与业务方法分离） ──

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
    const plain = disposition.match(/filename=([^;]+)/i);
    if (plain) return plain[1].replace(/^"|"$/g, "");
    return fallback;
}

// 保存文件：responseType:blob 时 data 已是原始字节 Blob（BOM 原样保留）直接用；
// 兜底兼容字符串（文本解码路径）时补 BOM，防止 Excel 按 GBK 读乱码
function saveBlob(data, fileName, type) {
    const blob = data instanceof Blob
        ? data
        : new Blob(['\uFEFF' + data], {type: type || "text/csv;charset=utf-8"});
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

// ── 导出业务接口 ──

function exportInterface() {
    // 期间订单总计 CSV
    const exportBetween = async (from, to) => {
        const response = await exportAPI.exportBetween(from, to);
        const fileName = parseFileName(response.headers["content-disposition"], `期间订单总计_${from}_${to}.csv`);
        saveBlob(response.data, fileName);
        return true;
    }

    // 商品列表 CSV
    const exportProducts = async () => {
        const response = await exportAPI.exportProducts();
        const fileName = parseFileName(response.headers["content-disposition"], "商品列表.csv");
        saveBlob(response.data, fileName);
        return true;
    }

    return {
        exportBetween,
        exportProducts,
    }
}

export default exportInterface();