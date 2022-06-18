package com.debijenkorf.service.debijenkorfservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDetailsResponse {
    private String imageName;
    private String bucketName;
    private String imageSize;
    private String imageType;
    private String dimension;
    private String folderPath;
    private String predefinedImageType;
}
