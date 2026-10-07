package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 媒资文件管理接口
 */
@Slf4j
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    @Autowired
    private MediaFileService mediaFileService;

    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto)
    {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        return mediaFileService.queryMediaFiles(companyId,pageParams,queryMediaParamsDto);
    }

    @ApiOperation("媒资文件删除接口")
    @DeleteMapping("/{mediaFileId}")
    public RestResponse delete(@PathVariable String mediaFileId)
    {
        log.info("删除媒资文件，mediaFileId:{}", mediaFileId);
        return mediaFileService.deleteMediaFile(mediaFileId);
    }


    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile fileData,
        @RequestParam(value= "objectName",required=false) String objectName) throws IOException
    {
        UploadFileParamsDTO uploadFileParamsDTO = new UploadFileParamsDTO();
        uploadFileParamsDTO.setFilename(fileData.getOriginalFilename());
        uploadFileParamsDTO.setFileSize(fileData.getSize());
        uploadFileParamsDTO.setFileType("001001");
        File tempFile = File.createTempFile("minio", ".temp");
        fileData.transferTo(tempFile);

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        String localFilePath = tempFile.getAbsolutePath();

        // 上传图片
        return mediaFileService.uploadFile(companyId, uploadFileParamsDTO, localFilePath, objectName);
    }

}
