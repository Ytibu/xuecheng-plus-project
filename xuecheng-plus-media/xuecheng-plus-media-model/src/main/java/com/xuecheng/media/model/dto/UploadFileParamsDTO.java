package com.xuecheng.media.model.dto;

import lombok.Data;

@Data
public class UploadFileParamsDTO {

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     *  标签
     */
    private String tags;

    /**
     * 用户
     */
    private String username;

    /**
     * 备注
     */
    private String remark;
}
