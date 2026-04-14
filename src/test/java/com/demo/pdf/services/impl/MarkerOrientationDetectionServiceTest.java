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

    // --- RGB (red marker) tests ---

    @Test
    void shouldDetectRedMarkerAtTopLeft() {
        BufferedImage image = createRgbImageWithRedMarkerAt(Corner.TOP_LEFT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(0, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectRedMarkerAtBottomLeft() {
        BufferedImage image = createRgbImageWithRedMarkerAt(Corner.BOTTOM_LEFT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(90, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectRedMarkerAtTopRight() {
        BufferedImage image = createRgbImageWithRedMarkerAt(Corner.TOP_RIGHT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(180, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectRedMarkerAtBottomRight() {
        BufferedImage image = createRgbImageWithRedMarkerAt(Corner.BOTTOM_RIGHT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(270, service.detectMarkerCorner(image));
    }

    @Test
    void shouldNotConfuseBlackBordersWithRedMarker() {
        // Image with black table borders near all corners, red marker only at bottom-left
        int w = 500, h = 700;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        // Draw black table borders near edges (simulating a table)
        g.setColor(Color.BLACK);
        g.drawRect(20, 20, w - 40, h - 40);  // outer border
        g.drawLine(20, h / 2, w - 20, h / 2); // horizontal line

        // Red marker at bottom-left corner
        g.setColor(Color.RED);
        g.fillRect(0, h - 15, 15, 15);

        g.dispose();

        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(90, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDefaultToZeroWhenNoMarkerPresent() {
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
        BufferedImage image = createRgbImageWithRedMarkerAt(Corner.BOTTOM_LEFT);
        Path imagePath = tempDir.resolve("page-1.png");
        ImageIO.write(image, "PNG", imagePath.toFile());

        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        List<Integer> angles = service.detectAngles(List.of(imagePath.toString()));

        Assertions.assertEquals(List.of(90), angles);
    }

    // --- Grayscale fallback tests ---

    @Test
    void shouldDetectDarkMarkerInGrayscaleAtBottomRight() {
        BufferedImage image = createGrayscaleImageWithMarkerAt(Corner.BOTTOM_RIGHT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(270, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectDarkMarkerInGrayscaleAtTopLeft() {
        BufferedImage image = createGrayscaleImageWithMarkerAt(Corner.TOP_LEFT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(0, service.detectMarkerCorner(image));
    }

    // --- Helpers ---

    private enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private BufferedImage createRgbImageWithRedMarkerAt(Corner corner) {
        int w = 500, h = 700, markerSize = 15;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        g.setColor(Color.RED);
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

    private BufferedImage createGrayscaleImageWithMarkerAt(Corner corner) {
        int w = 500, h = 700, markerSize = 10;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

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
