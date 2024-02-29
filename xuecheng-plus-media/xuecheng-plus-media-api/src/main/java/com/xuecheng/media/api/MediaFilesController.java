package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDTO;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);
    }


    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDTO upload(@RequestPart("filedata") MultipartFile file,
                                      @RequestParam(value = "objectName", required = false) String objectName) throws Exception {
        Long companyId = 1232141425L;
        //准备上传文件信息
        UploadFileParamsDTO uploadFileParamsDTO = new UploadFileParamsDTO();
        //原始文件名
        uploadFileParamsDTO.setFilename(file.getOriginalFilename());
        //文件大小
        uploadFileParamsDTO.setFileSize(file.getSize());
        //文件类型
        uploadFileParamsDTO.setFileType("001001");
        //创建临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        //将file文件写入临时文件
        file.transferTo(tempFile);
        //获取临时文件的绝对路径
        String absoluteFile = tempFile.getAbsolutePath();
        //调用service上传文件
        return mediaFileService.uploadFile(companyId, uploadFileParamsDTO, absoluteFile,objectName);
    }



}
