package com.demo.pdf.models;

import java.util.ArrayList;
import java.util.List;

public class TaskResult {

    private String taskId;
    private List<PageAngle> angles = new ArrayList<>();
    private List<String> normalizedImagePaths = new ArrayList<>();

    public TaskResult(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public List<PageAngle> getAngles() {
        return angles;
    }

    public void setAngles(List<PageAngle> angles) {
        this.angles = angles;
    }

    public List<String> getNormalizedImagePaths() {
        return normalizedImagePaths;
    }

    public void setNormalizedImagePaths(List<String> normalizedImagePaths) {
        this.normalizedImagePaths = normalizedImagePaths;
    }
}
