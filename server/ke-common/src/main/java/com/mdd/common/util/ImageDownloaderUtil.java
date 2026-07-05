package com.mdd.common.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class ImageDownloaderUtil {

    /**
     * 单张图片最小字节数，低于该值视为无效图并丢弃
     */
    public static final int MIN_IMAGE_BYTES = 50 * 1024;

    /**
     * 抓取图片下载结果
     */
    public static class FetchedImage {
        private final byte[] bytes;
        private final String extension;

        public FetchedImage(byte[] bytes, String extension) {
            this.bytes = bytes;
            this.extension = extension;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getExtension() {
            return extension;
        }
    }

    public static void downloadImage(String imageUrl, Map<String, String> headers, String savePath) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.setRequestMethod("GET");
        conn.connect();

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        conn.disconnect();
    }

    /**
     * 下载远程图片到内存，返回 byte[] 与探测出的扩展名
     * - 小于 {@link #MIN_IMAGE_BYTES} 的图片会被视为无效
     * - 扩展名探测顺序：ImageReader > Content-Type > URL
     */
    public static FetchedImage downloadImageAsBytes(String imageUrl, Map<String, String> headers) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("Accept-Encoding", "identity");
        conn.connect();

        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP status " + conn.getResponseCode() + " for " + imageUrl);
            }

            byte[] bytes;
            try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                bytes = out.toByteArray();
            }
            if (bytes.length < MIN_IMAGE_BYTES) {
                return null;
            }
            String ext = detectExtension(bytes, conn.getContentType(), imageUrl);
            return new FetchedImage(bytes, ext);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 通过 ImageReader / Content-Type / URL 后缀 探测图片扩展名
     */
    public static String detectExtension(byte[] bytes, String contentType, String imageUrl) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (iis != null) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    try {
                        return getFileExtension(reader.getFormatName());
                    } finally {
                        reader.dispose();
                    }
                }
            }
        } catch (IOException ignored) {
        }
        if (contentType != null && contentType.startsWith("image/")) {
            String sub = contentType.substring(6).split(";")[0].trim().toLowerCase();
            if (!sub.isEmpty()) return getFileExtension(sub);
        }
        if (imageUrl != null) {
            int q = imageUrl.indexOf('?');
            String clean = q > 0 ? imageUrl.substring(0, q) : imageUrl;
            int dot = clean.lastIndexOf('.');
            if (dot > 0 && dot < clean.length() - 1) {
                return getFileExtension(clean.substring(dot + 1).toLowerCase());
            }
        }
        return "jpg";
    }

    /**
     * 增强版远程图片处理：多重方式获取图片信息并保存
     */
    public static String processRemoteImageWithFallback(String imageUrl, String savePath) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept-Encoding", "identity"); // 避免压缩传输

        connection.connect();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // 方法1：使用ImageReader获取精确格式信息
            ImageInputStream iis = ImageIO.createImageInputStream(connection.getInputStream());
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis, true);

                String formatName = reader.getFormatName();
                // 读取图片数据
                BufferedImage image = reader.read(0);
                reader.dispose();

                // 保存图片
                boolean saved = saveImageWithDetectedFormat(image, formatName, savePath);
                if (saved) {
                    return savePath + "." + getFileExtension(formatName);
                } else {
                    System.out.println("图片保存失败");
                }

            } else {
                // 方法2：回退到传统方式
                System.out.println("ImageReader无法识别格式，使用传统方式处理");
                processWithTraditionalMethod(connection, imageUrl, savePath);
            }

            iis.close();
        } else {
            System.out.println("HTTP连接失败，响应码: " + connection.getResponseCode());
        }

        connection.disconnect();
        return null;
    }

    /**
     * 传统处理方式
     */
    private static void processWithTraditionalMethod(HttpURLConnection connection, String imageUrl, String savePath) throws IOException {
        InputStream inputStream = connection.getInputStream();
        BufferedImage image = ImageIO.read(inputStream);

        if (image != null) {
            String contentType = connection.getContentType();
            String format = getImageFormat(contentType, imageUrl);

            boolean saved = saveImage(image, format, savePath + "." + getFileExtension(format));
            if (saved) {
                System.out.println("图片已保存到: " + savePath + "." + getFileExtension(format));
            } else {
                System.out.println("图片保存失败");
            }

            inputStream.close();
        } else {
            System.out.println("无法读取图片数据");
        }
    }

    /**
     * 根据Content-Type和URL获取图片格式
     */
    private static String getImageFormat(String contentType, String imageUrl) {
        // 优先从Content-Type获取格式
        if (contentType != null && contentType.startsWith("image/")) {
            return contentType.substring(6);
        }

        // 从URL中提取文件扩展名
        if (imageUrl != null && imageUrl.lastIndexOf(".") > 0) {
            String extension = imageUrl.substring(imageUrl.lastIndexOf(".") + 1).toLowerCase();

            switch (extension) {
                case "jpg":
                case "jpeg":
                    return "jpeg";
                case "png":
                    return "png";
                case "gif":
                    return "gif";
                case "bmp":
                    return "bmp";
                case "webp":
                    return "webp";
                default:
                    return extension;
            }
        }

        return "jpeg"; // 默认格式
    }

    /**
     * 保存图片到本地文件
     */
    private static boolean saveImage(BufferedImage image, String format, String filePath) {
        try {
            File outputFile = new File(filePath);
            if (outputFile.exists()) {
                System.out.println("文件已存在，跳过保存: " + filePath);
                return false;
            }
            ImageIO.write(image, format, outputFile);
            //取得保存后图片文件大小
            long fileSize = (outputFile.length() + 512) / 1024;
            System.out.println("保存的图片文件大小: " + fileSize + " KB");
            if (fileSize < 50) {
                boolean delete = outputFile.delete();
                return false;
            }
            return true;
        } catch (IOException e) {
            System.err.println("保存图片时发生错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 使用检测到的格式保存图片
     */
    private static boolean saveImageWithDetectedFormat(BufferedImage image, String formatName, String basePath) {
        try {
            String extension = getFileExtension(formatName);
            String filePath = basePath + "." + extension;
            File outputFile = new File(filePath);

            if (outputFile.exists()) {
                System.out.println("文件已存在，跳过保存: " + filePath);
                return false;
            }
            ImageIO.write(image, formatName, outputFile);

            long fileSize = (outputFile.length() + 512) / 1024;
            System.out.println("保存的图片文件大小: " + fileSize + " KB");
            if (fileSize < 50) {
                boolean delete = outputFile.delete();
                return false;
            }

            System.out.println("保存的图片文件大小: " + fileSize + " KB");

            return true;
        } catch (IOException e) {
            System.err.println("保存图片时发生错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String formatName) {
        if (formatName == null) return "jpg";
        switch (formatName.toLowerCase()) {
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "gif":
                return "gif";
            case "bmp":
                return "bmp";
            case "webp":
                return "webp";
            default:
                return formatName.toLowerCase();
        }
    }
}
