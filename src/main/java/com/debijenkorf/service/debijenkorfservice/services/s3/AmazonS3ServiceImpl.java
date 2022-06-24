package com.debijenkorf.service.debijenkorfservice.services.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

import com.debijenkorf.service.debijenkorfservice.dtos.ImageDetailsResponse;
import com.debijenkorf.service.debijenkorfservice.exception.CustomException;

import com.debijenkorf.service.debijenkorfservice.services.externalwebdownload.ExternalWebDownloadServiceImpl;
import com.debijenkorf.service.debijenkorfservice.utils.S3Utility;
import com.debijenkorf.service.debijenkorfservice.utils.ResizeImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

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
    private static final Logger LOG = LoggerFactory.getLogger(AmazonS3ServiceImpl.class);
    private static String thumbNail = "thumbnail";
    private static String original = "original";
    //private static String TMP_FOLDER = "tmp";
    private static String tempDir = System.getProperty("java.io.tmpdir");
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
    private ExternalWebDownloadServiceImpl externalWebDownloadService;

    @Autowired
    @Qualifier("restTemplateExternal")
    private RestTemplate restTemplateExternal;

    @Value("${external.server.url}")
    private String externalServerUrl;

    @Override
    public File downloadFileFromS3bucket (String fileName) {

        while(true) {
            String normalizedUrl = normalizeUrl(fileName);
            if (normalizedUrl.contains(thumbNail)) {
                return s3FileDownload(normalizedUrl);
            }
            else if (normalizedUrl.contains(original)) {
                LOG.info("Downloading original File " + fileName + " from s3 bucket!");
                File s3File = s3FileDownload(normalizedUrl);
                File resizedFile = generateThumbnail.resizedImage(s3File);
                LOG.info("Original File "+ fileName + " resized!");
                uploadFileTos3bucket(thumbNail, resizedFile);
            }
            else if (normalizedUrl.isEmpty()) {
                LOG.info("File not found in s3 bucket, attempting to download from external web server " + externalServerUrl);
                //File fileExternal = externalWebDownloadService.download(fileName);
                File fileExternal = restTemplateExternal.getForObject("http://EXTERNAL-DOWNLOAD-SERVICE/external-downloads/" + fileName, File.class);
                uploadFileTos3bucket(original, fileExternal);
            }
        }
    }

    @Override
    public String uploadFileTos3bucket(String predefinedTypeName, File file) {

        String keyName = predefinedTypeName + pathSeparator + imageUtility.s3KeyName(file.getName());
        LOG.info("START: Uploading file " + file.getName() + " to s3 bucket. Bucket name - " + bucketName + ", keyName - " + keyName);
        try {
            s3Client.putObject(new PutObjectRequest(bucketName, keyName, file));
            LOG.info("END: File " + file.getName() + " uploaded to s3 bucket. Bucket name - " + bucketName + ", keyName - " + keyName);
        } catch (AmazonServiceException e) {
            LOG.error(e.getErrorMessage());
        }
        return "File uploaded";
    }

    @Override
    public void deleteS3BucketImage(String predefinedTypeName, String fileReference) {
        String normalizeUrl = predefinedTypeName + pathSeparator + imageUtility.s3KeyName(fileReference);
        if (predefinedTypeName.equals(original)) {

            s3Client.deleteObject(bucketName, normalizeUrl);
            normalizeUrl = thumbNail + pathSeparator + imageUtility.s3KeyName(fileReference);
            int index = normalizeUrl.lastIndexOf('/');
            String directoryPrefix = normalizeUrl.substring(0, index);
            deleteS3Directory(directoryPrefix);
            LOG.info("The original file " + fileReference + " and its thumbnail references has been deleted from the s3 bucket");
        } else {
            s3Client.deleteObject(bucketName, normalizeUrl);
            LOG.info("The thumbnail file " + fileReference + " has been deleted");
        }
    }

    @Override
    public ImageDetailsResponse s3BucketImageDetails (String predefinedTypeName, String reference) {
        String normalizeUrl = predefinedTypeName + pathSeparator + imageUtility.s3KeyName(reference);
        ImageDetailsResponse response = new ImageDetailsResponse();
        Path ticketTempDirPath = Paths.get(tempDir);
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
            LOG.warn("Requested file could not be downloaded from s3 bucket");
            throw new CustomException("Requested file could not be downloaded from s3 bucket");
        }

        return response;
    }
    private File s3FileDownload(String normalizeUrl) {
        Path ticketTempDirPath = Paths.get(tempDir + pathSeparator + original);
        try {
            return s3File(normalizeUrl, ticketTempDirPath);
        }
        catch(IOException io) {
            LOG.warn("Requested file could not be downloaded from s3 bucket");
            throw new CustomException("Requested file could not be downloaded from s3 bucket");
        }
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
        LOG.info("Downloaded file " + file.getName() + " from s3 bucket. Bucket name - " + bucketName);
        return file;
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
        if (isObjectInBucket(normalizedFileName, thumbNail)) {
            return thumbNail + pathSeparator + normalizedFileName;
        }
        if (isObjectInBucket(normalizedFileName, original)) {
            return original + pathSeparator + normalizedFileName;
        }
        return "";
    }

    private boolean isObjectInBucket(String normalizedFileName, String predefinedTypeName) {
        String url = predefinedTypeName + pathSeparator + normalizedFileName;
        ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            if (url.equals(os.getKey())) return true;
        }
        return false;
    }
}
