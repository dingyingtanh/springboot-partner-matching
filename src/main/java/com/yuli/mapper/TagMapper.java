package com.yuli.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuli.model.domain.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yuli
* @description 针对表【tag(标签)】的数据库操作Mapper
* @createDate 2025-06-11 11:55:01
* @Entity generator.domain.Tag
*/
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

}




