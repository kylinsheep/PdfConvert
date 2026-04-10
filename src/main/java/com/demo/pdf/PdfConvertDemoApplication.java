package com.demo.pdf;

import com.demo.pdf.options.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class PdfConvertDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfConvertDemoApplication.class, args);
    }
}
