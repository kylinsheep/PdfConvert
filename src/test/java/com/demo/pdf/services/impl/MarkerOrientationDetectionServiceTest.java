package com.demo.pdf.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class MarkerOrientationDetectionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnEmptyWhenInputIsNullOrEmpty() {
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(List.of(), service.detectAngles(null));
        Assertions.assertEquals(List.of(), service.detectAngles(List.of()));
    }

    @Test
    void shouldDetectMarkerAtTopLeft() {
        BufferedImage image = createImageWithMarkerAt(Corner.TOP_LEFT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(0, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectMarkerAtBottomLeft() {
        BufferedImage image = createImageWithMarkerAt(Corner.BOTTOM_LEFT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(90, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectMarkerAtTopRight() {
        BufferedImage image = createImageWithMarkerAt(Corner.TOP_RIGHT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(180, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectMarkerAtBottomRight() {
        BufferedImage image = createImageWithMarkerAt(Corner.BOTTOM_RIGHT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(270, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDefaultToZeroWhenNoMarkerPresent() {
        // All-white image, no marker
        BufferedImage image = new BufferedImage(500, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 500, 700);
        g.dispose();

        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(0, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectFromImageFile() throws IOException {
        BufferedImage image = createImageWithMarkerAt(Corner.BOTTOM_LEFT);
        Path imagePath = tempDir.resolve("page-1.png");
        ImageIO.write(image, "PNG", imagePath.toFile());

        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        List<Integer> angles = service.detectAngles(List.of(imagePath.toString()));

        Assertions.assertEquals(List.of(90), angles);
    }

    private enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private BufferedImage createImageWithMarkerAt(Corner corner) {
        int w = 500;
        int h = 700;
        int markerSize = 20;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        // Draw dark marker at the specified corner
        g.setColor(Color.BLACK);
        switch (corner) {
            case TOP_LEFT:
                g.fillRect(0, 0, markerSize, markerSize);
                break;
            case TOP_RIGHT:
                g.fillRect(w - markerSize, 0, markerSize, markerSize);
                break;
            case BOTTOM_LEFT:
                g.fillRect(0, h - markerSize, markerSize, markerSize);
                break;
            case BOTTOM_RIGHT:
                g.fillRect(w - markerSize, h - markerSize, markerSize, markerSize);
                break;
        }

        g.dispose();
        return image;
    }
}
