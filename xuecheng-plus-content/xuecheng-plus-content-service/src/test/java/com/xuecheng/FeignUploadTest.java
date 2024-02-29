package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/29 15:30
 * @description 远程媒资服务接口测试
 **/

@SpringBootTest(classes = {ContentApplication.class})
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void testFeignUpload() throws Exception {
        // 上传文件
        File file = new File("D:\\Programming_Learning\\Project\\freemaker\\26.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        mediaServiceClient.upload(multipartFile, "course/26.html");
    }
    @Test
    public void test(){
        System.out.println(233);
    }
}
