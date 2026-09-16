package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.internal.org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 媒资文件管理接口
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

     @Autowired
     MediaFileService mediaFileService;

    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto)
    {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId,pageParams,queryMediaParamsDto);
    }


    @ApiOperation("上传文件接口")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile fileData) throws IOException
    {
        Log.debug("上传文件");
        UploadFileParamsDTO uploadFileParamsDTO = new UploadFileParamsDTO();
        uploadFileParamsDTO.setFilename(fileData.getOriginalFilename());
        uploadFileParamsDTO.setFileSize(fileData.getSize());
        uploadFileParamsDTO.setFileType("001001");
        File tempFile = File.createTempFile("minio", ".temp");
        fileData.transferTo(tempFile);
        Long companyId = 1232141425L;
        String localFilePath = tempFile.getAbsolutePath();
        return mediaFileService.uploadFile(companyId, uploadFileParamsDTO, localFilePath);
    }

}
