package com.demo.pdf.services;

import java.util.List;

public interface PdfRenderService {
    List<String> renderToImages(String taskId, String pdfPath);
}
