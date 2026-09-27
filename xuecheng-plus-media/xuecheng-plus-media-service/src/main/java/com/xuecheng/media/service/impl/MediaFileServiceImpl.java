package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;



    /**
     * 将文件写入minio
     * @param localFilePath 本地文件路径
     * @param mimeType 文件类型
     * @param bucket 文件桶
     * @param objectName 对象
     * @return 成功与否
     */
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket,
                                        String objectName)
    {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testbucket);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因： {}",bucket,objectName,e.getMessage(),e);
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
    }

    @Override
    public MediaFiles getFileById(String mediaLd) {
        return mediaFilesMapper.selectById(mediaLd);
    }

    /**
     * 将文件添加到文件表
     * @param companyId 机构ID
     * @param fileMd5 文件MD%
     * @param uploadFileParamsDto 上传文件参数
     * @param bucket 文件桶
     * @param objectName 对象
     * @return 文件
     */
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5,
            UploadFileParamsDTO uploadFileParamsDto, String bucket, String objectName)
    {
        // 1. 参数校验
        if (companyId == null || companyId <= 0) {
            log.error("公司ID无效: {}", companyId);
            XueChengPlusException.cast("公司ID无效");
        }
        if (fileMd5 == null || fileMd5.trim().isEmpty()) {
            log.error("文件MD5为空");
            XueChengPlusException.cast("文件MD5不能为空");
        }
        if (uploadFileParamsDto == null) {
            log.error("文件上传参数DTO为空");
            XueChengPlusException.cast("文件上传参数不能为空");
        }

        // 2. 从数据库查询文件（使用MD5作为主键）
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        // 3. 如果文件不存在，则创建新记录
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            // 3.1 拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            // 3.2 设置文件标识信息
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            // 3.3 设置公司信息
            mediaFiles.setCompanyId(companyId);
            // 3.4 设置存储信息
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            // 3.5 设置状态信息
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003"); // 审核状态：已审核
            mediaFiles.setStatus("1");           // 状态：正常
            // 3.6 保存到数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("保存文件信息到数据库失败，mediaFiles: {}", mediaFiles);
                XueChengPlusException.cast("保存文件信息失败");
            }
            // 添加待处理任务
            addWaitingTask(mediaFiles);
            log.debug("保存文件信息到数据库成功，fileMd5: {}, objectName: {}", fileMd5, objectName);
        } else {
            log.debug("文件已存在，fileMd5: {}", fileMd5);
        }

        return mediaFiles;
    }


    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto)
    {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //测试阶段：不分页，一次性返回全部媒资供前端选择
        List<MediaFiles> list = mediaFilesMapper.selectList(queryWrapper);
        // 构建结果集：总记录数与每页记录数都按实际返回条数填充，保证前端分页组件只显示一页
        return new PageResult<>(list, list.size(), 1, list.size());
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDTO uploadFileParamsDTO, String localFilePath, String objectName)
    {
        //文件名称
        String filename = uploadFileParamsDTO.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = getMimetype(extension);
        //文件的md5值
        String fileMd5 = fileMd5(new File(localFilePath));
        //文件的默认目录
        String defaultFolderPath = getDefaultFolderPath();
        //存储到minio中的对象名(带目录)
        if(StringUtils.isEmpty(objectName)){
            objectName = defaultFolderPath + fileMd5 + extension;
        }
        //将文件上传到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_files, objectName);
        if(!result){
            XueChengPlusException.cast("上传文件失败");
        }
        //文件大小
//        uploadFileParamsDTO.setFileSize(file.length());
        //将文件信息存储到数据库
        // 事务只包住数据库操作，不包住耗时的 MinIO 上传
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        String finalObjectName = objectName;
        MediaFiles mediaFiles = transactionTemplate.execute(status ->
                addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDTO, bucket_files, finalObjectName));
        if(mediaFiles == null){
            XueChengPlusException.cast("文件信息保存失败");
        }
        //准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Override
    public RestResponse<Boolean> checkFile(String md5Hex)
    {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(md5Hex);
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            InputStream inputStream;
            try {
                inputStream = minioClient.getObject(
                        GetObjectArgs.builder().bucket(bucket).object(filePath).build()
                );
                if (inputStream != null) {
                    return RestResponse.success(true);
                }
            }catch (Exception e){
                log.error("异常行为：{}", e.getMessage());
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String md5Hex, int chunkIndex)
    {
        String chunkFileFolderPath = getChunkFileFolderPath(md5Hex);
        GetObjectArgs objectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try {
            FilterInputStream object = minioClient.getObject(objectArgs);
            if(object != null){
                return RestResponse.success(true);
            }
        }catch (Exception e){

        }

        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String md5Hex, int chunkIndex, String loadChunkFilePath)
    {
        String mimetype = getMimetype(null);
        String chunkFilePath = getChunkFileFolderPath(md5Hex) + chunkIndex;
        boolean result = addMediaFilesToMinIO(loadChunkFilePath, mimetype, bucket_video, chunkFilePath);
        if(!result){
            log.error("文件上传失败");
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        log.warn("文件上传成功");
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDTO uploadFileParamsDTO)
    {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal).map(
                        i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build()
                ).collect(Collectors.toList()
                );

        String filename = uploadFileParamsDTO.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String objectName = getFilePath(fileMd5, extension);

        ComposeObjectArgs build = ComposeObjectArgs.builder().bucket(bucket_video).object(objectName).sources(sources).build();

        //=================文件合并=======================
        try {
            minioClient.composeObject(build);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件合并出现问题，bucket:{}, objectName:{}, 信息为:{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }

        //===================校验合并的文件与源文件是否一致
        File file = downloadFileFromMinIO(bucket_video, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if(!fileMd5.equals(md5Hex)){
                log.error("校验合并文件的md5值不一致，原始文件：{}，合并文件{}", md5Hex, objectName);
                return RestResponse.validfail(false, "文件校验失败");
            }
            // 文件大小设置
            uploadFileParamsDTO.setFileSize(file.length());
        }catch (Exception e){
            return RestResponse.validfail(false, "文件校验失败");
        }

        //=========================文件入库==============================
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        MediaFiles mediaFiles = transactionTemplate.execute(status ->
                addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDTO, bucket_video, objectName));
        if(mediaFiles == null){
            return RestResponse.validfail(false, "文件入库失败");
        }

        //==================清理中间的分块文件========================
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }



    /**
     * 从minIO下载文件到本地，进行对比
     * @param bucket 桶
     * @param objectName 文件对象
     * @return 文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName)
    {
        File minioFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
            minioFile = File.createTempFile("minio", ".merge");
            fileOutputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, fileOutputStream);
            return minioFile;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取默认文件存储路径（由日期确定）即：2026/09/01
     * @return 返回存储路径
     */
    private String getDefaultFolderPath()
    {
        return new SimpleDateFormat("yyyy-MM-dd")
                .format(new Date())
                .replace("-", "/")
                + "/";
    }

    /**
     * 获取文件MD5
     * @param file 文件
     * @return 文件MD5字符串
     */
    private String fileMd5(File file)
    {
        try (FileInputStream inputStream = new FileInputStream(file)){
            return DigestUtils.md5Hex(inputStream);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 由文件扩展名获取文件类型
     * @param extension 文件扩展名
     * @return 文件类型
     */
    private String getMimetype(String extension)
    {
        if (extension == null){
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 添加待处理任务
     * @param mediaFiles 文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles)
    {
        // 根据文件后缀获取文件类型
        String filename = mediaFiles.getFilename();
        String substring = filename.substring(filename.lastIndexOf("."));
        String mimetype = getMimetype(substring);

        // 处理指定类型的文件
        if(mimetype.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);

            // 设置信息保存到数据库中
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    /**
     * 清除需要合并的文件
     * @param chunkFileFolderPath 文件路径
     * @param chunkTotal 文件数量
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal)
    {
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(
                i -> new DeleteObject(chunkFileFolderPath + i)).collect(Collectors.toList());

        RemoveObjectsArgs build = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(build);
        results.forEach(result -> {
            try {
                DeleteError deleteError = result.get();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据文件md5和扩展名，确定文件目录
     * @param fileMd5
     * @param fileExtension
     * @return
     */
    private String getFilePath(String fileMd5, String fileExtension)
    {
        return fileMd5.charAt(0) + "/" +fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExtension;
    }

    /**
     * 根据文件的md5来确定文件目录
     * @param fileMD5
     * @return
     */
    private String getChunkFileFolderPath(String fileMD5)
    {
        return fileMD5.charAt(0) + "/" + fileMD5.charAt(1) + "/" + fileMD5 + "/chunk/";
    }
}
