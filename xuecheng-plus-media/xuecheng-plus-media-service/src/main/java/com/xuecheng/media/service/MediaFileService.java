package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDTO;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.vo.RestResponse;

import java.util.List;

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
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

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

    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkIndex, UploadFileParamsDTO uploadFileParamsDTO);
}
