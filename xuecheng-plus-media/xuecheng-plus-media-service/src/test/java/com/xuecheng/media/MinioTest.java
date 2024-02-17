package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/16 21:37
 * @description Minio测试类
 **/
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void uploadTest() throws Exception {
        //根据拓展名获取媒体类型
        ContentInfo mimeTypeMatch = ContentInfoUtil.findMimeTypeMatch(".png");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mineType,也叫字节流
        if ( mimeTypeMatch != null ) {
            mimeType = mimeTypeMatch.getMimeType();
        }
        UploadObjectArgs asiatrip = UploadObjectArgs.builder()
                .bucket("testbucket")         //指定桶
                .object("logo.png")//指定对象名
                .filename("C:\\Users\\Wwhds\\Desktop\\logo\\logo-active.png")
                .contentType(mimeType)//指定本地文件地址
                .build();
        minioClient.uploadObject(asiatrip);
    }

    @Test
    public void delete() {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("logo.png").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    public void getFile() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("logo.png").build();
        //远程流
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        //指定输出流
        FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Wwhds\\Desktop\\Minio测试\\logo+test.png");
        IOUtils.copy(inputStream, outputStream);

        //校验文件完整性
        //不要传远程流
        String source_MD5 = DigestUtils.md5Hex(inputStream);
        String local_MD5 = DigestUtils.md5Hex(Files.newInputStream(Paths.get("C:\\Users\\Wwhds\\Desktop\\Minio测试\\logo+test.png")));
        if( source_MD5.equals(local_MD5) ){
            System.out.println("下载成功");
        }
    }
}
