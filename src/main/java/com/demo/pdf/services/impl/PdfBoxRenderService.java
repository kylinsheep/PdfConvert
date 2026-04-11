package com.demo.pdf.services.impl;

import com.demo.pdf.exceptions.BusinessException;
import com.demo.pdf.options.RenderProperties;
import com.demo.pdf.options.StorageProperties;
import com.demo.pdf.services.PdfRenderService;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
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
    private final RenderProperties renderProperties;

    public PdfBoxRenderService(StorageProperties storageProperties,
                               RenderProperties renderProperties) {
        this.storageProperties = storageProperties;
        this.renderProperties = renderProperties;
    }

    @Override
    public List<String> renderToImages(String taskId, String pdfPath) {
        Path taskDir = Paths.get(storageProperties.getPagesDir(), taskId);
        try (PDDocument document = PDDocument.load(Paths.get(pdfPath).toFile(), MemoryUsageSetting.setupTempFileOnly())) {
            int pageCount = document.getNumberOfPages();
            if (pageCount > renderProperties.getMaxPages()) {
                throw new BusinessException("PDF page count exceeds limit: " + renderProperties.getMaxPages());
            }

            Files.createDirectories(taskDir);
            PDFRenderer renderer = new PDFRenderer(document);
            List<String> outputPaths = new ArrayList<>();
            ImageType imageType = renderProperties.isGrayscale() ? ImageType.GRAY : ImageType.RGB;

            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, renderProperties.getDpi(), imageType);
                Path pagePath = taskDir.resolve("page-" + (i + 1) + ".png");
                ImageIO.write(image, "PNG", pagePath.toFile());
                image.flush();
                outputPaths.add(pagePath.toAbsolutePath().toString());
            }
            return outputPaths;
        } catch (OutOfMemoryError oom) {
            throw new BusinessException("Not enough memory while rendering PDF. Try smaller PDF / lower DPI / fewer pages.");
        } catch (IOException ex) {
            throw new BusinessException("Failed to render PDF pages: " + ex.getMessage());
        }
    }
}
