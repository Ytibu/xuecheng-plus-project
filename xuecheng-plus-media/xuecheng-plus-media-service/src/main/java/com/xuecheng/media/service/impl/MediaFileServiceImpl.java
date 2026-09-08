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
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
            return "";
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
                                        String objectName) {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(testbucket);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            System.out.println("上传成功");
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
}
