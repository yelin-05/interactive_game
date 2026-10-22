package com.example.minigame.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 t_user。
 *
 * <p>任务发布者、素材上传者、排行榜玩家都指向这张表，是后面所有模块的地基。
 * 密码只存 BCrypt 摘要，绝不存明文。
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名，全局唯一 */
    private String username;

    /** BCrypt 密码摘要（60 位左右），不允许明文 */
    private String password;

    /** 昵称，排行榜上显示的名字 */
    private String nickname;

    /** 头像地址，可以是内置占位图 */
    private String avatar;

    /** 角色：PLAYER / ADMIN */
    private String role;

    /** 1 正常，0 禁用 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
