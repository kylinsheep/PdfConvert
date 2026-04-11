package com.demo.pdf.services;

import java.util.List;

public interface PageNormalizeService {
    List<String> normalize(String taskId, List<String> pageImagePaths, List<Integer> detectedAngles);
}
