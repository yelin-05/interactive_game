package com.example.moshi.dao;

import com.example.moshi.model.Asset;
import com.example.moshi.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 素材 DAO。 */
public class AssetDao {

    public void insert(Asset a) throws SQLException {
        String sql = "INSERT INTO t_asset (name, original_name, content_type, size_bytes, category, "
                + "stored_path, uploader_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getName());
            ps.setString(2, a.getOriginalName());
            ps.setString(3, a.getContentType());
            ps.setLong(4, a.getSizeBytes());
            ps.setString(5, a.getCategory());
            ps.setString(6, a.getStoredPath());
            ps.setLong(7, a.getUploaderId());
            ps.executeUpdate();
        }
    }

    public List<Asset> list(String category, int limit) throws SQLException {
        String sql = "SELECT id, name, original_name, content_type, size_bytes, category, stored_path "
                + "FROM t_asset "
                + (category == null || category.isBlank() ? "" : "WHERE category = ? ")
                + "ORDER BY id DESC LIMIT ?";
        List<Asset> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (category != null && !category.isBlank()) {
                ps.setString(idx++, category);
            }
            ps.setInt(idx, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asset a = new Asset();
                    a.setId(rs.getLong("id"));
                    a.setName(rs.getString("name"));
                    a.setOriginalName(rs.getString("original_name"));
                    a.setContentType(rs.getString("content_type"));
                    a.setSizeBytes(rs.getLong("size_bytes"));
                    a.setCategory(rs.getString("category"));
                    a.setStoredPath(rs.getString("stored_path"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    public Asset findByStoredPath(String storedPath) throws SQLException {
        String sql = "SELECT id, name, original_name, content_type, size_bytes, category, stored_path, "
                + "uploader_id FROM t_asset WHERE stored_path = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, storedPath);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Asset a = new Asset();
                a.setId(rs.getLong("id"));
                a.setName(rs.getString("name"));
                a.setOriginalName(rs.getString("original_name"));
                a.setContentType(rs.getString("content_type"));
                a.setSizeBytes(rs.getLong("size_bytes"));
                a.setCategory(rs.getString("category"));
                a.setStoredPath(rs.getString("stored_path"));
                a.setUploaderId(rs.getLong("uploader_id"));
                return a;
            }
        }
    }

    public long count() throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM t_asset");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
}
