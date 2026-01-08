package com.yuli.once;


import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * @author dingy
 */
public class ImportExcel {
    /**
     * 读取数据
     */
    public static void main(String[] args) {
        String fileName = "C:\\Users\\dingy\\Documents\\工作簿1.xlsx";
        readByListener(fileName);
        synchronouseRead(fileName);
    }

    /**
     * 使用监听器读取数据
     */
    public static void readByListener(String fileName) {
        // 读取excel
        EasyExcel.read(fileName, XingQiuUserInfo.class, new TableListener()).sheet().doRead();

    }

    /**
     * 同步读取数据
     * @param fileName
     */
    public static void synchronouseRead(String fileName) {
        //这里 需要指定被读那个class去读 然后去读取第一个sheet 同步读取会自动的finish
        List<XingQiuUserInfo> objects = EasyExcel.read(fileName).head(XingQiuUserInfo.class).sheet().doReadSync();
        for (XingQiuUserInfo object : objects) {
            System.out.println(object);
        }
    }
}
