package com.debijenkorf.service.debijenkorfservice.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ImageUtility {
    public File convertMultipartToFile(MultipartFile file) {

        File convertedFile = new File(file.getOriginalFilename());
        try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(convertedFile))) {
            bos.write(file.getBytes());
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        return convertedFile;
    }

    public static boolean imageValidation(String imageName) {
        String regex = "([^\\s]+(\\.(?i)(jpe?g|png))$)";
        Pattern p = Pattern.compile(regex);
        if (imageName == null) {
            return false;
        }
        Matcher m = p.matcher(imageName);
        return m.matches();
    }

}
