package com.xuecheng.media.api;

import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Api(value = "大文件上传接口", tags = "大文件上传接口")
@Slf4j
@RestController
public class BigFilesController {

    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        return mediaFileService.checkFile(fileMd5);
    }

    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
        @RequestParam("chunk") int chunk) {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file") MultipartFile file,
        @RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) throws Exception
    {
        File tempFile = File.createTempFile("minio", "temp");
        file.transferTo(tempFile);
        String absolutePath = tempFile.getAbsolutePath();
        return mediaFileService.uploadChunk(fileMd5, chunk, absolutePath);
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5,
        @RequestParam("fileName") String fileName, @RequestParam("chunkTotal") int chunkTotal) {


        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            throw new RuntimeException("未获取到用户身份，请重新登录"); // 或走统一异常/401
        }
        Long companyId = StringUtils.isNotEmpty(user.getCompanyId()) ? Long.parseLong(user.getCompanyId()) : null;
        UploadFileParamsDTO uploadFileParamsDTO = new UploadFileParamsDTO();
        uploadFileParamsDTO.setFilename(fileName);
        uploadFileParamsDTO.setTags("测试视频");
        uploadFileParamsDTO.setFileType("001002");

        return mediaFileService.mergeChunks(companyId, fileMd5, chunkTotal, uploadFileParamsDTO);
    }
}
