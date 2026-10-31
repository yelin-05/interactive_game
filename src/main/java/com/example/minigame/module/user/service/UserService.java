package com.example.minigame.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.minigame.module.user.entity.User;

/**
 * 用户业务接口。继承 IService 后自带 save / getById / count 等通用方法。
 */
public interface UserService extends IService<User> {

    /** 按用户名查用户，登录时用。 */
    User getByUsername(String username);

    /**
     * 注册。用户名重复、密码不合规时抛 IllegalArgumentException，
     * 由 AuthController 捕获后回填到页面错误提示。
     */
    User register(String username, String rawPassword, String nickname);

    /** 登录校验，失败抛 IllegalArgumentException。 */
    User login(String username, String rawPassword);
}
