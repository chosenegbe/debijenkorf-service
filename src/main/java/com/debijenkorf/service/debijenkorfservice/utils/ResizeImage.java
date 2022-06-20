package com.debijenkorf.service.debijenkorfservice.utils;


import com.debijenkorf.service.debijenkorfservice.exception.CustomException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.valueOf;

@Component
public class ResizeImage {

    @Autowired
    private ImageUtility imageUtility;
    private static final Logger LOG = LoggerFactory.getLogger(ResizeImage.class);
    private enum Type {
        png, jpg
    };

    private enum ScaleType {
        Crop,
        Fill,
        Skew;
        private static ResizeImage.ScaleType getRandomScaleType() { //just for simulation
            Random random = new Random();
            return values()[random.nextInt(values().length)];
        }
    };

    private static int WIDTH = 200;
    private static int HEIGHT = 200;

    public File resizedImage(File originalFile) {
        if (!imageUtility.imageValidation(originalFile.getName())) throw new CustomException("The specified format is not supported, image format should be png or jp(e)g");
        String tempDir = System.getProperty("java.io.tmpdir");
        String pathSeparator = FileSystems.getDefault().getSeparator();
        Path ticketTempDirPath = Paths.get(tempDir + pathSeparator + "thumbnail");
        try {
            Path filePath = Files.createDirectories(ticketTempDirPath);
            BufferedImage readImage = ImageIO.read(originalFile);
            if (!imageNeedsResizing(originalFile, readImage)) return originalFile;

            ScaleType scaleType = ScaleType.getRandomScaleType();
            boolean isFilled = scaleType == scaleType.Skew;
            File resizedFile = getResizedImage(originalFile, pathSeparator, filePath, readImage);

            LOG.info("Image " + originalFile.getName() + " resized. New dimension is " + WIDTH + " * " + HEIGHT + ". Scale type set to " + scaleType + (isFilled? ". The filled property was set": ""));
            return resizedFile;

        } catch (IOException ex) {
            LOG.error("Error occurred in image resizing");
            throw new CustomException("Error occurred in image resizing");
        }
    }

    private File getResizedImage(File originalFile, String pathSeparator, Path filePath, BufferedImage readImage) throws IOException {
        BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, readImage.getType());
        Graphics2D g2 = resized.createGraphics();
        g2.drawImage(readImage, 0, 0, WIDTH, HEIGHT, null);
        g2.dispose();
        File resizedFile = new File(filePath + pathSeparator + originalFile.getName());
        ImageIO.write(resized, newImageFormat(originalFile.getName()), resizedFile);
        return resizedFile;
    }

    private boolean imageNeedsResizing(File originalFile, BufferedImage image) {
        if (image.getHeight() <= HEIGHT || image.getWidth() <= WIDTH) {
            LOG.info("No resizing needed for " + originalFile.getName() + ". The image dimension is already smaller than the specified resize dimension of " + WIDTH + " * " + HEIGHT);
            return false;
        }
        return true;
    }
    private String newImageFormat(String format) {
        if (format.endsWith(".png"))
            return valueOf(Type.png);
        else
            return valueOf(Type.jpg);
    }


}
