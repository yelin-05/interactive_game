package com.example.minigame.module.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.minigame.module.quiz.entity.Score;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScoreMapper extends BaseMapper<Score> {
}
