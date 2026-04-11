# PDF 表格方向纠正 + 结构提取 Demo（最小可运行版）

这是一个 Java 11 + Spring Boot 的本地 demo，目标是跑通：

1. 上传 PDF
2. PDF 按页转 PNG
3. 检测每页方向（当前最小版先返回 0）
4. 旋转输出标准方向图片

## 技术选型

- Spring Boot 2.7.x：稳定、Java 11 友好。
- Apache PDFBox：开源（Apache License 2.0）、轻量、适合做 PDF 转图。
- 暂未引入 OCR 大依赖：第一版重点先保证端到端流程可运行。

> 后续增强可接入 Tesseract（tess4j）实现真实方向检测。

## 项目结构

```text
src/main/java/com/demo/pdf
├── PdfConvertDemoApplication.java
├── common
│   └── Result.java
├── controllers
│   └── PdfTaskController.java
├── exceptions
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── helpers
│   ├── ImageRotateHelper.java
│   └── OcrOrientationParser.java
├── models
│   ├── PageAngle.java
│   ├── TaskResult.java
│   └── UploadResponse.java
├── options
│   └── StorageProperties.java
└── services
    ├── FileStorageService.java
    ├── OrientationDetectionService.java
    ├── PageNormalizeService.java
    ├── PdfRenderService.java
    ├── PdfTaskService.java
    └── impl
        ├── LocalFileStorageService.java
        ├── PageNormalizeServiceImpl.java
        ├── PdfBoxRenderService.java
        ├── PdfTaskServiceImpl.java
        └── SimpleOrientationDetectionService.java
```

## 本地运行

```bash
mvn spring-boot:run
```

## 接口

- `POST /api/pdf/upload` 上传 PDF 并执行完整流程
- `GET /api/pdf/{taskId}/angles` 查看页面角度
- `GET /api/pdf/{taskId}/normalized` 查看纠正后图片路径

## 测试

```bash
mvn test
```
