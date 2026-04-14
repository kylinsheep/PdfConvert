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
 * Detects page orientation by locating a corner marker in the rendered page image.
 *
 * The marker is a small solid-colored block placed at one of the four corners
 * of the original document. Its position indicates the required clockwise
 * rotation to make the page content upright:
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
    private static final double CORNER_SCAN_RATIO = 0.04;

    /** Minimum corner scan region size in pixels. */
    private static final int MIN_CORNER_SIZE = 15;

    /** Luminance threshold: pixels with luminance below this are considered "dark". */
    private static final int DARK_LUMINANCE_THRESHOLD = 180;

    /** Minimum dark-pixel ratio in a corner region to consider it as containing a marker. */
    private static final double MIN_DARK_RATIO = 0.08;

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
        int w = image.getWidth();
        int h = image.getHeight();
        int cw = Math.max(MIN_CORNER_SIZE, (int) (w * CORNER_SCAN_RATIO));
        int ch = Math.max(MIN_CORNER_SIZE, (int) (h * CORNER_SCAN_RATIO));

        double topLeft = darkPixelRatio(image, 0, 0, cw, ch);
        double topRight = darkPixelRatio(image, w - cw, 0, cw, ch);
        double bottomLeft = darkPixelRatio(image, 0, h - ch, cw, ch);
        double bottomRight = darkPixelRatio(image, w - cw, h - ch, cw, ch);

        LOGGER.debug("Corner dark-pixel ratios: TL={}, TR={}, BL={}, BR={}",
                String.format("%.3f", topLeft),
                String.format("%.3f", topRight),
                String.format("%.3f", bottomLeft),
                String.format("%.3f", bottomRight));

        // Find the corner with the highest dark-pixel ratio
        double max = Math.max(Math.max(topLeft, topRight), Math.max(bottomLeft, bottomRight));

        if (max < MIN_DARK_RATIO) {
            LOGGER.warn("No corner marker detected (max dark ratio={} < threshold={}), defaulting to 0",
                    String.format("%.3f", max), MIN_DARK_RATIO);
            return 0;
        }

        if (max == topLeft) return 0;
        if (max == bottomLeft) return 90;
        if (max == topRight) return 180;
        // bottomRight
        return 270;
    }

    private double darkPixelRatio(BufferedImage image, int startX, int startY, int regionW, int regionH) {
        int totalPixels = regionW * regionH;
        if (totalPixels == 0) return 0.0;

        int darkCount = 0;
        for (int y = startY; y < startY + regionH; y++) {
            for (int x = startX; x < startX + regionW; x++) {
                int rgb = image.getRGB(x, y);
                int luminance = luminance(rgb);
                if (luminance < DARK_LUMINANCE_THRESHOLD) {
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
