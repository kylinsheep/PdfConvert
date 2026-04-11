package com.demo.pdf.services.impl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestAngleDetectionService {

    private static final Pattern ORIENTATION_PATTERN =
            Pattern.compile("Orientation in degrees:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private final SimpleOrientationDetectionService.OcrCommandRunner ocrCommandRunner;
    private final String tesseractExecutable;
    private final String language;
    private final long timeoutMs;

    public BestAngleDetectionService(SimpleOrientationDetectionService.OcrCommandRunner ocrCommandRunner,
                                     String tesseractExecutable,
                                     String language,
                                     long timeoutMs) {
        this.ocrCommandRunner = ocrCommandRunner;
        this.tesseractExecutable = tesseractExecutable;
        this.language = language;
        this.timeoutMs = timeoutMs;
    }

    public int detectBestAngle(String imagePath) throws IOException {
        String osdOutput = ocrCommandRunner.runOsd(tesseractExecutable, language, timeoutMs, imagePath);
        Integer parsed = parseOrientationDegrees(osdOutput);
        return parsed == null ? 0 : parsed;
    }

    private Integer parseOrientationDegrees(String osdOutput) {
        Matcher matcher = ORIENTATION_PATTERN.matcher(osdOutput == null ? "" : osdOutput);
        if (!matcher.find()) {
            return null;
        }
        int degrees = Integer.parseInt(matcher.group(1));
        if (degrees == 0 || degrees == 90 || degrees == 180 || degrees == 270) {
            return degrees;
        }
        return null;
    }
}