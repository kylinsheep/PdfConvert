package com.demo.pdf.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OcrOrientationParser {

    private static final Pattern ORIENTATION_PATTERN = Pattern.compile("(?i)orientation\\s+in\\s+degrees\\s*:\\s*(\\d+)");

    private OcrOrientationParser() {
    }

    public static int parseAngle(String rawOutput) {
        if (rawOutput == null || rawOutput.trim().isEmpty()) {
            return 0;
        }

        Matcher matcher = ORIENTATION_PATTERN.matcher(rawOutput);
        if (!matcher.find()) {
            return 0;
        }

        int parsed = Integer.parseInt(matcher.group(1));
        int normalized = ((parsed % 360) + 360) % 360;
        if (normalized % 90 != 0) {
            return 0;
        }
        return normalized;
    }
}
