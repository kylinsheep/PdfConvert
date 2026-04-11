package com.demo.pdf.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveUpload(String taskId, MultipartFile file);
}
