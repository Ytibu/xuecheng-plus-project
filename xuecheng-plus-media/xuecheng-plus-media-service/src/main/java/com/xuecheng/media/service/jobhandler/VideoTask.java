package com.xuecheng.media.service.jobhandler;

import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.xuecheng.base.utils.Mp4VideoUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VideoTask {

    @Autowired
    private MediaFileProcessService mediaFileProcessService;
    @Autowired
    private MediaFileService mediaFileService;
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        // 查询任务
        List<MediaProcess> mediaProcesses = mediaFileProcessService.getMediaProcessListByMediaId(
                shardIndex, shardTotal, Runtime.getRuntime().availableProcessors());
        int size = mediaProcesses.size();
        if(size == 0){
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(size);

        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcesses.forEach(mediaProcess -> executorService.execute(() -> {
            try{
                Long taskId = mediaProcess.getId();
                boolean isOk = mediaFileProcessService.startTask(taskId);
                if (!isOk) {
                    log.debug("抢占任务失败，任务Id:{}", taskId);
                    return;
                }

                String fileId = mediaProcess.getFileId();
                String bucket = mediaProcess.getBucket();
                String objectName = mediaProcess.getFilePath();
                File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                if (file == null) {
                    log.error("下载视频错误，任务id{}，bucket:{}，objectName:{}", taskId, bucket, objectName);
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                    return;
                }

                String videoPath = file.getAbsolutePath();
                String mp4Name = fileId + ".mp4";
                File mp4File;
                try {
                    mp4File = File.createTempFile("minio", ".pm4");
                } catch (IOException e) {
                    log.error("创建临时文件异常：{}", e.getMessage());
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                    return;
                }
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, videoPath, mp4Name, mp4File.getAbsolutePath());
                String result = videoUtil.generateMp4();
                if (!result.equals("success")) {
                    log.error("视频转码失败：{}, bucket:{}, objectName:{}", result, bucket, objectName);
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                    return;
                }

                boolean isOk2 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                if (!isOk2) {
                    log.error("上传mp4到minio失败，taskId:{}, bucket:{}, objectName:{}", taskId, bucket, objectName);
                    return;
                }

                String url = getFilePath(fileId, ".mp4");
                mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "执行成功");
            }finally {
                countDownLatch.countDown();
            }
        }));

        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5, String fileExtension)
    {
        return fileMd5.charAt(0) + "/" +fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExtension;
    }
}
