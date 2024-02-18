package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDTO;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * @param uploadFileParamsDTO 上传文件参数
     * @param localFilePath       本地文件路径
     * @return com.xuecheng.media.model.dto.UploadFileResultDTO 上传文件结果
     * @description 上传文件方法
     * @date 2024/2/17 18:32
     */
    UploadFileResultDTO uploadFile(Long companyId, UploadFileParamsDTO uploadFileParamsDTO, String localFilePath) throws Exception;

    /**
     * @param companyId           企业id
     * @param fileMd5             文件md5
     * @param uploadFileParamsDTO 上传文件参数
     * @param bucketName          桶名称
     * @param objectName          对象名称
     * @return com.xuecheng.media.model.dto.UploadFileResultDTO 上传文件结果
     * @description 上传文件方法到数据库
     * @date 2024/2/17 20:34
     */
    MediaFiles addMediaFilesToDB(Long companyId, String fileMd5, UploadFileParamsDTO uploadFileParamsDTO, String bucketName, String objectName);

    /**
     * @description 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @author Mr.M
     * @date 2022/9/13 15:38
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * @description 检查分块文件是否存在
     * @param fileMd5 文件的md5
     * @param chunkIndex 分块索引
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     */

    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * @description 上传分块文件
     * @param localPathChunk 文件
     * @param fileMd5 文件md5
     * @param chunk 分块索引
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> true成功，false失败
     */
    RestResponse<Boolean> uploadChunk(String fileMd5, int chunk,String localPathChunk) throws Exception;

    /**
     * @description 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDTO 文件信息
     * @return com.xuecheng.base.model.RestResponse
     * @author Mr.M
     * @date 2022/9/13 15:56
     */
    RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDTO uploadFileParamsDTO) throws IOException;


}
