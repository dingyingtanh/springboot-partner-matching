package com.yuli.once;


import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入星球数据信息到数据库
 */
public class ImportXingQiuUser {
    public static void main(String[] args) {
        String fileName = "C:\\Users\\dingy\\Documents\\工作簿1.xlsx";
        //这里 需要指定被读那个class去读 然后去读取第一个sheet 同步读取会自动的finish
        List<XingQiuUserInfo> objects = EasyExcel.read(fileName).head(XingQiuUserInfo.class).sheet().doReadSync();
        for (XingQiuUserInfo object : objects) {
            System.out.println(object);
        }
        Map<String, List<XingQiuUserInfo>> listMap = objects.stream().filter(userInfo-> StringUtils.isNotEmpty(userInfo.getUsername())).collect(Collectors.groupingBy(XingQiuUserInfo::getPlanetCode));
        System.out.println("不重复昵称数据=" + listMap.keySet().size());
        for (Map.Entry<String,List<XingQiuUserInfo>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size()>0) {
                System.out.println("username"+stringListEntry.getKey());
                System.out.println("1");
            }
        }

    }
}
