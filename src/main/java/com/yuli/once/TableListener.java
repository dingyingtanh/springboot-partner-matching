package com.yuli.once;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;


/**
 * @author dingy
 */
@Slf4j
public class TableListener implements ReadListener<XingQiuUserInfo> {
    @Override
    public void invoke(XingQiuUserInfo xingQiuUserInfo, AnalysisContext analysisContext) {
        System.out.println(xingQiuUserInfo);
    }
    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        System.out.println("所有数据解析完成！");
    }

}