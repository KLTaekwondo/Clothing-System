package com.superkl.backend.utils;

import com.superkl.backend.enums.ErrorCodeEnum;
import com.superkl.backend.enums.KindOfDirEnum;
import com.superkl.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class FileUploadUtil {
    @Value("${upload.path}")
    private String uploadPath;// 上传路径:实际上传硬盘的路径
    @Value("${access.path}")
    private String accessPath;// 访问路径:写入数据库

    // 每个类一份，而不是每一个实例一份
    private static final Set<String> ALLOWED = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    // 返回最后的访问路径
    public String uploadFile(MultipartFile file , KindOfDirEnum kindOfDirEnum) {
        if(file == null || file.isEmpty()){
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "文件不能为空");
        }

        // 获取文件名
        String fileName = file.getOriginalFilename();

        // 检查文件后缀是否为图片类型
        String suffix = isImageFile(fileName);

        // 检查无误后，重命名
        String newFileName = LocalDate.now() + "_" + UUID.randomUUID().toString().replace("-", "") + suffix;
        String urlPath = accessPath + kindOfDirEnum.getDir() + "/" + newFileName;
        String absolutePath = new File(uploadPath).getAbsolutePath();
        try{
            // 这里写的是实际硬盘地址，不是数据库访问地址
            File dir = new File(absolutePath,kindOfDirEnum.getDir());
            // 如果没有出现则创建目录
            if(!dir.exists()){
                dir.mkdirs();
            }

            // 出现了就尝试上传文件
            file.transferTo(new File(dir,newFileName));

            // 返回最后的访问路径:写入数据库的路径
            return urlPath;

        }catch (IOException e) {
            log.error("文件上传失败{}",fileName,e);
            throw new BusinessException(ErrorCodeEnum.SYSTEM_ERROR, "文件上传失败");
        }
    }

    private String isImageFile(String fileName) {
        if(fileName == null || !fileName.contains(".")){
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "请检查文件是否存在后缀名或者文件名是否为空");
        }
        // 获取文件后缀名
        String suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

        // 检查后缀名是否在允许列表中
        if(!ALLOWED.contains(suffix)){
            throw new BusinessException(ErrorCodeEnum.RULE_VALID_ERROR, "文件仅允许上传jpg/jpeg/png/gif/webp格式。");
        }

        return suffix;
    }

}

