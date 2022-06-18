package com.debijenkorf.service.debijenkorfservice.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import com.debijenkorf.service.debijenkorfservice.dtos.ImageDetailsResponse;
import com.debijenkorf.service.debijenkorfservice.service.AmazonS3Service;

import com.debijenkorf.service.debijenkorfservice.utils.S3Utility;
import com.debijenkorf.service.debijenkorfservice.utils.ResizeImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service

public class AmazonS3ServiceImpl implements AmazonS3Service {
    private static String THUMB_NAIL = "thumbnail";
    private static String ORIGINAL = "original";
    private static String TMP_FOLDER = "tmp";
    private static String pathSeparator = FileSystems.getDefault().getSeparator();
    @Value("${application.bucket.name}")
    private String bucketName;
    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private S3Utility imageUtility;

    @Autowired
    private ResizeImage generateThumbnail;

    @Autowired
    private DownloadService downloadService;

    private static Logger logger = LoggerFactory.getLogger(AmazonS3ServiceImpl.class);

    @Override
    public String uploadFileTos3bucket(String predefinedTypeName, File file) {

        String keyName = predefinedTypeName + pathSeparator + imageUtility.s3KeyName(file.getName());
        System.out.println("Uploading file from " + file.getAbsoluteFile() + "   filename  = "+ file.getName() + "   keyName" + keyName);
        try {
            s3Client.putObject(new PutObjectRequest(bucketName, keyName, file));
            System.out.println("Uploaded file" + file.getName());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }

        return "File updated";
    }

    @Override
    public File downloadFileFromS3bucket (String fileName) {

        while(true) {
            String normalizedUrl = normalizeUrl(fileName);
            if (normalizedUrl.contains(THUMB_NAIL)) {
                return s3FileDownload(normalizedUrl);
            }
            else if (normalizedUrl.contains(ORIGINAL)) {
                File s3File = s3FileDownload(normalizedUrl);
                File resizedFile = generateThumbnail.resizedImage(s3File);
                uploadFileTos3bucket(THUMB_NAIL, resizedFile);
            }
            else if (normalizedUrl.isEmpty()) {
                File fileExternal = downloadService.download(fileName);
                uploadFileTos3bucket(ORIGINAL, fileExternal);
            }
        }
    }

    @Override
    public void deleteS3BucketImage(String predefinedTypeName, String fileReference) {
        String normalizeUrl = predefinedTypeName + pathSeparator + imageUtility.s3KeyName(fileReference);
        if (predefinedTypeName.equals(ORIGINAL)) {
            s3Client.deleteObject(bucketName, normalizeUrl);
            normalizeUrl = THUMB_NAIL + pathSeparator + imageUtility.s3KeyName(fileReference);
            int index = normalizeUrl.lastIndexOf('/');
            String directoryPrefix = normalizeUrl.substring(0, index);
            deleteS3Directory(directoryPrefix);
        }
        s3Client.deleteObject(bucketName, normalizeUrl);
    }

    @Override
    public ImageDetailsResponse s3BucketImageDetails (String predefinedTypeName, String reference) {
        String normalizeUrl = predefinedTypeName + pathSeparator + imageUtility.s3KeyName(reference);
        ImageDetailsResponse response = new ImageDetailsResponse();
        Path ticketTempDirPath = Paths.get(System.getProperty("java.io.tmpdir"));
        try {
            File ret = s3File(normalizeUrl, ticketTempDirPath);
            BufferedImage originalImage = ImageIO.read(ret);

            response.setImageType(String.valueOf(originalImage.getType()));
            response.setDimension(originalImage.getHeight() + " * " + originalImage.getHeight());
            response.setImageName(reference);
            response.setPredefinedImageType(predefinedTypeName);
            response.setImageSize(ret.length() / 1024 + " kb");
            response.setFolderPath(normalizeUrl);
            response.setBucketName(bucketName);

            ret.delete();

        } catch (IOException e) {
            throw new RuntimeException("Something went wrong");
        }

        return response;
    }
    private File s3FileDownload(String normalizeUrl) {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path ticketTempDirPath = Paths.get(tempDir + pathSeparator + ORIGINAL);
        try {
            return s3File(normalizeUrl, ticketTempDirPath);
        }
        catch(IOException io) {
            throw new RuntimeException("Something went wrong");
        }
    }
    private void deleteS3Directory(String prefix) {
        ObjectListing objectList = s3Client.listObjects( bucketName, prefix );
        List<S3ObjectSummary> objectSummeryList =  objectList.getObjectSummaries();
        String[] keysList = new String[ objectSummeryList.size() ];
        int count = 0;
        for( S3ObjectSummary summary : objectSummeryList ) {
            keysList[count++] = summary.getKey();
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest( bucketName ).withKeys( keysList );
        s3Client.deleteObjects(deleteObjectsRequest);
    }

    private String normalizeUrl(String fileName) {
        String normalizedFileName = imageUtility.s3KeyName(fileName);
        if (isObjectInBucket(normalizedFileName, THUMB_NAIL)) {
            return THUMB_NAIL + pathSeparator + normalizedFileName;
        }
        if (isObjectInBucket(normalizedFileName, ORIGINAL)) {
            return ORIGINAL + pathSeparator + normalizedFileName;
        }
        return "";
    }

    private boolean isObjectInBucket(String normalizedFileName, String predefinedTypeName) {
        String url = predefinedTypeName + pathSeparator + normalizedFileName;
        //Optimized function to include prefix
        ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            if (url.equals(os.getKey())) return true;
        }
        return false;
    }

    private File s3File(String normalizeUrl, Path ticketTempDirPath) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, normalizeUrl);
        S3ObjectInputStream s3is = s3Object.getObjectContent();

        Path filePath = Files.createDirectories(ticketTempDirPath);
        String fileName = normalizeUrl.contains("/") ? normalizeUrl.substring(normalizeUrl.lastIndexOf('/') + 1) : normalizeUrl;
        File file = new File(filePath.toFile(), fileName);

        BufferedOutputStream downloadedBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        StreamUtils.copy(s3is, downloadedBufferedOutputStream);
        s3is.close();
        downloadedBufferedOutputStream.close();
        return file;
    }
}
