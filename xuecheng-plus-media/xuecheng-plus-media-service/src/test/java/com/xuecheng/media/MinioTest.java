package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if ( source_MD5.equals(local_MD5) ) {
            System.out.println("下载成功");
        }
    }

    //将分块文件上传到Minio
    @Test
    public void testUploadChunk() throws Exception {
        for ( int i = 0; i <=41; i++ ) {
            UploadObjectArgs asiatrip = UploadObjectArgs.builder()
                    .bucket("testbucket")         //指定桶
                    .object("chunk/" + i)//指定对象名
                    .filename("C:\\Users\\Wwhds\\Desktop\\分块测试\\chunk\\" + i)
                    .build();
            minioClient.uploadObject(asiatrip);
            System.out.println("分块" + i + "上传成功");
        }
    }

    //调用minio接口合并分块
    @Test
    public void testMergeChunk() throws Exception {
//        List<ComposeSource> sources = new ArrayList<>();
//
//        for ( int i = 0; i < 41; i++ ) {
//            sources.add(ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build());
//        }
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(42)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .build())
                .collect(Collectors.toList());
        ComposeObjectArgs testbucket = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(sources)
                .build();
        minioClient.composeObject(testbucket);
    }

    //批量清理分块文件
}
