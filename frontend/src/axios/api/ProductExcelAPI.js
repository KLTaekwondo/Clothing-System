import fileService from "../fileService.js";

// 商品导入相关接口
// 全部走 fileService：模板下载是文件流，validate/import 是文件上传
const productExcelAPI = {

    // 下载导入模板：返回文件流
    downloadTemplate: () => fileService.get("/product-excel/template"),

    // 校验：上传文件，返回 JSON（fileService 默认 blob，需要读文本解析）
    validate: (file) => {
        const formData = new FormData();
        formData.append("file", file);
        return fileService.post("/product-excel/validate", formData);
    },

    // 导入：autoCreateMissing 控制是否自动创建缺失的规格值
    importProducts: (file, autoCreateMissing) => {
        const formData = new FormData();
        formData.append("file", file);
        return fileService.post(
            `/product-excel/import?autoCreateMissing=${autoCreateMissing}`,
            formData
        );
    },
}

export default productExcelAPI;
