package com.debijenkorf.service.debijenkorfservice.controller;

import com.amazonaws.AmazonServiceException;
import com.debijenkorf.service.debijenkorfservice.exception.CustomException;
import com.debijenkorf.service.debijenkorfservice.dtos.PredefineTypeName;
import com.debijenkorf.service.debijenkorfservice.services.s3.AmazonS3Service;
import com.debijenkorf.service.debijenkorfservice.utils.ImageUtility;
import com.debijenkorf.service.debijenkorfservice.utils.ResizeImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/image")
public class AmazonS3Controller {
    private static final Logger LOG = LoggerFactory.getLogger(AmazonS3Controller.class);

    @Autowired
    private AmazonS3Service s3Service;

    @Autowired
    private ImageUtility imageUtility;

    @GetMapping(value= {"/show/{predefined-type-name}/{dummy-seo-name}","/show/{predefined-type-name}"})
    public ResponseEntity <?> s3BucketImage(@PathVariable("predefined-type-name") PredefineTypeName predefinedTypeName,
                                            @PathVariable(name = "dummy-seo-name", required = false) String dummySeoName,
                                            @RequestParam(name = "reference") String reference,
                                            HttpServletRequest request) {
        if (!imageUtility.imageValidation(reference)) throw new CustomException("The specified reference is not supported. File should be an image of the supported format png or jp(e)g");
        try {

           if(dummySeoName != null)  {
               return new ResponseEntity < > (s3Service.s3BucketImageDetails(predefinedTypeName.toString(), reference), HttpStatus.OK);
           }
           LOG.info("START - Retrieving " + reference + " from Amazon S3 server");
           File file = s3Service.downloadFileFromS3bucket(reference);

           Path path = FileSystems.getDefault().getPath(file.getAbsolutePath()).normalize();

           Resource resource = new UrlResource(path.toUri());
           String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
           LOG.info("END " + reference + " retrieved from Amazon S3 server..");
           return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename())
                    .body(resource);

        } catch(AmazonServiceException e) {
            LOG.info("The Specified key " + reference + " does not exist in the S3 bucket!");
            throw new CustomException("The Specified key " + reference + " does not exist in the S3 bucket!");
        } catch(MethodArgumentTypeMismatchException e) {
            LOG.info("The predefined type" + predefinedTypeName + " is incorrect! Allow values are  [thumbnails or original]");
            throw new CustomException("The predefined type" + predefinedTypeName + " is incorrect! Allow values are  [thumbnails or original]");
        } catch(IOException e){
            LOG.warn("Something went wrong, retry will be attempted");
            throw new CustomException("Something went wrong, retry will be attempted\"");
        } catch (Exception e) {
            throw new CustomException("OOOPS");
        }
   }
    @DeleteMapping("/flush/{predefined-type-name}")
    public void deleteImageInS3Bucket(@PathVariable("predefined-type-name") PredefineTypeName predefinedTypeName,
                                                 @RequestParam(name = "reference") String reference) {
        if (!imageUtility.imageValidation(reference)) throw new CustomException("The specified reference is not supported. File should be an image of the supported format png or jp(e)g");        try {
            s3Service.deleteS3BucketImage(predefinedTypeName.toString(), reference);
            ResponseEntity.ok();
        } catch (AmazonServiceException e) {
            LOG.info("The Specified key " + reference + " does not exist in the S3 bucket!");
            throw new CustomException("The Specified key does not exist in the S3 bucket!");
        }
        catch (Exception e) {
            throw new CustomException("OOOPS, something went wrong, could not delete the specified file");
        }
    }

    @PostMapping("/uploads/{predefined-type-name}")
    public ResponseEntity<String> uploadFile(@PathVariable("predefined-type-name") PredefineTypeName predefinedTypeName,
                                             @RequestParam("file") MultipartFile file) {
        if (!imageUtility.imageValidation(file.getName())) throw new CustomException("The specified reference is not supported. File should be an image of the supported format png or jp(e)g");

        try {
            return new ResponseEntity<>(s3Service.uploadFileTos3bucket(predefinedTypeName.toString(), imageUtility.convertMultipartToFile(file)), HttpStatus.CREATED);
        } catch(Exception e) {
            throw new CustomException("Something went wrong, could not upload file(s) to the server! Try again later");
        }
    }

    @PostMapping("/uploads/{predefined-type-name}/multiple")
    public List<ResponseEntity<String>> uploadMultipleFiles(@PathVariable("predefined-type-name") PredefineTypeName predefinedTypeName,
                                                            @RequestParam("files") MultipartFile files) {
        return Arrays
                .asList(files)
                .stream()
                .map(file -> uploadFile(predefinedTypeName, file))
                .collect(Collectors.toList());
    }
}