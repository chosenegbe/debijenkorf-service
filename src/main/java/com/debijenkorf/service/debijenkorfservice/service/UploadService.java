package com.debijenkorf.service.debijenkorfservice.service;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.debijenkorf.service.debijenkorfservice.dtos.ImageDetailsResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

public interface UploadService {
    String uploadFileTos3bucket(String predefinedTypeName, File file);
    File downloadFileFromS3bucket(String fileName);
    void deleteS3BucketImage(String predefinedTypeName, String fileReference);
    ImageDetailsResponse s3BucketImageDetails (String predefinedTypeName, String reference);

}
