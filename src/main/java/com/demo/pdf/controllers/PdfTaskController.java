package com.demo.pdf.controllers;

import com.demo.pdf.common.Result;
import com.demo.pdf.models.PageAngle;
import com.demo.pdf.models.TaskResult;
import com.demo.pdf.models.UploadResponse;
import com.demo.pdf.services.PdfTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PdfTaskController {

    private final PdfTaskService pdfTaskService;

    public PdfTaskController(PdfTaskService pdfTaskService) {
        this.pdfTaskService = pdfTaskService;
    }

    @PostMapping("/upload")
    public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        TaskResult result = pdfTaskService.process(file);
        UploadResponse response = new UploadResponse(
                result.getTaskId(),
                "./data/uploads/" + result.getTaskId() + ".pdf",
                result.getAngles()
        );
        return Result.ok(response);
    }

    @GetMapping("/{taskId}/angles")
    public Result<List<PageAngle>> getAngles(@PathVariable String taskId) {
        return Result.ok(pdfTaskService.getResult(taskId).getAngles());
    }

    @GetMapping("/{taskId}/normalized")
    public Result<Map<String, Object>> getNormalized(@PathVariable String taskId) {
        TaskResult result = pdfTaskService.getResult(taskId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", result.getTaskId());
        payload.put("images", result.getNormalizedImagePaths());
        return Result.ok(payload);
    }
}
