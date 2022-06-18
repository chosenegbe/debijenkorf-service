package com.debijenkorf.service.debijenkorfservice.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class DownloadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadService.class);

    @Autowired
    private RestTemplate restTemplate;

    public File download(String fileName)  {

        String urlTemplate = "https://i.imgur.com/"+ fileName;


        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("downloaded", "test123");
        uriVariables.put("file", fileName);

        System.out.println("inside Resource download(String fileName)   urlTemplate" + urlTemplate);

        File receivedFile  = restTemplate.execute(urlTemplate, HttpMethod.GET, requestCallback(),
                clientHttpResponse -> {
                    String tempDir = System.getProperty("java.io.tmpdir");
                    String pathSeparator = FileSystems.getDefault().getSeparator();
                    Path ticketTempDirPath = Paths.get(tempDir + pathSeparator + "test123");
                    Path filePath = Files.createDirectories(ticketTempDirPath);
                    File ret = new File(filePath.toFile(), fileName);
                    BufferedOutputStream downloadedFileOutputStream = new BufferedOutputStream(new FileOutputStream(ret));
                    StreamUtils.copy(clientHttpResponse.getBody(), downloadedFileOutputStream);
                    downloadedFileOutputStream.close();
                    return ret;
                },
                uriVariables
        );

        return receivedFile;
    }
    private RequestCallback requestCallback() {
        return clientHttpRequest -> {
            clientHttpRequest.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        };
    }

}

