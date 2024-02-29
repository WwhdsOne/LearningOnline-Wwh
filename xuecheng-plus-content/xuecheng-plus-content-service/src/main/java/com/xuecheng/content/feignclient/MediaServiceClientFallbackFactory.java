package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/29 19:37
 * @description 远程媒资服务接口熔断工厂
 **/
@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    //发生熔断时上级服务会调用此方法
    //拿到当时熔断异常信息
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile file, String objectName) throws Exception {
                log.info("远程调用文件上传接口触发熔断异常信息：{}",throwable.toString(),throwable);
                return null;
            }
        };
    }


}
