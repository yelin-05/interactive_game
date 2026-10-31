package com.example.minigame.module.asset.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.minigame.module.asset.entity.Asset;
import com.example.minigame.module.asset.mapper.AssetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 素材存取。
 *
 * <p>安全点：
 * <ul>
 *   <li>只允许图片后缀，其它一律拒绝</li>
 *   <li>落盘文件名用 UUID 重新生成，不用用户上传的文件名（防 ../ 路径穿越）</li>
 *   <li>大小上限由 spring.servlet.multipart.max-file-size 控制（10MB）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    /** uploads 目录（相对工程根目录） */
    public static final Path ROOT = Paths.get("uploads").toAbsolutePath().normalize();

    private final AssetMapper assetMapper;

    public Asset save(String name, String category, Long uploaderId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        String original = file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename();
        String ext = extension(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("只支持 png / jpg / jpeg / gif / webp 图片");
        }

        String month = LocalDate.now().format(MONTH);
        Path dir = ROOT.resolve(month);
        Files.createDirectories(dir);

        String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        file.transferTo(dir.resolve(stored).toFile());

        Asset asset = new Asset();
        asset.setName(name == null || name.isBlank() ? original : name.trim());
        asset.setOriginalName(original);
        asset.setContentType(file.getContentType());
        asset.setSizeBytes(file.getSize());
        asset.setCategory(category);
        asset.setStoredPath(month + "/" + stored);
        asset.setUploaderId(uploaderId);
        assetMapper.insert(asset);

        log.info("素材上传成功: id={} name={} size={}", asset.getId(), asset.getName(), asset.getSizeBytes());
        return asset;
    }

    public List<Asset> list(String category, int limit) {
        var query = Wrappers.<Asset>lambdaQuery();
        if (category != null && !category.isBlank()) {
            query.eq(Asset::getCategory, category);
        }
        query.orderByDesc(Asset::getId).last("limit " + Math.max(1, Math.min(limit, 200)));
        return assetMapper.selectList(query);
    }

    public long count() {
        return assetMapper.selectCount(null);
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
