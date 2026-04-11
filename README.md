# PDF 表格方向纠正 + 结构提取 Demo（最小可运行版）

这是一个 Java 11 + Spring Boot 的本地 demo，目标是跑通：

1. 上传 PDF
2. PDF 按页转 PNG
3. 检测每页方向（默认调用 Tesseract OSD，失败时单页回退到 0）
4. 旋转输出标准方向图片

## 技术选型

- Spring Boot 2.7.x：稳定、Java 11 友好。
- Apache PDFBox：开源（Apache License 2.0）、轻量、适合做 PDF 转图。
- 方向检测通过外部 `tesseract` 命令实现 OSD，不额外引入 JNI OCR 依赖。

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

- `POST /api/pdf/upload` 上传 PDF 并执行完整流程（返回 `taskId`、上传路径、`angles`）
- `GET /api/pdf/{taskId}/angles` 查看页面角度
- `GET /api/pdf/{taskId}/normalized` 查看纠正后图片路径

## 测试

```bash
mvn test
```

## OCR 配置（可选）

默认使用系统 `tesseract` 命令，支持通过环境变量覆盖：

- `OCR_ENABLED`：是否启用 OCR（默认 `true`）
- `OCR_EXECUTABLE`：可执行文件名或绝对路径（默认 `tesseract`）
- `OCR_LANGUAGE`：OSD 语言包（默认 `osd`）
- `OCR_TIMEOUT_MS`：单页超时毫秒（默认 `8000`）

## Docker 本地运行

```bash
docker build -t pdf-convert-demo:latest .
docker run --rm -p 8080:8080 -e PORT=8080 -v $(pwd)/data:/app/data pdf-convert-demo:latest
```

## Render 部署（Docker）

本项目已提供 `Dockerfile` + `render.yaml`，可以直接在 Render 创建 **Web Service**：

1. 推送代码到 GitHub。
2. 在 Render 里选择 **New + > Blueprint**，选择该仓库。
3. Render 会读取 `render.yaml` 自动创建 Docker Web Service。
4. 部署后访问 Render 分配的 URL。

> 注意：Render 的磁盘是临时的（ephemeral）。该 demo 的 `./data` 目录中间产物在重启后可能丢失。

## Render 500 / Java heap space 排查

如果调用上传接口报错：`OutOfMemoryError: Java heap space`，可按下面处理：

1. 减小渲染 DPI（默认已调到 120）：设置环境变量 `RENDER_DPI=100` 或更低。
2. 限制 PDF 页数（默认 30 页）：设置 `RENDER_MAX_PAGES=20`。
3. 提高 JVM 堆内存：设置 `JAVA_OPTS=-Xms128m -Xmx384m -XX:+UseSerialGC`（或根据套餐上调）。
4. 上传更小的 PDF（当前接口限制 15MB）。

> 本项目已在 `PdfBoxRenderService` 中启用 `MemoryUsageSetting.setupTempFileOnly()`，尽量用磁盘换内存，降低 OOM 概率。
