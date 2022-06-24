package com.debijenkorf.externaldownloadservice.externaldownloadservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class DownloadService {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadService.class);
    @Autowired
    private RestTemplate restTemplate;

    @Value("${downloads.server.url}")
    private String downloadServerUrl;

    public File download(String fileName)  {

        String urlTemplate = downloadServerUrl + "/"+ fileName;

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("downloaded", "test123");
        uriVariables.put("file", fileName);

        LOG.info("Downloading file " + fileName + " from " + downloadServerUrl);

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
        LOG.info("Download of file " + fileName + " completed. File downloaded from " + downloadServerUrl);

        return receivedFile;
    }

    private RequestCallback requestCallback() {
        return clientHttpRequest -> {
            clientHttpRequest.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        };
    }

}
