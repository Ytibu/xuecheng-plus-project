package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {

    List<MediaProcess> getMediaProcessListByMediaId(int shardIndex, int shardTotal, int count);

    boolean startTask(Long id);

    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
