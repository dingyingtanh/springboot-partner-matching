package com.yuli.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.yuli.model.domain.Tag;
import com.yuli.service.TagService;
import com.yuli.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author yuli
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2025-06-11 11:55:01
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService{

}




