package com.demo.pdf.models;

import java.util.List;

public class UploadResponse {

    private String taskId;
    private String filePath;
    private List<PageAngle> angles;

    public UploadResponse(String taskId, String filePath, List<PageAngle> angles) {
        this.taskId = taskId;
        this.filePath = filePath;
        this.angles = angles;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<PageAngle> getAngles() {
        return angles;
    }
}
