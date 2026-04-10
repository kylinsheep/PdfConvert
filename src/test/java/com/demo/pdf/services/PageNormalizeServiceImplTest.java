package com.demo.pdf.services;

import com.demo.pdf.options.StorageProperties;
import com.demo.pdf.services.impl.PageNormalizeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class PageNormalizeServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRotateImageToExpectedSize() throws Exception {
        BufferedImage source = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Path sourcePath = tempDir.resolve("page-1.png");
        ImageIO.write(source, "PNG", sourcePath.toFile());

        StorageProperties props = new StorageProperties();
        props.setNormalizedDir(tempDir.resolve("normalized").toString());

        PageNormalizeServiceImpl service = new PageNormalizeServiceImpl(props);
        List<String> output = service.normalize("task1", Collections.singletonList(sourcePath.toString()), Collections.singletonList(90));

        BufferedImage normalized = ImageIO.read(Path.of(output.get(0)).toFile());
        Assertions.assertEquals(100, normalized.getWidth());
        Assertions.assertEquals(200, normalized.getHeight());
    }
}
