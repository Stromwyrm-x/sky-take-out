package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/admin/common")
public class CommonController
{
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException
    {
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String url = aliOssUtil.upload(file.getBytes(), fileName);
        return Result.success(url);
    }

}
