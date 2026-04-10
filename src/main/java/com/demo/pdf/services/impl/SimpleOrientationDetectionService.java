package com.demo.pdf.services.impl;

import com.demo.pdf.services.OrientationDetectionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleOrientationDetectionService implements OrientationDetectionService {

    @Override
    public List<Integer> detectAngles(List<String> pageImagePaths) {
        // 最小可运行版本：先统一返回 0，确保流程跑通。
        // 下一步可替换为 Tesseract OSD 或自定义图像方向模型。
        List<Integer> angles = new ArrayList<>();
        for (int i = 0; i < pageImagePaths.size(); i++) {
            angles.add(0);
        }
        return angles;
    }
}
