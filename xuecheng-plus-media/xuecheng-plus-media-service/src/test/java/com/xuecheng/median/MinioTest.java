package com.xuecheng.median;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder().endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin").build();


    @Test
    public void testUpload() throws Exception
    {

        minioClient.uploadObject( UploadObjectArgs.builder()
                .bucket("testbucket")
                .object("test/jpg/pic.jpg")
                .filename("D:\\Users\\dar06\\Pictures\\桌面背景.jpg")
                .build()
        );
    }


    @Test
    public void testDownload() throws Exception
    {
        GetObjectArgs testbucket = GetObjectArgs
                .builder()
                .bucket("testbucket")
                .object("test/jpg/pic.jpg")
                .build();

        FilterInputStream filterInputStream = minioClient.getObject(testbucket);
        FileOutputStream outputStream = new FileOutputStream("D:\\Users\\dar06\\Pictures\\uu.jpg");
        IOUtils.copy(filterInputStream, outputStream);

        String md5Hex = DigestUtils.md5Hex(filterInputStream);
        String local_md5Hex = DigestUtils.md5Hex(Files.newInputStream(Paths.get("D:\\Users\\dar06\\Pictures\\桌面背景.jpg")));
        if(md5Hex.equals(local_md5Hex)){
            System.out.println("文件成功下载");
        }else{
            System.out.println("文件信息不对应");
        }

    }
}
