package com.demo.pdf.services.impl;

import com.demo.pdf.exceptions.BusinessException;
import com.demo.pdf.models.PageAngle;
import com.demo.pdf.models.TaskResult;
import com.demo.pdf.services.FileStorageService;
import com.demo.pdf.services.OrientationDetectionService;
import com.demo.pdf.services.PageNormalizeService;
import com.demo.pdf.services.PdfRenderService;
import com.demo.pdf.services.PdfTaskService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PdfTaskServiceImpl implements PdfTaskService {

    private final FileStorageService fileStorageService;
    private final PdfRenderService pdfRenderService;
    private final OrientationDetectionService orientationDetectionService;
    private final PageNormalizeService pageNormalizeService;

    private final Map<String, TaskResult> taskStore = new ConcurrentHashMap<>();

    public PdfTaskServiceImpl(FileStorageService fileStorageService,
                              PdfRenderService pdfRenderService,
                              OrientationDetectionService orientationDetectionService,
                              PageNormalizeService pageNormalizeService) {
        this.fileStorageService = fileStorageService;
        this.pdfRenderService = pdfRenderService;
        this.orientationDetectionService = orientationDetectionService;
        this.pageNormalizeService = pageNormalizeService;
    }

    @Override
    public TaskResult process(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Uploaded file is empty.");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException("Only PDF file is supported.");
        }

        String taskId = UUID.randomUUID().toString().replace("-", "");
        String pdfPath = fileStorageService.saveUpload(taskId, file);

        List<String> pageImagePaths = pdfRenderService.renderToImages(taskId, pdfPath);
        List<Integer> angles = orientationDetectionService.detectAngles(pageImagePaths);
        List<String> normalizedPaths = pageNormalizeService.normalize(taskId, pageImagePaths, angles);

        TaskResult result = new TaskResult(taskId);
        List<PageAngle> pageAngles = IntStream.range(0, angles.size())
                .mapToObj(i -> new PageAngle(i + 1, angles.get(i)))
                .collect(Collectors.toList());
        result.setAngles(pageAngles);
        result.setNormalizedImagePaths(normalizedPaths);

        taskStore.put(taskId, result);
        return result;
    }

    @Override
    public TaskResult getResult(String taskId) {
        TaskResult result = taskStore.get(taskId);
        if (result == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        return result;
    }
}
