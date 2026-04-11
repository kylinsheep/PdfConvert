package com.demo.pdf.services.impl;

import com.demo.pdf.exceptions.BusinessException;
import com.demo.pdf.options.StorageProperties;
import com.demo.pdf.services.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final StorageProperties storageProperties;

    public LocalFileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public String saveUpload(String taskId, MultipartFile file) {
        try {
            Path uploadDir = Paths.get(storageProperties.getUploadDir());
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(taskId + ".pdf");
            file.transferTo(target);
            return target.toAbsolutePath().toString();
        } catch (IOException ex) {
            throw new BusinessException("Failed to save uploaded PDF: " + ex.getMessage());
        }
    }
}
