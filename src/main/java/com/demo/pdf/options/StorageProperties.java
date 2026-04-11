package com.demo.pdf.options;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String uploadDir;
    private String pagesDir;
    private String normalizedDir;
    private String tempDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getPagesDir() {
        return pagesDir;
    }

    public void setPagesDir(String pagesDir) {
        this.pagesDir = pagesDir;
    }

    public String getNormalizedDir() {
        return normalizedDir;
    }

    public void setNormalizedDir(String normalizedDir) {
        this.normalizedDir = normalizedDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }
}
