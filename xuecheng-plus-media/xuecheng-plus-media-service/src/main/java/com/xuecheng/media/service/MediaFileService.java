package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDTO;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * @description 上传文件方法
     * @param uploadFileParamsDTO 上传文件参数
     * @param localFilePath 本地文件路径
     * @return com.xuecheng.media.model.dto.UploadFileResultDTO 上传文件结果
     * @date 2024/2/17 18:32
     */
    UploadFileResultDTO uploadFile(Long companyId, UploadFileParamsDTO uploadFileParamsDTO, String localFilePath) throws Exception;

    /**
     * @description 上传文件方法到数据库
     * @param companyId 企业id
     * @param fileMd5 文件md5
     * @param uploadFileParamsDTO 上传文件参数
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return com.xuecheng.media.model.dto.UploadFileResultDTO 上传文件结果
     * @date 2024/2/17 20:34
     */
    MediaFiles addMediaFilesToDB(Long companyId, String fileMd5, UploadFileParamsDTO uploadFileParamsDTO, String bucketName, String objectName) ;


}
