package com.xuecheng.media;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

/**
 * @author Wwh
 * @ProjectName xuecheng-plus-project
 * @dateTime 2024/2/17 20:56
 * @description 大文件分块测试
 **/
public class BigFileTest {

    //分块测试
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("C:\\Users\\Wwhds\\Desktop\\分块测试\\test.mp4");

        String fileChunkPath = "C:\\Users\\Wwhds\\Desktop\\分块测试\\chunk\\";
        //分块大小
        int chunkSize = 1024 * 1024 * 5;
        //分块数量
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        RandomAccessFile ref_r = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] b = new byte[1024];
        for ( int i = 0; i < chunkNum; i++ ) {
            //创建分块文件
            File chunkFile = new File(fileChunkPath + i);
            //分块文件写入流
            RandomAccessFile ref_w = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            //读取文件
            while ( (len = ref_r.read(b)) != -1 ) {
                ref_w.write(b, 0, len);
                if ( chunkFile.length() >= chunkSize ) {
                    break;
                }
            }
            ref_w.close();
        }
        ref_r.close();
    }

    //合并测试
    @Test
    public void testMerge() throws Exception {
        //找到分块文件路径
        File chunkFolder = new File("C:\\Users\\Wwhds\\Desktop\\分块测试\\chunk");
        //源文件
        File sourceFile = new File("C:\\Users\\Wwhds\\Desktop\\分块测试\\test.mp4");
        //合并文件
        File mergeFile = new File("C:\\Users\\Wwhds\\Desktop\\分块测试\\test_merge.mp4");


        //取出所有分块文件
        File[] chunkFiles = chunkFolder.listFiles();

        List<File> list = null;
        if ( chunkFiles != null ) {
            list = Arrays.asList(chunkFiles);
        }
        //根据文件名称排序list
        if ( list != null ) {
            list.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
        }
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        byte b[] = new byte[1024];
        for ( File file : list ) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len;
            while ( (len = raf_r.read(b)) != -1 ) {
                raf_rw.write(b, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();
        System.out.println("合并完成");
        if ( DigestUtils.md5Hex(new FileInputStream(sourceFile)).equals(DigestUtils.md5Hex(new FileInputStream(mergeFile))) ) {
            System.out.println("文件一致");
        } else {
            System.out.println("文件不一致");
        }
    }

    @Test
    public void extension() {
        ContentInfo mimeTypeMatch = ContentInfoUtil.findExtensionMatch(".avi");
        System.out.println(mimeTypeMatch.getMimeType());
    }

    @Test
    public void whereisTempFile() throws IOException {
        File minio = File.createTempFile("minio", ".txt");
        System.out.println(minio.getAbsolutePath());
    }


}
