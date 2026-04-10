package com.demo.pdf.models;

public class UploadResponse {

    private String taskId;
    private String filePath;

    public UploadResponse(String taskId, String filePath) {
        this.taskId = taskId;
        this.filePath = filePath;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getFilePath() {
        return filePath;
    }
}
