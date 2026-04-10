package com.demo.pdf.services;

import com.demo.pdf.models.TaskResult;
import org.springframework.web.multipart.MultipartFile;

public interface PdfTaskService {
    TaskResult process(MultipartFile file);
    TaskResult getResult(String taskId);
}
