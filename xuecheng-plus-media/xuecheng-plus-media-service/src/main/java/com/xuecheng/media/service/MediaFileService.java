package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.base.model.RestResponse;

import java.io.File;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

    /**
     * 媒资文件查询方法
     * @param pageParams 分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @author Mr.M
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 文件上传
     * @param companyId 机构ID
     * @param uploadFileParamsDTO 上传文件参数
     * @param loadFilePath 本地文件路径
     * @return 文件信息
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDTO uploadFileParamsDTO, String loadFilePath);


    /**
     * 检查该文件是否已经保存了
     * @param md5Hex 文件MD5
     * @return 检查结果
     */
    RestResponse<Boolean> checkFile(String md5Hex);


    /**
     * 检查该分块是否已经完成了上传
     * @param md5Hex 文件MD%
     * @param chunkIndex 分块索引
     * @return 检查结果
     */
    RestResponse<Boolean> checkChunk(String md5Hex, int chunkIndex);

    /**
     * 上传分块
     * @param md5Hex 分块md5
     * @param chunkIndex 分块索引
     * @param loadChunkFilePath 存储路径
     * @return 操作结果
     */
    RestResponse uploadChunk(String md5Hex, int chunkIndex, String loadChunkFilePath);

    /**
     * 合并
     * @param companyId 机构ID
     * @param fileMd5 文件MD5
     * @param chunkIndex 索引
     * @param uploadFileParamsDTO 上传文件信息
     * @return 结果
     */
    RestResponse mergeChunks(Long companyId, String fileMd5, int chunkIndex, UploadFileParamsDTO uploadFileParamsDTO);

    /**
     * 将minio中的文件下载下来
     * @param bucket 文件桶
     * @param objectName 文件对象
     * @return 下载的文件
     */
    File downloadFileFromMinIO(String bucket, String objectName);

    /**
     * 将文件上传到minio
     * @param localFilePath 本地文件地址
     * @param mimeType 文件类型
     * @param bucket 文件桶
     * @param objectName 文件对象
     * @return 是否成功
     */
    boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName);
}
