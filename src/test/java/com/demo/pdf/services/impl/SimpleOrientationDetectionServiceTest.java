package com.demo.pdf.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class SimpleOrientationDetectionServiceTest {

    private static final String EXECUTABLE = "tesseract";
    private static final String LANGUAGE = "osd";
    private static final long TIMEOUT_MS = 1000;

    @Test
    void shouldReturnEmptyWhenInputIsNullOrEmpty() {
        SimpleOrientationDetectionService service = new SimpleOrientationDetectionService(true, EXECUTABLE, LANGUAGE, TIMEOUT_MS,
                (executable, language, timeout, image) -> "");

        Assertions.assertEquals(List.of(), service.detectAngles(null));
        Assertions.assertEquals(List.of(), service.detectAngles(List.of()));
    }

    @Test
    void shouldReturnAllZeroWhenOcrDisabled() {
        AtomicInteger calls = new AtomicInteger();
        SimpleOrientationDetectionService service = new SimpleOrientationDetectionService(false, EXECUTABLE, LANGUAGE, TIMEOUT_MS,
                (executable, language, timeout, image) -> {
            calls.incrementAndGet();
            return "Orientation in degrees: 90";
        });

        List<Integer> angles = service.detectAngles(List.of("a.png", "b.png", "c.png"));

        Assertions.assertEquals(List.of(0, 0, 0), angles);
        Assertions.assertEquals(0, calls.get());
    }

    @Test
    void shouldParseAnglesFromOsdOutput() {
        SimpleOrientationDetectionService service = new SimpleOrientationDetectionService(true, EXECUTABLE, LANGUAGE, TIMEOUT_MS,
                (executable, language, timeout, image) -> {
            if ("p1.png".equals(image)) {
                return "Orientation in degrees: 270";
            }
            return "Orientation in degrees: 90";
        });

        List<Integer> angles = service.detectAngles(List.of("p1.png", "p2.png"));

        Assertions.assertEquals(List.of(270, 90), angles);
    }

    @Test
    void shouldFallbackToZeroWhenSinglePageFails() {
        SimpleOrientationDetectionService service = new SimpleOrientationDetectionService(true, EXECUTABLE, LANGUAGE, TIMEOUT_MS,
                (executable, language, timeout, image) -> {
            if ("bad.png".equals(image)) {
                throw new IOException("boom");
            }
            return "Orientation in degrees: 180";
        });

        List<Integer> angles = service.detectAngles(List.of("ok.png", "bad.png"));

        Assertions.assertEquals(List.of(180, 0), angles);
    }
}


