package com.example.minigame.module.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.minigame.module.quiz.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
