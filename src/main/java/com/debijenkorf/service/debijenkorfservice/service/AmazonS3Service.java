package com.debijenkorf.service.debijenkorfservice.service;

import com.debijenkorf.service.debijenkorfservice.dtos.ImageDetailsResponse;

import java.io.File;

public interface AmazonS3Service {
    String uploadFileTos3bucket(String predefinedTypeName, File file);
    File downloadFileFromS3bucket(String fileName);
    void deleteS3BucketImage(String predefinedTypeName, String fileReference);
    ImageDetailsResponse s3BucketImageDetails (String predefinedTypeName, String reference);

}
