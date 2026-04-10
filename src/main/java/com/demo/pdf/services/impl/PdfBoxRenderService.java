package com.demo.pdf.services.impl;

import com.demo.pdf.exceptions.BusinessException;
import com.demo.pdf.options.StorageProperties;
import com.demo.pdf.services.PdfRenderService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfBoxRenderService implements PdfRenderService {

    private final StorageProperties storageProperties;
    private final int dpi;

    public PdfBoxRenderService(StorageProperties storageProperties,
                               @Value("${app.render.dpi:200}") int dpi) {
        this.storageProperties = storageProperties;
        this.dpi = dpi;
    }

    @Override
    public List<String> renderToImages(String taskId, String pdfPath) {
        Path taskDir = Paths.get(storageProperties.getPagesDir(), taskId);
        try (PDDocument document = PDDocument.load(Paths.get(pdfPath).toFile())) {
            Files.createDirectories(taskDir);
            PDFRenderer renderer = new PDFRenderer(document);
            List<String> outputPaths = new ArrayList<>();
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                Path pagePath = taskDir.resolve("page-" + (i + 1) + ".png");
                ImageIO.write(image, "PNG", pagePath.toFile());
                outputPaths.add(pagePath.toAbsolutePath().toString());
            }
            return outputPaths;
        } catch (IOException ex) {
            throw new BusinessException("Failed to render PDF pages: " + ex.getMessage());
        }
    }
}
