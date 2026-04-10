package com.demo.pdf.helpers;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class ImageRotateHelper {

    private ImageRotateHelper() {
    }

    public static BufferedImage rotateClockwise(BufferedImage source, int angle) {
        int normalized = ((angle % 360) + 360) % 360;
        if (normalized == 0) {
            return source;
        }

        double radians = Math.toRadians(normalized);
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        int targetWidth = (normalized == 90 || normalized == 270) ? srcHeight : srcWidth;
        int targetHeight = (normalized == 90 || normalized == 270) ? srcWidth : srcHeight;

        BufferedImage target = new BufferedImage(targetWidth, targetHeight, source.getType());
        Graphics2D g2d = target.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform transform = new AffineTransform();
        transform.translate(targetWidth / 2.0, targetHeight / 2.0);
        transform.rotate(radians);
        transform.translate(-srcWidth / 2.0, -srcHeight / 2.0);

        g2d.drawRenderedImage(source, transform);
        g2d.dispose();
        return target;
    }
}
