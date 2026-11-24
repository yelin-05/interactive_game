package com.example.moshi.service;

import com.example.moshi.dao.AssetDao;
import com.example.moshi.model.Asset;
import com.example.moshi.util.UploadPaths;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class AssetService {

    private static final Set<String> EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private final AssetDao assets = new AssetDao();

    public Asset upload(long uploaderId, Part part, String name, String category)
            throws IOException, java.sql.SQLException {
        if (part == null || part.getSize() <= 0) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        if (part.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件太大（单文件上限 10MB）");
        }
        String submitted = part.getSubmittedFileName();
        String submittedExt = extension(submitted);
        if (!EXTENSIONS.contains(submittedExt)) {
            throw new IllegalArgumentException("只支持 png / jpg / jpeg / gif / webp 图片");
        }

        File root = UploadPaths.root();
        File temp = File.createTempFile("moshi-upload-", ".tmp", root);
        File target = null;
        boolean persisted = false;
        try {
            part.write(temp.getAbsolutePath());
            String detected = detect(temp);
            String actualExt = extensionFromMime(detected);
            if (!actualExt.equals(submittedExt) && !(actualExt.equals("jpeg") && submittedExt.equals("jpg"))) {
                throw new IllegalArgumentException("文件内容与图片扩展名不一致");
            }

            String stored = UUID.randomUUID().toString().replace("-", "") + "." + actualExt;
            target = new File(root, stored);
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);

            Asset asset = new Asset();
            asset.setName(limit(name == null || name.isBlank() ? submitted : name.trim(), 120, "未命名素材"));
            asset.setOriginalName(limit(submitted, 255, stored));
            asset.setContentType(detected);
            asset.setSizeBytes(part.getSize());
            asset.setCategory(category == null || category.isBlank() ? null : category);
            asset.setStoredPath(stored);
            asset.setUploaderId(uploaderId);
            assets.insert(asset);
            persisted = true;
            return asset;
        } finally {
            Files.deleteIfExists(temp.toPath());
            if (target != null && !persisted) {
                Files.deleteIfExists(target.toPath());
            }
        }
    }

    public List<Asset> list(String category, int limit) throws java.sql.SQLException {
        return assets.list(category, limit);
    }

    public long count() throws java.sql.SQLException {
        return assets.count();
    }

    public Asset find(String storedPath) throws java.sql.SQLException {
        return assets.findByStoredPath(storedPath);
    }

    private String detect(File file) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                if (isWebP(file)) {
                    return "image/webp";
                }
                throw new IllegalArgumentException("文件不是有效图片");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > 4096 || height > 4096
                        || (long) width * height > 16_000_000L) {
                    throw new IllegalArgumentException("图片尺寸超出限制");
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new IllegalArgumentException("图片内容损坏");
                }
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if ("jpg".equals(format) || "jpeg".equals(format)) {
                    return "image/jpeg";
                }
                if ("png".equals(format)) {
                    return "image/png";
                }
                if ("gif".equals(format)) {
                    return "image/gif";
                }
                throw new IllegalArgumentException("不支持的图片格式");
            } finally {
                reader.dispose();
            }
        }
    }

    private boolean isWebP(File file) throws IOException {
        byte[] header = Files.readAllBytes(file.toPath());
        if (header.length < 16) {
            return false;
        }
        String riff = new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);
        String webp = new String(header, 8, 4, java.nio.charset.StandardCharsets.US_ASCII);
        String chunk = new String(header, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        return "RIFF".equals(riff) && "WEBP".equals(webp)
                && ("VP8 ".equals(chunk) || "VP8L".equals(chunk) || "VP8X".equals(chunk));
    }

    private String extensionFromMime(String mime) {
        switch (mime) {
            case "image/png": return "png";
            case "image/gif": return "gif";
            case "image/webp": return "webp";
            default: return "jpeg";
        }
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 || dot == filename.length() - 1
                ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String limit(String value, int max, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
