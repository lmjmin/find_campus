package com.example.find_campus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.find_campus.dto.ItemImageDto;

@Service
public class ImageStorageService {

    private final Path uploadRoot;

    public ImageStorageService(@Value("${findcampus.upload-root:uploads}") String uploadRoot) {
        this.uploadRoot = Path.of(uploadRoot).toAbsolutePath().normalize();
    }

    public List<ItemImageDto> store(String itemType, Long itemId, List<MultipartFile> images) {
        List<ItemImageDto> savedImages = new ArrayList<>();
        if (images == null || images.isEmpty()) {
            return savedImages;
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path targetDir = uploadRoot.resolve(dateDir);

        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload directory.", e);
        }

        int sortOrder = 1;
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }

            String originalName = StringUtils.cleanPath(image.getOriginalFilename() == null ? "image" : image.getOriginalFilename());
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }
            String savedName = UUID.randomUUID() + extension;
            Path savedPath = targetDir.resolve(savedName);

            try {
                image.transferTo(savedPath);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot save uploaded image.", e);
            }

            ItemImageDto dto = new ItemImageDto();
            dto.setItemType(itemType);
            dto.setItemId(itemId);
            dto.setOriginalName(originalName);
            dto.setSavedName(dateDir + "/" + savedName);
            dto.setImageUrl("/uploads/" + dateDir + "/" + savedName);
            dto.setSortOrder(sortOrder++);
            savedImages.add(dto);
        }

        return savedImages;
    }
}
