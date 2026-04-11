package com.demo.pdf.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OcrOrientationParserTest {

    @Test
    void shouldParseOsdOutput() {
        String osd = "Orientation in degrees: 270\nRotate: 90\n";
        int angle = OcrOrientationParser.parseAngle(osd);
        Assertions.assertEquals(270, angle);
    }

    @Test
    void shouldFallbackToZeroWhenInvalid() {
        int angle = OcrOrientationParser.parseAngle("No orientation data");
        Assertions.assertEquals(0, angle);
    }
}
