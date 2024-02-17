package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDTO;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    private String bucket_mediaFiles;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/") + "/";
    }


    /**
     * 根据文件拓展名获取文件类型
     *
     * @param extension 文件拓展名
     * @return 文件类型
     */
    private String getMimeType(String extension) {
        if(extension == null){
            extension = "";
        }
        ContentInfo mimeTypeMatch = ContentInfoUtil.findMimeTypeMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mineType,也叫字节流
        if ( mimeTypeMatch != null ) {
            mimeType = mimeTypeMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 上传文件到MinIO
     *
     * @param localFilePath 本地文件地址
     * @param mimeType      文件类型
     * @param bucketName    桶名称
     * @param objectName    对象名称
     * @return 是否上传成功
     * @throws Exception
     */
    private boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucketName, String objectName) throws Exception {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)         //指定桶
                    .object(objectName)//指定对象名
                    .filename(localFilePath)
                    .contentType(mimeType)//指定本地文件地址
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucketName, objectName);
            System.out.println("minio上传成功");
            return true;
        } catch (IllegalArgumentException e) {
            log.info("minio文件上传失败,bucket:{},objectName:{},错误信息:{}", bucketName, objectName, e.getMessage());
            e.printStackTrace();
            XueChengPlusException.cast("minio文件上传失败到文件系统失败");
        }
        return false;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将文件信息保存到数据库
     * @param companyId 机构ID
     * @param fileMd5 文件MD5
     * @param uploadFileParamsDTO 上传文件参数
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 保存的文件信息
     */
    @Transactional
    public MediaFiles addMediaFilesToDB(Long companyId, String fileMd5,
                                        UploadFileParamsDTO uploadFileParamsDTO, String bucketName, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if ( mediaFiles == null ) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDTO, mediaFiles);
            //文件ID
            mediaFiles.setId(fileMd5);
            //机构ID
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucketName);
            //文件路径
            mediaFiles.setFilePath(objectName);
            //fileID
            mediaFiles.setFileId(fileMd5);
            //访问路径url
            mediaFiles.setUrl("/" + bucketName + "/" + objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态
            mediaFiles.setAuditStatus("002003");
        }
        //插入数据库
        int insert = mediaFilesMapper.insert(mediaFiles);
        if(insert < 0){
            log.info("文件向数据库保存失败,objectName:{},bucketName:{}", objectName, bucketName);
            return null;
        }
        log.info("文件向数据库保存成功,objectName:{},bucketName:{}", objectName, bucketName);
        return mediaFiles;
    }

    @Override
    public UploadFileResultDTO uploadFile(Long companyId, UploadFileParamsDTO uploadFileParamsDto, String localFilePath) throws Exception {
        //1.文件上传到Minio
        String fileName = uploadFileParamsDto.getFilename();
        //获取文件拓展名
        String extension = StringUtils.substringAfterLast(fileName, ".");

        //根据拓展名获取媒体类型
        String mimeType = getMimeType(extension);

        String defaultFolderPath = getDefaultFolderPath();

        //获取MD5值
        String fileMd5 = getFileMd5(new File(localFilePath));

        //objectName以年月日作为名称存储
        String objectName = defaultFolderPath + fileMd5 + extension;

        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediaFiles, objectName);
        if(!result){
            XueChengPlusException.cast("文件上传失败");
        }
        //2.数据上传到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, bucket_mediaFiles, objectName);
        if(mediaFiles == null){
            XueChengPlusException.cast("文件上传后保存信息失败");
        }
        //准备返回的对象
        UploadFileResultDTO uploadFileResultDTO = new UploadFileResultDTO();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDTO);
        return uploadFileResultDTO;
    }
}
