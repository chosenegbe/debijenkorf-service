package com.debijenkorf.service.debijenkorfservice.controller;

import com.amazonaws.AmazonServiceException;
import com.debijenkorf.service.debijenkorfservice.exception.CustomException;
import com.debijenkorf.service.debijenkorfservice.dtos.PredefineTypeName;
import com.debijenkorf.service.debijenkorfservice.service.AmazonS3Service;
import com.debijenkorf.service.debijenkorfservice.utils.S3Utility;
import lombok.extern.slf4j.Slf4j;
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
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/image")
@Slf4j
public class AmazonS3Controller {

    @Autowired
    private AmazonS3Service uploadService;

    @Autowired
    private S3Utility s3Utility;

    @GetMapping(value= {"/show/{predefined-type-name}/{dummy-seo-name}","/show/{predefined-type-name}"})
    public ResponseEntity <?> s3BucketImage(@PathVariable("predefined-type-name") PredefineTypeName predefinedTypeName,
                                            @PathVariable(name = "dummy-seo-name", required = false) String dummySeoName,
                                            @RequestParam(name = "reference") String fileName,
                                            HttpServletRequest request) {

        try {

           if(dummySeoName != null)  {
               return new ResponseEntity < > (uploadService.s3BucketImageDetails(predefinedTypeName.toString(), fileName), HttpStatus.OK);
           }
           File file = uploadService.downloadFileFromS3bucket(fileName);
           System.out.println(file.getName());
           System.out.println(file.getAbsoluteFile());
           Path path = FileSystems.getDefault().getPath(file.getAbsolutePath()).normalize();

           Resource resource = new UrlResource(path.toUri());
           String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename())
                    .body(resource);

        } catch(AmazonServiceException e) {
            throw new CustomException("The Specified key does not exist in the S3 bucket!");
        }
        catch (FileNotFoundException e) {
            throw new CustomException("The Specified key does not exist in the S3 bucket!");
        }
        catch(MethodArgumentTypeMismatchException | IllegalArgumentException e) {
            throw new CustomException("The Specified key does not exist in the S3 bucket! ----11");
        }
        catch (MalformedURLException e) {
            throw new CustomException("Something went wrong!");
        } catch (Exception e) {
            throw new CustomException("Something went wrong");
        }


   }
    @DeleteMapping("/flush/{predefined-type-name}")
    public void deleteImageInS3Bucket(@PathVariable("predefined-type-name") PredefineTypeName predefinedTypeName,
                                                 @RequestParam(name = "reference") String reference) {
        try {
            uploadService.deleteS3BucketImage(predefinedTypeName.toString(), reference);
            ResponseEntity.ok();
        } catch (AmazonServiceException e) {
            throw new CustomException("The Specified key does not exist in the S3 bucket!");
        }
        catch (Exception e) {
            throw new CustomException("Could not delete the specified file");
        }
    }

    @PostMapping("/uploads/{predefined-type-name}")
    public ResponseEntity<String> uploadFile(@PathVariable("predefined-type-name") PredefineTypeName predefinedTypeName,
                                             @RequestParam("file") MultipartFile file) {
        try {
            return new ResponseEntity<>(uploadService.uploadFileTos3bucket(predefinedTypeName.toString(), s3Utility.convertMultipartFileToFile(file)), HttpStatus.CREATED);
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