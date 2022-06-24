package com.debijenkorf.service.debijenkorfservice.utils;


import com.debijenkorf.service.debijenkorfservice.exception.CustomException;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
            Random rand = new Random();
            int quality = rand.nextInt(100);
            System.out.println("We are here 1");
            if (!imageNeedsResizing(originalFile, readImage)) return originalFile;

            ScaleType scaleType = ScaleType.getRandomScaleType();
            boolean isFilled = scaleType == scaleType.Skew;
            System.out.println("We are here 2");
            File resizedFile = getResizedImageThumbnailer(readImage, scaleType, quality, originalFile.getName(), pathSeparator, filePath);
            //File resizedFile = getResizedImage(originalFile, pathSeparator, filePath, readImage);

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
    private File getResizedImageThumbnailer(BufferedImage originalImage, ScaleType scaleType, int quality, String imageName, String pathSeparator, Path filePath) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.Builder<BufferedImage> bufferedImageBuilder = Thumbnails.of(originalImage)
                .size(WIDTH, HEIGHT)
                .outputFormat(newImageFormat(imageName))
                .outputQuality(quality * 0.01)
                .keepAspectRatio(true);
        switch(scaleType) {
            case Crop : bufferedImageBuilder.crop(Positions.CENTER);
            case Fill: //NOT Implemented;
            case Skew: //NOT IMPLemented
        }
        bufferedImageBuilder.toOutputStream(outputStream);
        byte[] data = outputStream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        BufferedImage bImage = ImageIO.read(bis);

        File resizedFile = new File(filePath + pathSeparator + imageName);
        ImageIO.write(bImage, newImageFormat(imageName), resizedFile );

        return resizedFile;
    }

}
