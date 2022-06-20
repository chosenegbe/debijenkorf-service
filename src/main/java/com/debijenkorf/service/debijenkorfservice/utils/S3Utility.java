package com.debijenkorf.service.debijenkorfservice.utils;

import org.springframework.stereotype.Component;
import java.nio.file.FileSystems;

@Component
public class S3Utility {
    private static int FOLDER_LENGTH = 4;

    public String s3KeyName(String fileName) {

        String path = "";
        String pathSeparator = FileSystems.getDefault().getSeparator();
        String originalFinalName = fileName;
        int index = 0;

        if (fileName.contains(".")) {
            index = originalFinalName.lastIndexOf('.');
            fileName = fileName.substring(0, index);
        }
        if (fileName.contains("/")) {
            fileName = fileName.replace('/', '_');
        }
        if (fileName.length() >= 4 && fileName.length() <= 8) {
            path = fileName.substring(0, FOLDER_LENGTH) + pathSeparator;
        }
        if (fileName.length() > 8) {
            path = fileName.substring(0, FOLDER_LENGTH) + pathSeparator + fileName.substring(FOLDER_LENGTH, FOLDER_LENGTH * 2) + pathSeparator;
        }

        if (index > 0) {
            String extension = originalFinalName.substring(index + 1);
            fileName = fileName + "." + extension;

        }
        return path + fileName;

    }
}
