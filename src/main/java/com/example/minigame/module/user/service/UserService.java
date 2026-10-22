package com.example.minigame.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.minigame.module.user.entity.User;

/**
 * 用户业务接口。继承 IService 后自带 save / getById / count 等通用方法。
 */
public interface UserService extends IService<User> {

    /** 按用户名查用户，登录时用。 */
    User getByUsername(String username);
}
