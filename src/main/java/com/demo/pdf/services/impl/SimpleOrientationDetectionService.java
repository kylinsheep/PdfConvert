package com.demo.pdf.services.impl;

import com.demo.pdf.helpers.OcrOrientationParser;
import com.demo.pdf.services.OrientationDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SimpleOrientationDetectionService implements OrientationDetectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleOrientationDetectionService.class);

    private final boolean ocrEnabled;
    private final String ocrExecutable;
    private final String ocrLanguage;
    private final long ocrTimeoutMs;
    private final OcrCommandRunner ocrCommandRunner;

    public SimpleOrientationDetectionService(
            @Value("${app.ocr.enabled:true}") boolean ocrEnabled,
            @Value("${app.ocr.executable:tesseract}") String ocrExecutable,
            @Value("${app.ocr.language:osd}") String ocrLanguage,
            @Value("${app.ocr.timeout-ms:8000}") long ocrTimeoutMs
    ) {
        this(ocrEnabled, ocrExecutable, ocrLanguage, ocrTimeoutMs, new TesseractOcrCommandRunner());
    }

    SimpleOrientationDetectionService(boolean ocrEnabled,
                                      String ocrExecutable,
                                      String ocrLanguage,
                                      long ocrTimeoutMs,
                                      OcrCommandRunner ocrCommandRunner) {
        this.ocrEnabled = ocrEnabled;
        this.ocrExecutable = ocrExecutable;
        this.ocrLanguage = ocrLanguage;
        this.ocrTimeoutMs = ocrTimeoutMs;
        this.ocrCommandRunner = ocrCommandRunner;
    }

    @Override
    public List<Integer> detectAngles(List<String> pageImagePaths) {
        if (pageImagePaths == null || pageImagePaths.isEmpty()) {
            return List.of();
        }

        if (!ocrEnabled) {
            return IntStream.range(0, pageImagePaths.size()).mapToObj(i -> 0).collect(Collectors.toList());
        }

        int failedPages = 0;
        List<Integer> angles = IntStream.range(0, pageImagePaths.size())
                .mapToObj(i -> detectSinglePage(pageImagePaths.get(i), i + 1))
                .collect(Collectors.toList());

        for (Integer angle : angles) {
            if (angle == null) {
                failedPages++;
            }
        }

        if (failedPages == pageImagePaths.size()) {
            LOGGER.warn("OSD detection failed for all pages, fallback to zero angles.");
        }

        return angles.stream().map(angle -> angle == null ? 0 : angle).collect(Collectors.toList());
    }

    private Integer detectSinglePage(String imagePath, int pageNo) {
        try {
            String output = ocrCommandRunner.runOsd(ocrExecutable, ocrLanguage, ocrTimeoutMs, imagePath);
            return OcrOrientationParser.parseAngle(output);
        } catch (Exception ex) {
            LOGGER.warn("OSD detection failed for page {} ({}), fallback to 0: {}", pageNo, imagePath, ex.getMessage());
            return null;
        }
    }

    interface OcrCommandRunner {
        String runOsd(String executable, String language, long timeoutMs, String imagePath) throws IOException;
    }

    static class TesseractOcrCommandRunner implements OcrCommandRunner {
        @Override
        public String runOsd(String executable, String language, long timeoutMs, String imagePath) throws IOException {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    executable,
                    imagePath,
                    "stdout",
                    "--psm",
                    "0",
                    "-l",
                    language
            );
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            try {
                boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    throw new IOException("Tesseract OSD timed out.");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting Tesseract OSD process.", ex);
            }

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }

            if (process.exitValue() != 0) {
                throw new IOException("Tesseract OSD exited with code " + process.exitValue() + ".");
            }
            return output;
        }
    }
}
