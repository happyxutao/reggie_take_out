package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file)  {
        String originalFilename = file.getOriginalFilename();
        String suffix= originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName= UUID.randomUUID().toString()+suffix;
//        log.info(files.toString());
        //创建一个新目录
        File dir=new File(basePath);
        //如果目录不存在，就创建一个新的
        if(!dir.exists()){
            dir.mkdir();
        }
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将文件名返回
        return R.success(fileName);
    }
    /**
     * 文件下载
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response)  {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            int len=0;
            byte[] bytes = new byte[1024];
            while((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
