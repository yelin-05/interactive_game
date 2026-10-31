package com.example.minigame.module.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.minigame.module.user.entity.User;
import com.example.minigame.module.user.mapper.UserMapper;
import com.example.minigame.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public User getByUsername(String username) {
        return getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username), false);
    }

    @Override
    @Transactional
    public User register(String username, String rawPassword, String nickname) {
        validate(username, rawPassword);

        if (getByUsername(username) != null) {
            throw new IllegalArgumentException("用户名「" + username + "」已被占用");
        }

        User user = new User();
        user.setUsername(username);
        // BCrypt 自带随机盐，同一个密码每次摘要都不同
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname.trim());
        user.setRole("PLAYER");
        user.setStatus(1);
        save(user);
        return user;
    }

    @Override
    public User login(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("请输入用户名和密码");
        }
        User user = getByUsername(username.trim());
        // 用户名不存在也返回同一句提示，避免被用来枚举账号
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码不正确");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("该账号已被禁用");
        }
        return user;
    }

    private void validate(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        String name = username.trim();
        if (name.length() < 3 || name.length() > 20) {
            throw new IllegalArgumentException("用户名长度需在 3 到 20 之间");
        }
        if (!name.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字和下划线");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }
        if (rawPassword.length() > 32) {
            throw new IllegalArgumentException("密码不能超过 32 位");
        }
    }
}
