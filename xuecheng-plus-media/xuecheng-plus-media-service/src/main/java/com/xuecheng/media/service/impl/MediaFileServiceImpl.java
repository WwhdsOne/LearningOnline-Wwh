package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDTO;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    MediaProcessMapper mediaProcessMapper;

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
        if ( extension == null ) {
            extension = "";
        }
        ContentInfo mimeTypeMatch = ContentInfoUtil.findExtensionMatch(extension);
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
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucketName, String objectName) throws Exception{
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)         //指定桶
                    .object(objectName)         //指定对象名
                    .filename(localFilePath)    //指定本地文件地址
                    .contentType(mimeType)      //指定文件类型
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

    @Override
    public void updateById(MediaFiles mediaFiles) {
        mediaFilesMapper.updateById(mediaFiles);
    }

    /**
     * 获取文件MD5
     * @param file 文件
     * @return 文件MD5
     */
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
     *
     * @param companyId           机构ID
     * @param fileMd5             文件MD5
     * @param uploadFileParamsDTO 上传文件参数
     * @param bucketName          桶名称
     * @param objectName          对象名称
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
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if ( insert <= 0 ) {
                log.error("文件向数据库保存失败,objectName:{},bucketName:{}", objectName, bucketName);
                XueChengPlusException.cast("文件向数据库保存失败");
                return null;
            }
            //记录待处理任务
            addWitingTask(mediaFiles);
            log.debug("保存文件信息到数据库成功,{}", mediaFiles);
        }
        return mediaFiles;
    }

    /**
     * 记录待处理任务
     * @param mediaFiles 文件信息
     */
    public void addWitingTask(MediaFiles mediaFiles) {
        //获取文件名
        String filename = mediaFiles.getFilename();
        //获取拓展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //获取mimeType类型
        String mimeType = getMimeType(extension);

        if(mimeType.equals("video/x-msvideo")){         //如果是avi文件，记录待处理任务
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            //状态
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    /**
     * 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return false不存在，true存在
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if ( mediaFiles != null ) {
            //数据库存在再查Minio
            //桶名称
            String bucket = mediaFiles.getBucket();
            //判断文件夹是否存在
            String objectName = mediaFiles.getFilePath();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();

            //获取远程流
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if ( inputStream != null ) {
                    //文件存在
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //文件不存在
            return RestResponse.success(false);
        }
        return RestResponse.success(false);
    }

    /**
     * 检查分块文件是否存在
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块索引
     * @return false不存在，true存在
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //根据MD5查询路径
        String chunkPath = getChunkFileFolderPath(fileMd5);
        //数据库存在再查Minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_videoFiles)
                .object(chunkPath + chunkIndex)
                .build();
        //获取远程流
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if ( inputStream != null ) {
                //文件存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //文件不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, String localPathChunk) throws Exception {

        boolean sucess = addMediaFilesToMinIO(localPathChunk,                //本地文件地址
                getMimeType(null),                //分块文件类型
                bucket_videoFiles,                           //桶名称
                getChunkFileFolderPath(fileMd5) + chunk     //对象名称
        );
        if ( !sucess ) {
            return RestResponse.validfail("分块文件上传失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDTO uploadFileParamsDTO) throws IOException {
        //分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //找到分块文件进行合并
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_videoFiles)
                        .object("chunkFileFolderPath" + i)
                        .build())
                .collect(Collectors.toList());
        //文件名称
        String fileName = uploadFileParamsDTO.getFilename();
        //文件扩展名
        String extName = fileName.substring(fileName.lastIndexOf("."));
        //合并文件路径
        String mergeFilePath = getChunkFileFolderPath(fileMd5, extName);

        //指定合并后的文件属性
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_videoFiles)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());

        try {
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucket_videoFiles)
                            .object(mergeFilePath)
                            .sources(sourceObjectList)
                            .build());
            log.debug("合并文件成功:{}", mergeFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("合并文件异常,fileMd5:{},异常:{}", fileMd5, e.getMessage());
            return RestResponse.validfail(false,"合并文件失败");
        }
        //=============下载文件
        File file = downloadFileFromMinIO(bucket_videoFiles, mergeFilePath);
        if(file == null){
            log.info("合并后文件下载失败,objectName:{},bucketName:{}", mergeFilePath, bucket_videoFiles);
            return RestResponse.validfail(false,"合并后文件下载失败");
        }
        //=============计算下载文件MD5是否和源文件相同
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            //下载成功
            String fileMd5AfterMerge = DigestUtils.md5Hex(fileInputStream);
            if ( !fileMd5.equals(fileMd5AfterMerge) ) {
                log.info("合并后文件MD5不一致,objectName:{},bucketName:{}", mergeFilePath, bucket_videoFiles);
                return RestResponse.validfail(false,"合并后文件MD5不一致");
            }
            //文件大小调整
            uploadFileParamsDTO.setFileSize(file.length());
        } catch (Exception e) {
            //下载失败
            e.printStackTrace();
            log.info("合并后文件MD5计算失败,objectName:{},bucketName:{}", mergeFilePath, bucket_videoFiles);
            return RestResponse.validfail(false,"合并后文件MD5计算失败");
        }finally {
            file.delete();
        }

        //============文件信息加入数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDTO, bucket_videoFiles, mergeFilePath);
        if ( mediaFiles == null ) {
            log.info("文件信息加入数据库失败,objectName:{},bucketName:{}", mergeFilePath, bucket_videoFiles);
            return RestResponse.validfail("文件信息加入数据库失败");
        }
        //=============清理分块文件
        cleanChunkFile(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }


    private void cleanChunkFile(String chunkFileFolderPath, int chunkTotal) {
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath + i))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket_videoFiles)
                .objects(objects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(result -> {
            try {
                if ( result.get() != null ) {
                    log.info("删除分块文件失败,objectName:{},bucketName:{}", result.get().objectName(), bucket_videoFiles);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 从minio下载文件
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ( outputStream != null ) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    //得到分块文件路径,MD5前两位
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1)  + "/chunk/";
    }

    //得到分块文件路径,MD5前两位,再加上扩展名
    private String getChunkFileFolderPath(String fileMd5, String fileExtension) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "." + fileExtension;
    }

    @Override
    public UploadFileResultDTO uploadFile(Long companyId, UploadFileParamsDTO uploadFileParamsDto, String localFilePath) throws Exception {
        //1.文件上传到Minio
        String fileName = uploadFileParamsDto.getFilename();
        //获取文件拓展名
        String extension = fileName.substring(fileName.lastIndexOf("."));

        //根据拓展名获取媒体类型
        String mimeType = getMimeType(extension);

        String defaultFolderPath = getDefaultFolderPath();

        //获取MD5值
        String fileMd5 = getFileMd5(new File(localFilePath));

        //objectName以年月日作为名称存储
        String objectName = defaultFolderPath + fileMd5 + extension;

        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediaFiles, objectName);
        if ( !result ) {
            XueChengPlusException.cast("文件上传失败");
        }
        //2.数据上传到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, bucket_mediaFiles, objectName);
        if ( mediaFiles == null ) {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }
        //准备返回的对象
        UploadFileResultDTO uploadFileResultDTO = new UploadFileResultDTO();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDTO);
        return uploadFileResultDTO;
    }
}
