package com.example.musicplayer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

class MyFilter implements FilenameFilter {
    private String type;
    public MyFilter(String type){ //构造函数
        this.type = type;
    }
    @Override //实现接口accept()方法
    public boolean accept(File dir, String name) { //dir当前目录, name文件名
        return name.endsWith(type); //返回true的文件则合格
    }
}
