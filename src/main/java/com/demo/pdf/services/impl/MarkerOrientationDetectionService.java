package com.demo.pdf.services.impl;

import com.demo.pdf.services.OrientationDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Detects page orientation by locating a red corner marker in the rendered page image.
 *
 * The marker is a small red-colored shape (triangle/block) placed at one of the
 * four corners of the original document. Its position indicates the required
 * clockwise rotation to make the page content upright:
 *
 *   Marker at top-left     -> rotate = 0
 *   Marker at bottom-left  -> rotate = 90
 *   Marker at top-right    -> rotate = 180
 *   Marker at bottom-right -> rotate = 270
 */
@Primary
@Service
public class MarkerOrientationDetectionService implements OrientationDetectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerOrientationDetectionService.class);

    /** Fraction of image width/height to scan at each corner. */
    private static final double CORNER_SCAN_RATIO = 0.05;

    /** Minimum corner scan region size in pixels. */
    private static final int MIN_CORNER_SIZE = 20;

    /**
     * Minimum red-pixel count in a corner region to consider it as containing a marker.
     * The marker must have at least this many red pixels.
     */
    private static final int MIN_RED_PIXEL_COUNT = 5;

    /**
     * For grayscale fallback: scan a very small region at the extreme corner tip.
     * This avoids table borders/text which have margins from the page edge.
     */
    private static final double GRAYSCALE_CORNER_RATIO = 0.015;
    private static final int GRAYSCALE_DARK_THRESHOLD = 160;
    private static final double GRAYSCALE_MIN_DARK_RATIO = 0.10;

    @Override
    public List<Integer> detectAngles(List<String> pageImagePaths) {
        if (pageImagePaths == null || pageImagePaths.isEmpty()) {
            return List.of();
        }

        return IntStream.range(0, pageImagePaths.size())
                .mapToObj(i -> detectSinglePage(pageImagePaths.get(i), i + 1))
                .collect(Collectors.toList());
    }

    private int detectSinglePage(String imagePath, int pageNo) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                LOGGER.warn("Cannot read image for page {} ({}), defaulting to 0", pageNo, imagePath);
                return 0;
            }
            int angle = detectMarkerCorner(image);
            LOGGER.info("Page {}: marker detected -> rotate = {}", pageNo, angle);
            return angle;
        } catch (IOException ex) {
            LOGGER.warn("Marker detection failed for page {} ({}), defaulting to 0: {}",
                    pageNo, imagePath, ex.getMessage());
            return 0;
        }
    }

    int detectMarkerCorner(BufferedImage image) {
        boolean isGrayscale = image.getType() == BufferedImage.TYPE_BYTE_GRAY;

        if (isGrayscale) {
            return detectByDarkPixels(image);
        }
        return detectByRedColor(image);
    }

    /**
     * Detect the marker by looking for RED pixels (high R, low G, low B).
     * This is robust against black table borders, text, and other content.
     */
    private int detectByRedColor(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int cw = Math.max(MIN_CORNER_SIZE, (int) (w * CORNER_SCAN_RATIO));
        int ch = Math.max(MIN_CORNER_SIZE, (int) (h * CORNER_SCAN_RATIO));

        int topLeft = countRedPixels(image, 0, 0, cw, ch);
        int topRight = countRedPixels(image, w - cw, 0, cw, ch);
        int bottomLeft = countRedPixels(image, 0, h - ch, cw, ch);
        int bottomRight = countRedPixels(image, w - cw, h - ch, cw, ch);

        LOGGER.debug("Corner red-pixel counts: TL={}, TR={}, BL={}, BR={}",
                topLeft, topRight, bottomLeft, bottomRight);

        int max = Math.max(Math.max(topLeft, topRight), Math.max(bottomLeft, bottomRight));

        if (max < MIN_RED_PIXEL_COUNT) {
            LOGGER.warn("No red marker detected (max red count={} < threshold={}), defaulting to 0",
                    max, MIN_RED_PIXEL_COUNT);
            return 0;
        }

        if (max == topLeft) return 0;
        if (max == bottomLeft) return 90;
        if (max == topRight) return 180;
        return 270;
    }

    /**
     * Fallback for grayscale images: scan a very small region at the extreme
     * corner tip where only the marker should exist (not table borders/text).
     */
    private int detectByDarkPixels(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int cw = Math.max(10, (int) (w * GRAYSCALE_CORNER_RATIO));
        int ch = Math.max(10, (int) (h * GRAYSCALE_CORNER_RATIO));

        double topLeft = darkPixelRatio(image, 0, 0, cw, ch);
        double topRight = darkPixelRatio(image, w - cw, 0, cw, ch);
        double bottomLeft = darkPixelRatio(image, 0, h - ch, cw, ch);
        double bottomRight = darkPixelRatio(image, w - cw, h - ch, cw, ch);

        LOGGER.debug("Grayscale corner dark-pixel ratios: TL={}, TR={}, BL={}, BR={}",
                String.format("%.3f", topLeft),
                String.format("%.3f", topRight),
                String.format("%.3f", bottomLeft),
                String.format("%.3f", bottomRight));

        double max = Math.max(Math.max(topLeft, topRight), Math.max(bottomLeft, bottomRight));

        if (max < GRAYSCALE_MIN_DARK_RATIO) {
            LOGGER.warn("No corner marker detected in grayscale (max ratio={} < threshold={}), defaulting to 0",
                    String.format("%.3f", max), GRAYSCALE_MIN_DARK_RATIO);
            return 0;
        }

        if (max == topLeft) return 0;
        if (max == bottomLeft) return 90;
        if (max == topRight) return 180;
        return 270;
    }

    /**
     * Count pixels that are "red" in the given region.
     * A red pixel has: R > 100, (R - G) > 50, (R - B) > 50.
     * This catches bright red, dark red, and similar hues while ignoring
     * black (table borders), gray, and white pixels.
     */
    private int countRedPixels(BufferedImage image, int startX, int startY, int regionW, int regionH) {
        int count = 0;
        for (int y = startY; y < startY + regionH; y++) {
            for (int x = startX; x < startX + regionW; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r > 100 && (r - g) > 50 && (r - b) > 50) {
                    count++;
                }
            }
        }
        return count;
    }

    private double darkPixelRatio(BufferedImage image, int startX, int startY, int regionW, int regionH) {
        int totalPixels = regionW * regionH;
        if (totalPixels == 0) return 0.0;

        int darkCount = 0;
        for (int y = startY; y < startY + regionH; y++) {
            for (int x = startX; x < startX + regionW; x++) {
                int rgb = image.getRGB(x, y);
                int luminance = luminance(rgb);
                if (luminance < GRAYSCALE_DARK_THRESHOLD) {
                    darkCount++;
                }
            }
        }
        return (double) darkCount / totalPixels;
    }

    private static int luminance(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }
}
