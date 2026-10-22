package com.example.minigame.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.minigame.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。继承 BaseMapper 即拥有单表增删改查，复杂 SQL 后续写在
 * resources/mapper/UserMapper.xml 里。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
