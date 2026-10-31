package com.example.minigame.module.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 素材 t_asset。文件落在磁盘 uploads/ 下，库里只存相对路径。
 */
@Data
@TableName("t_asset")
public class Asset {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String originalName;

    private String contentType;

    private Long sizeBytes;

    /** 归类分区，可空 */
    private String category;

    /** 相对 uploads/ 的路径，如 202610/ab12cd.png */
    private String storedPath;

    private Long uploaderId;

    private LocalDateTime createdAt;
}
