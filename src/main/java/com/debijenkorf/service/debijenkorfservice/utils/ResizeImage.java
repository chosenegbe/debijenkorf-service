package com.debijenkorf.service.debijenkorfservice.utils;


import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ResizeImage {

    private static int WIDTH = 100;
    private static int HEIGHT = 100;
    private static String FORMAT_PNG = "png";
    private static String FORMAT_JPG = "jpg";

    public File resizedImage(File originalFile) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String pathSeparator = FileSystems.getDefault().getSeparator();
        Path ticketTempDirPath = Paths.get(tempDir + pathSeparator + "thumbnail");
        try {
            Path filePath = Files.createDirectories(ticketTempDirPath);
            BufferedImage originalImage = ImageIO.read(originalFile);
            BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, originalImage.getType());

            Graphics2D g2 = resized.createGraphics();
            g2.drawImage(originalImage, 0, 0, WIDTH, HEIGHT, null);
            g2.dispose();

            File resizedFile = new File(filePath + pathSeparator + originalFile.getName());
            ImageIO.write(resized, FORMAT_PNG, resizedFile);
            return resizedFile;

        } catch (IOException ex) {
            throw new RuntimeException();
        }

    }

}
