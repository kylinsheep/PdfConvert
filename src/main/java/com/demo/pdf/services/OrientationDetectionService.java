package com.demo.pdf.services;

import java.util.List;

public interface OrientationDetectionService {
    List<Integer> detectAngles(List<String> pageImagePaths);
}
