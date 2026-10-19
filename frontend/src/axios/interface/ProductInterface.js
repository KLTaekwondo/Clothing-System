import productAPI from "../api/ProductAPI.js";

// ── 模块级工具（与业务方法分离） ──

// 解析后端返回的 JSON 文本
function parseResult(text) {
    try {
        return JSON.parse(text)
    } catch {
        return null
    }
}

function productInterface() {
    const create = async (data) => {
        await productAPI.createProduct(data);
        return true;
    }

    const update = async (id, data) => {
        await productAPI.updateProduct(id, data);
        return true;
    }

    const softDelete = async (id) => {
        await productAPI.deleteProduct(id);
        return true;
    }

    const search = async (id) => {
        const data = await productAPI.searchProduct(id);
        return data || {};
    }

    const searchPage = async (page = 0, size = 10) => {
        const data = await productAPI.searchProductPage(page, size);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            page: page,
            size,
        };
    }

    // 上传商品图片：fileService 默认 blob 响应，成功体读文本后解析；失败由拦截器统一提示
    const uploadImage = async (id, file) => {
        const response = await productAPI.uploadProductImage(id, file)
        const result = parseResult(await response.data.text())
        if (!result || result.code !== 200) {
            throw new Error(result?.msg || "图片上传失败")
        }
        return true
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchPage,
        uploadImage,
    }
}

export default productInterface();
