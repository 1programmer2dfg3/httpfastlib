package com.fastlib.utils.zip;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fastlib.db.SaveUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by sgfb on 17/7/24.
 * zip压缩文件实体
 */
public class ZipFileEntity{
    private ZipFile mZipFile;
    private List<ZipFileData> mZipFileDataList=new ArrayList<>();

    /**
     * 解压构造
     * @param file
     * @throws IOException
     */
    private ZipFileEntity(File file)throws IOException{
        mZipFile=new ZipFile(file);
        Enumeration<?extends ZipEntry> entries=mZipFile.entries();
        Map<String,ZipFileData> pathToZipFile=new HashMap<>(); //路径对压缩文件实体

        while(entries.hasMoreElements()){
            ZipEntry entry=entries.nextElement();
            ZipFileData zfd=parseZipEntry(entry);

            File entryFile=new File(entry.getName());
            if(entryFile.getParent()!=null&&pathToZipFile.containsKey(entryFile.getParent())){
                ZipFileData parent=pathToZipFile.get(entryFile.getParent());
                parent.children.add(zfd);
                zfd.parent=parent;
            }
            else mZipFileDataList.add(zfd);
            if(entry.getName().endsWith(File.separator))
                pathToZipFile.put(entry.getName().substring(0, entry.getName().length() - 1), zfd);
            computeDirectionContainSize(zfd);
        }
    }

    /**
     * 压缩构造
     * @param level 压缩级别
     * @param srcFile 要被压缩的文件和文件夹
     * @param destFile 压缩目标位置
     * @param callback 进度监听
     * @throws IOException
     */
    private ZipFileEntity(int level,File[] srcFile,File destFile,ProgressCallback callback) throws IOException{
        ZipOutputStream zos=new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(destFile),new Adler32()));
        SaveUtil.FileSizeWrapper fileSizeWrapper=new SaveUtil.FileSizeWrapper();
        CallbackWrapper callbackWrapper=null;

        zos.setLevel(level);
        if(callback!=null){ //如果回调是空的，跳过文件数和占用空间查询，节省时间
            for(File file:srcFile){
                SaveUtil.FileSizeWrapper oneOfFiles=SaveUtil.fileSizeDetail(file);
                fileSizeWrapper.fileCount+=oneOfFiles.fileCount;
                fileSizeWrapper.directionCount+=oneOfFiles.directionCount;
                fileSizeWrapper.fileSizeCount+=oneOfFiles.fileSizeCount;
            }
            callback.prepared();
            callbackWrapper=new CallbackWrapper(fileSizeWrapper.fileCount,fileSizeWrapper.fileSizeCount,callback);
            callbackWrapper.uncompressedByte=callbackWrapper.countByte;
            callbackWrapper.uncompressFileCount=callbackWrapper.fileCount;
        }
        for(File file:srcFile)
            compressEntry(zos, file, file.getParent(), null,callbackWrapper);
        zos.close();
        mZipFile=new ZipFile(destFile);
    }

    /**
     * 压缩文件并且返回压缩文件实体
     * @param needCompressFile 将要压缩的文件组
     * @param destFile 压缩文件的存储路径，不存在将被创建
     * @return
     */
    public static ZipFileEntity open(File[] needCompressFile,File destFile){
        return open(needCompressFile,destFile,Deflater.DEFAULT_COMPRESSION,null);
    }

    /**
     * 压缩文件并且返回压缩文件实体,指定进度监听
     * @param needCompressFile 将要压缩的文件组
     * @param destFile 压缩文件的存储路径，不存在将被创建
     * @param callback 进度监听
     * @return
     */
    public static ZipFileEntity open(File[] needCompressFile, File destFile,int level,ProgressCallback callback){
        try {
            boolean createSuccess=true;
            if(!destFile.exists())
                createSuccess=destFile.createNewFile();
            if(createSuccess)
                return new ZipFileEntity(level,needCompressFile,destFile,callback);
            else System.out.println("创建压缩目标文件失败");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析并返回压缩文件实体
     * @param file 压缩文件
     * @return 压缩文件实体
     */
    public static ZipFileEntity open(File file) throws IOException {
        return new ZipFileEntity(file);
    }

    /**
     * 压缩子实体
     * @param zos 压缩流
     * @param file 被压缩的文件
     * @param rootParentPath 父路径
     * @param parentZfd 父ZipFileData
     * @throws IOException
     */
    private ZipFileData compressEntry(ZipOutputStream zos, File file, String rootParentPath, ZipFileData parentZfd,CallbackWrapper callback) throws IOException {
        if(file==null||!file.exists()) return null;
        //+1是因为还有分隔符.最后的分隔符来指名在压缩包里是否是文件夹
        ZipEntry entry=new ZipEntry(file.getAbsolutePath().substring(rootParentPath.length()+1)+(file.isDirectory()?File.separator:""));
        ZipFileData zfd;

        //如果是文件夹，遍历压缩子文件或文件夹.文件直接压缩
        if(file.isDirectory()){
            entry.setCrc(0);
            entry.setCompressedSize(0);
            entry.setMethod(ZipEntry.STORED);
            zos.putNextEntry(entry);
            zos.closeEntry();
            zfd=parseZipEntry(entry);
            zfd.parent=parentZfd;
            File[] children=file.listFiles();
            for(File child:children){
                ZipFileData childZfd=compressEntry(zos,child,rootParentPath,zfd,callback);
                zfd.children.add(childZfd);
            }
        }
        else{
            zos.putNextEntry(entry);
            InputStream fIn=new FileInputStream(file);
            byte[] data=new byte[4096];
            int len;

            while((len=fIn.read(data))!=-1)
                zos.write(data,0,len);
            fIn.close();
            if(callback!=null)
                callback.callback.onUncompressing(callback.fileCount,callback.uncompressFileCount-=1,callback.countByte,callback.uncompressedByte-=file.length());
            zos.closeEntry();
            zfd=parseZipEntry(entry);
            zfd.parent=parentZfd;
        }
        if(parentZfd==null)
            mZipFileDataList.add(zfd);
        computeDirectionContainSize(zfd);
        return zfd;
    }

    /**
     * 解压文件到压缩文件当前目录
     * @throws IOException
     */
    public void uncompress()throws IOException{
        uncompress(null);
    }

    /**
     * 解压文件到压缩文件当前目录.有回调
     * @param callback 回调
     * @throws IOException
     */
    public void uncompress(ProgressCallback callback) throws IOException{
        File parent=new File(mZipFile.getName()).getParentFile();
        uncompress(callback,parent);
    }

    /**
     * 解压文件到指定位置
     * @param callback 进度回调监听
     * @param destFile 指定位置,如果不存在打印提示
     * @throws IOException
     */
    public void uncompress(ProgressCallback callback,File destFile)throws IOException{
        if(destFile.exists()&&destFile.isDirectory()){
            File zipFile=new File(mZipFile.getName());
            int suffixIndex=zipFile.getName().lastIndexOf(".");
            int nameLength=suffixIndex==-1?zipFile.getName().length()-1:suffixIndex;
            File rootDirectory=new File(destFile,zipFile.getName().substring(0,nameLength));
            File defendDuplicationFile=rootDirectory;
            int defendDuplicationIndex=2;

            //安全创建根目录
            while(defendDuplicationFile.exists())
                defendDuplicationFile = new File(rootDirectory.getAbsolutePath() + (defendDuplicationIndex++));
            rootDirectory=defendDuplicationFile;
            rootDirectory.mkdir();

            CallbackWrapper wrapper=null;
            if(callback!=null){
                int fileCount=0; //回调用文件总数
                long byteCount=0; //回调用字节总数
                fileCount=getFileCount(mZipFileDataList);
                byteCount=getByteCount(mZipFileDataList);
                wrapper=new CallbackWrapper(fileCount,byteCount,callback);
            }
            //遍历解压所有文件夹和文件
            for(ZipFileData zfd:mZipFileDataList)
                uncompressItems(zfd,rootDirectory,wrapper);
        }
        else System.out.println("文件夹不存在或者指定的路径是一个文件");
    }

    /**
     * 解压选定的文件组
     * @param zipFileData 被解压的组
     * @param parentFile 目标父位置
     * @param progressCallback 回调
     */
    public void uncompress(ZipFileData zipFileData,File parentFile,@Nullable ProgressCallback progressCallback) throws IOException{
        CallbackWrapper callbackWrapper=null;

        if(progressCallback!=null){
            SaveUtil.FileSizeWrapper fileSizeWrapper=getZipFileDataSize(zipFileData);
            callbackWrapper=new CallbackWrapper(fileSizeWrapper.fileCount,fileSizeWrapper.fileSizeCount,progressCallback);
            callbackWrapper.uncompressFileCount=fileSizeWrapper.fileCount;
            callbackWrapper.uncompressedByte=fileSizeWrapper.fileSizeCount;
            progressCallback.prepared();
        }
        uncompress(zipFileData,parentFile,callbackWrapper);
    }

    private void uncompress(ZipFileData zipFileData,File parentFile,@Nullable CallbackWrapper callbackWrapper)throws IOException{
        File file=new File(parentFile,zipFileData.name);
        int defenseDuplicationFile=2;

        while(file.exists())
            file=new File(parentFile,zipFileData.name+Integer.toString(defenseDuplicationFile++));
        if(zipFileData.isDirection){
            file.mkdir();
            if(zipFileData.children!=null)
                for(ZipFileData child:zipFileData.children)
                    uncompress(child,file,callbackWrapper);
        }
        else{
            file.createNewFile();
            OutputStream fOut=new FileOutputStream(file);
            ZipEntry zipEntry=mZipFile.getEntry(zipFileData.getPath());
            InputStream in=mZipFile.getInputStream(zipEntry);
            byte[] data=new byte[4096];
            int len;

            while((len=in.read(data))!=-1)
                fOut.write(data,0,len);
            in.close();
            fOut.close();
            if(callbackWrapper!=null){
                callbackWrapper.callback.onUncompressing(callbackWrapper.fileCount,callbackWrapper.uncompressFileCount-=1,
                        callbackWrapper.countByte,callbackWrapper.uncompressedByte-=zipEntry.getSize());
            }
        }
    }

    /**
     * 解压压缩包内文件到指定位置
     * @param data
     * @param destFile
     * @throws IOException
     */
    private void uncompressItems(ZipFileData data,File destFile,CallbackWrapper callback) throws IOException {
        File file=new File(destFile,data.name);
        if(data.isDirection){
            file.mkdir();
            if(data.children!=null&&!data.children.isEmpty())
                for(ZipFileData zfd:data.children)
                    uncompressItems(zfd,file,callback);
        }
        else{
            file.createNewFile();
            InputStream in=mZipFile.getInputStream(mZipFile.getEntry(data.getPath()));
            OutputStream out=new FileOutputStream(file);
            byte[] bytes=new byte[4096];
            int len;

            while((len=in.read(bytes))!=-1)
                out.write(bytes,0,len);
            out.close();
            in.close();
            if(callback!=null){
                callback.uncompressFileCount++;
                callback.uncompressedByte+=data.uncompressSize;
                callback.callback.onUncompressing(callback.fileCount,callback.uncompressFileCount,callback.countByte,callback.uncompressedByte);
            }
        }
    }

    /**
     * 文件夹本身仅很小的占用,所以要计算包含文件的大小
     * @param zipFileData zip包裹条目
     */
    private void computeDirectionContainSize(ZipFileData zipFileData){
        if(!zipFileData.isDirection&&zipFileData.parent!=null){
            zipFileData.parent.uncompressSize+=zipFileData.uncompressSize;
            zipFileData.parent.compressSize+=zipFileData.compressSize;

            ZipFileData parentDirection=zipFileData.parent;
            while(parentDirection.parent!=null){
                parentDirection.parent.uncompressSize+=zipFileData.uncompressSize;
                parentDirection.parent.compressSize+=zipFileData.compressSize;
                parentDirection=parentDirection.parent;
            }
        }
    }

    /**
     * 获取文件总数
     * @param list
     * @return
     */
    private int getFileCount(List<ZipFileData> list){
        int count=0;
        if(list!=null&&!list.isEmpty()){
            for(ZipFileData zfd:list){
                if(!zfd.isDirection)
                    count++;
                else count+=getFileCount(zfd.children);
            }
        }
        return count;
    }

    /**
     * 获取字节总数（解压完会占用的大小）
     * @param list
     * @return
     */
    private long getByteCount(List<ZipFileData> list){
        int count=0;
        if(list!=null&&!list.isEmpty()) {
            for (ZipFileData zfd : list) {
                count += zfd.uncompressSize;
                count += getByteCount(zfd.children);
            }
        }
        return count;
    }

    /**
     * 解析ZipEntry数据到ZipFileData
     * @param entry zip条目
     * @return 包裹的zip条目
     */
    private ZipFileData parseZipEntry(ZipEntry entry){
        ZipFileData zfd=new ZipFileData();
        zfd.isDirection =entry.isDirectory();
        zfd.deflate =entry.getMethod()==ZipEntry.DEFLATED;
        zfd.comment=entry.getComment();
        zfd.uncompressSize=entry.getSize();
        zfd.compressSize=entry.getCompressedSize();
        zfd.lastModifiedTime=entry.getTime();
        zfd.name=entry.getName();
        String[] pathFragment=zfd.name.split(File.separator);
        if(pathFragment.length>0)
            zfd.name=pathFragment[pathFragment.length-1];
        return zfd;
    }

    /**
     * 获取某zip条目文件的文件数量和占用大小信息
     * @param zipFileData
     * @return
     */
    private SaveUtil.FileSizeWrapper getZipFileDataSize(ZipFileData zipFileData){
        SaveUtil.FileSizeWrapper wrapper=new SaveUtil.FileSizeWrapper();

        if(zipFileData.isDirection) {
            wrapper.directionCount++;
            if(zipFileData.children!=null)
                for(ZipFileData child:zipFileData.children){
                    SaveUtil.FileSizeWrapper childSizeWrapper= getZipFileDataSize(child);
                    wrapper.fileCount+=childSizeWrapper.fileCount;
                    wrapper.fileSizeCount+=childSizeWrapper.fileSizeCount;
                    wrapper.directionCount+=childSizeWrapper.directionCount;
                }
        }
        else{
            wrapper.fileSizeCount+=zipFileData.uncompressSize;
            wrapper.fileCount++;
        }
        return wrapper;
    }

    public ZipFile getZipFile() {
        return mZipFile;
    }

    public List<ZipFileData> getZipFileDataList() {
        return mZipFileDataList;
    }

    /**
     * 清理数据
     */
    public void close(){
        if(mZipFile!=null) try {
            mZipFile.close();
        } catch (IOException e) {
            //do nothing
        }
    }

    /**
     * 简单压缩
     * @param data 源数据
     * @return 压缩字节
     * @throws IOException
     */
    public static byte[] memCompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        GZIPOutputStream out=new GZIPOutputStream(baos);
        out.write(data);
        out.close();
        return baos.toByteArray();
    }

    /**
     * 简单解压
     * @param compressData 被压缩过的数据
     * @return 源数据
     * @throws IOException
     */
    public static byte[] memUncompress(byte[] compressData) throws IOException {
        ByteArrayInputStream bais=new ByteArrayInputStream(compressData);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        GZIPInputStream in=new GZIPInputStream(bais);
        byte[] data=new byte[4096];
        int len;
        while((len=in.read(data))!=-1&&!Thread.currentThread().isInterrupted())
            baos.write(data,0,len);
        in.close();
        return baos.toByteArray();
    }

    /**
     * 解压进度回调.(在每个文件解压出后回调)
     */
    public interface ProgressCallback{

        /**
         * 计算文件数量占用空间等准备步骤结束后回调
         */
        void prepared();

        /**
         * 解压进度回调
         * @param fileCount 文件总数(不包含文件夹)
         * @param uncompressFileCount 已解压的文件数
         * @param countByte 总字节数(解压完后占用的字节数)
         * @param uncompressedByte 已解压字节数
         */
        void onUncompressing(int fileCount,int uncompressFileCount,long countByte,long uncompressedByte);
    }

    /**
     * 包裹一些数据的回调
     */
    class CallbackWrapper{
        public int fileCount;
        public int uncompressFileCount;
        public long countByte;
        public long uncompressedByte;
        public ProgressCallback callback;

        public CallbackWrapper(int fileCount, long countByte,@NonNull ProgressCallback callback) {
            this.fileCount = fileCount;
            this.countByte = countByte;
            this.callback = callback;
            uncompressFileCount=0;
            uncompressedByte=0;
        }
    }
}