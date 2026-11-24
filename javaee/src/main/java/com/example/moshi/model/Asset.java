package com.example.moshi.model;

import java.io.Serializable;

/** 素材（对应 t_asset）。 */
public class Asset implements Serializable {

    private long id;
    private String name;
    private String originalName;
    private String contentType;
    private long sizeBytes;
    private String category;
    private String storedPath;
    private long uploaderId;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }
    public long getUploaderId() { return uploaderId; }
    public void setUploaderId(long uploaderId) { this.uploaderId = uploaderId; }

    public long getSizeKb() {
        return sizeBytes / 1024;
    }
}
