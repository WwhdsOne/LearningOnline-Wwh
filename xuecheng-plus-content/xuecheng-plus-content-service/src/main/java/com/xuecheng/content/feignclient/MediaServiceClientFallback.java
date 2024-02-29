package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/29 19:34
 * @description 远程媒资服务接口熔断
 **/
public class MediaServiceClientFallback implements MediaServiceClient{
    @Override
    public String upload(MultipartFile file, String objectName) throws Exception {
        return null;
    }
}
