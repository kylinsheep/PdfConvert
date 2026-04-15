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
    void shouldDetectMarkerAtTopRight_rotate0() {
        BufferedImage image = createImageWithMarkerAt(Corner.TOP_RIGHT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(0, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectMarkerAtTopLeft_rotate90() {
        BufferedImage image = createImageWithMarkerAt(Corner.TOP_LEFT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(90, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectMarkerAtBottomLeft_rotate180() {
        BufferedImage image = createImageWithMarkerAt(Corner.BOTTOM_LEFT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(180, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectMarkerAtBottomRight_rotate270() {
        BufferedImage image = createImageWithMarkerAt(Corner.BOTTOM_RIGHT);
        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(270, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDefaultToZeroWhenNoMarkerPresent() {
        BufferedImage image = new BufferedImage(500, 700, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 500, 700);
        g.dispose();

        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(0, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectSmallTriangleMarker() {
        // Simulate a very small triangle marker (6 pixels) — realistic size
        int w = 500, h = 700;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        // Small 6px triangle at bottom-left
        g.setColor(Color.BLACK);
        int[] xPoints = {0, 6, 0};
        int[] yPoints = {h - 6, h, h};
        g.fillPolygon(xPoints, yPoints, 3);
        g.dispose();

        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        Assertions.assertEquals(180, service.detectMarkerCorner(image));
    }

    @Test
    void shouldDetectFromImageFile() throws IOException {
        BufferedImage image = createImageWithMarkerAt(Corner.BOTTOM_LEFT);
        Path imagePath = tempDir.resolve("page-1.png");
        ImageIO.write(image, "PNG", imagePath.toFile());

        MarkerOrientationDetectionService service = new MarkerOrientationDetectionService();
        List<Integer> angles = service.detectAngles(List.of(imagePath.toString()));

        Assertions.assertEquals(List.of(180), angles);
    }

    private enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private BufferedImage createImageWithMarkerAt(Corner corner) {
        int w = 500, h = 700, markerSize = 8;
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
