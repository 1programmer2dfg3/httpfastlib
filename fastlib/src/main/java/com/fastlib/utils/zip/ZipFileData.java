package com.fastlib.utils.zip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgfb on 17/7/24.
 * 压缩文件信息
 */
public class ZipFileData{
    public boolean deflate; //如果是压缩为true否则false
    public boolean isDirection;
    public long compressSize;
    public long uncompressSize;
    public long lastModifiedTime;
    public String name;
    public String comment;
    public ZipFileData parent;
    public List<ZipFileData> children=new ArrayList<>();

    /**
     * 获取完整路径
     * @return 完整路径
     */
    public String getPath(){
        String completePath=name+(isDirection?File.separator:"");
        ZipFileData parent=this.parent;

        while(parent!=null) {
            completePath = parent.name + File.separator + completePath;
            parent=parent.parent;
        }
        return completePath;
    }
}