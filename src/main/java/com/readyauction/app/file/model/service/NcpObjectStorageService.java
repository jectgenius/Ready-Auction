package com.readyauction.app.file.model.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.readyauction.app.file.model.dto.FileDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NcpObjectStorageService {
    private static final Logger log = LoggerFactory.getLogger(NcpObjectStorageService.class);
    private final AmazonS3Client amazonS3Client;

    @Value("${spring.s3.bucket}")
    private String bucketName;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String getUuidFileName(String fileName) {
        String ext = fileName.substring(fileName.indexOf(".") + 1);
        return UUID.randomUUID().toString() + "." + ext;
    }

    // 업로드 시 ncp에서 폴더 생성 후 폴더명 넣기
    public List<FileDto> uploadFilesSample(List<MultipartFile> multipartFiles){
        return uploadFiles(multipartFiles, "sample-folder");
    }

    // NOTICE: filePath의 맨 앞에 /는 안 붙여도 됨 ex) history/images
    public List<FileDto> uploadFiles(List<MultipartFile> multipartFiles, String filePath) {

        List<FileDto> s3files = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {

            String originalFileName = multipartFile.getOriginalFilename();
            String uploadFileName = getUuidFileName(originalFileName);
            String uploadFileUrl = "";
            String keyName = filePath + "/" + uploadFileName;

            // Check if the file already exists in S3
            if (amazonS3Client.doesObjectExist(bucketName, keyName)) {
                System.out.println("사진 이미 올라가있음");
                // File exists, get its URL
                uploadFileUrl = "https://kr.object.ncloudstorage.com/" + bucketName + "/" + keyName;
            } else {
                // File does not exist, proceed to upload
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(multipartFile.getSize());
                objectMetadata.setContentType(multipartFile.getContentType());

                try (InputStream inputStream = multipartFile.getInputStream()) {

                    // Upload the file to S3
                    amazonS3Client.putObject(
                            new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata)
                                    .withCannedAcl(CannedAccessControlList.PublicRead));

                    // Set the URL of the uploaded file
                    uploadFileUrl = "https://kr.object.ncloudstorage.com/" + bucketName + "/" + keyName;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Add file information to the list
            s3files.add(
                    FileDto.builder()
                            .originalFileName(originalFileName)
                            .uploadFileName(uploadFileName)
                            .uploadFilePath(filePath)
                            .uploadFileUrl(uploadFileUrl)
                            .build());
        }

        return s3files;
    }

    // ncp에 업로드한 이미지들 전체 불러오기
    public List<FileDto> listFiles(String filePath) {
        List<FileDto> files = new ArrayList<>();
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(filePath + "/");
        ListObjectsV2Result result;

        do {
            result = amazonS3Client.listObjectsV2(req);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                String key = objectSummary.getKey();
                String fileName = key.substring(key.lastIndexOf("/") + 1);

                files.add(FileDto.builder()
                        .originalFileName(fileName)
                        .uploadFileName(fileName)
                        .uploadFilePath(filePath) // 현재 지정한 폴더만 가져옴
                        .uploadFileUrl("https://kr.object.ncloudstorage.com/" + bucketName + "/" + key)
                        .build());
            }
            req.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return files;
    }

    // ncp에 업로드한 파일 삭제
    public void deleteFile(String filePath, String fileName) {
        String key = filePath + "/" + fileName;
        try {
            amazonS3Client.deleteObject(bucketName, key);
            System.out.println("파일이 성공적으로 삭제되었습니다.");
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            System.out.println("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
