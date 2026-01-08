package com.yuli.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuli.model.domain.User;
import com.yuli.model.vo.PageVo;

public class PageVoUtils {

    public PageVo pageVo(Page<User> page){
        PageVo pageVo = new PageVo();
        pageVo.setCurrentPage(page.getCurrent());
        pageVo.setPageSize(page.getSize());
        pageVo.setPageTotal(page.getTotal());
        pageVo.setRecords(page.getRecords());

        return pageVo;
    }
}
