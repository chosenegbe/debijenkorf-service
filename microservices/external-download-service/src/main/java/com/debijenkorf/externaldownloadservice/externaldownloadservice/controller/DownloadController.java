package com.debijenkorf.externaldownloadservice.externaldownloadservice.controller;

import com.debijenkorf.externaldownloadservice.externaldownloadservice.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external-downloads")
public class DownloadController {
    @Autowired
    private DownloadService downloadService;

    @RequestMapping("/{file-name}")
    public ResponseEntity<Object> downloadFile(@PathVariable("file-name") String fileName) {
        return new ResponseEntity<>(downloadService.download(fileName), HttpStatus.OK);
    }
}
