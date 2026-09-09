package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XuechengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.vo.RestResponse;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */

@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    /**
     * 获取默认文件路径
     * @return 返回存储路径
     */
    private String getDefaultFolderPath()
    {
        return new SimpleDateFormat("yyyy-MM-dd")
                .format(new Date())
                .replace("-", "/")
                + "/";
    }

    private String fileMd5(File file)
    {
        try (FileInputStream inputStream = new FileInputStream(file)){
            return DigestUtils.md5Hex(inputStream);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

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
            XuechengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
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
            XuechengPlusException.cast("公司ID无效");
        }
        if (fileMd5 == null || fileMd5.trim().isEmpty()) {
            log.error("文件MD5为空");
            XuechengPlusException.cast("文件MD5不能为空");
        }
        if (uploadFileParamsDto == null) {
            log.error("文件上传参数DTO为空");
            XuechengPlusException.cast("文件上传参数不能为空");
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
                XuechengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功，fileMd5: {}, objectName: {}", fileMd5, objectName);
        } else {
            log.debug("文件已存在，fileMd5: {}, 直接返回已有记录", fileMd5);
        }

        return mediaFiles;
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto)
    {

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
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDTO uploadFileParamsDTO, String localFilePath)
    {
        File file = new File(localFilePath);
        if (!file.exists()) {
            XuechengPlusException.cast("文件不存在");
        }
        //文件名称
        String filename = uploadFileParamsDTO.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = getMimetype(extension);
        //文件的md5值
        String fileMd5 = fileMd5(file);
        //文件的默认目录
        String defaultFolderPath = getDefaultFolderPath();
        //存储到minio中的对象名(带目录)
        String objectName = defaultFolderPath + fileMd5 + extension;
        //将文件上传到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_files, objectName);
        if(!result){
            XuechengPlusException.cast("上传文件失败");
        }
        //文件大小
        uploadFileParamsDTO.setFileSize(file.length());
        //将文件信息存储到数据库
        // 事务只包住数据库操作，不包住耗时的 MinIO 上传
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        MediaFiles mediaFiles = transactionTemplate.execute(status ->
                addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDTO, bucket_files, objectName));
        if(mediaFiles == null){
            XuechengPlusException.cast("文件信息保存失败");
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
            log.error("分块检查异常{}", e.getMessage());
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
        String objectName = getFilePathByMd5(fileMd5, extension);

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

    private String getFilePathByMd5(String fileMd5, String fileExtension)
    {
        return fileMd5.charAt(0) + "/" +fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExtension;
    }
    private String getChunkFileFolderPath(String fileMD5)
    {
        return fileMD5.charAt(0) + "/" + fileMD5.charAt(1) + "/" + fileMD5 + "/chunk/";
    }
}
