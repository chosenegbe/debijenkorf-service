package com.debijenkorf.service.debijenkorfservice.utils;


import com.debijenkorf.service.debijenkorfservice.exception.CustomException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static java.lang.String.valueOf;

@Component
public class ResizeImage {
    private static final Logger LOG = LoggerFactory.getLogger(ResizeImage.class);
    private enum Type {
        png, jpg
    };
    private static int WIDTH = 300;
    private static int HEIGHT = 300;

    /* Scale type not switch and fill validity not yet implemented */

    public File resizedImage(File originalFile) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String pathSeparator = FileSystems.getDefault().getSeparator();
        Path ticketTempDirPath = Paths.get(tempDir + pathSeparator + "thumbnail");
        try {
            Path filePath = Files.createDirectories(ticketTempDirPath);
            BufferedImage readImage = ImageIO.read(originalFile);

            if (!imageNeedsResizing(originalFile, readImage)) return originalFile;

            BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, readImage.getType());

            Graphics2D g2 = resized.createGraphics();
            g2.drawImage(readImage, 0, 0, WIDTH, HEIGHT, null);
            g2.dispose();

            File resizedFile = new File(filePath + pathSeparator + originalFile.getName());
            ImageIO.write(resized, imageFormat(originalFile.getName()), resizedFile);
            return resizedFile;

        } catch (IOException ex) {
            LOG.error("Error occurred in image resizing");
            throw new CustomException("Error occurred in image resizing");
        }

    }

    private boolean imageNeedsResizing(File originalFile, BufferedImage image) {
        if (image.getHeight() <= HEIGHT || image.getWidth() <= WIDTH) {
            LOG.info("No resizing needed for " + originalFile.getName() + ". The image size is already smaller than the specified resize parameters of " + WIDTH + " * " + HEIGHT);
            return false;
        }
        return true;
    }
    private String imageFormat(String format) {
        if (format.endsWith("png"))
            return valueOf(Type.png);
        else
            return valueOf(Type.jpg);
    }
}
