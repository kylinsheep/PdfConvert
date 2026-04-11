package com.demo.pdf.services.impl;

import com.demo.pdf.exceptions.BusinessException;
import com.demo.pdf.helpers.ImageRotateHelper;
import com.demo.pdf.options.StorageProperties;
import com.demo.pdf.services.PageNormalizeService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PageNormalizeServiceImpl implements PageNormalizeService {

    private final StorageProperties storageProperties;

    public PageNormalizeServiceImpl(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public List<String> normalize(String taskId, List<String> pageImagePaths, List<Integer> detectedAngles) {
        if (pageImagePaths.size() != detectedAngles.size()) {
            throw new BusinessException("Page count and angle count do not match.");
        }

        Path outputDir = Paths.get(storageProperties.getNormalizedDir(), taskId);
        try {
            Files.createDirectories(outputDir);
            List<String> outputs = new ArrayList<>();

            for (int i = 0; i < pageImagePaths.size(); i++) {
                Path source = Paths.get(pageImagePaths.get(i));
                BufferedImage image = ImageIO.read(source.toFile());
                if (image == null) {
                    throw new BusinessException("Cannot read image: " + source);
                }

                // 假设检测角度表示“当前页面顺时针偏转角度”，因此反向旋转纠正。
                int correctionAngle = (360 - detectedAngles.get(i)) % 360;
                BufferedImage normalized = ImageRotateHelper.rotateClockwise(image, correctionAngle);

                Path out = outputDir.resolve("page-" + (i + 1) + ".png");
                ImageIO.write(normalized, "PNG", out.toFile());
                outputs.add(out.toAbsolutePath().toString());
            }
            return outputs;
        } catch (IOException ex) {
            throw new BusinessException("Failed to normalize page images: " + ex.getMessage());
        }
    }
}
