package com.fastlib.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import com.fastlib.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUtil{
    public static final String TAG=ImageUtil.class.getSimpleName();
    public static final int REQUEST_FROM_ALBUM=10000;
    public static final int REQUEST_FROM_CAMERA=10001;
    public static final int REQUEST_FROM_CROP=10002;
    public static final String KEY_LAST_IMAGE="lastImage";
    private static Uri mLastUri;

    private ImageUtil(){
        //不实例化
    }

    private static void saveLastImage(Context context,Uri uri){
        SharedPreferences.Editor edit=context.getSharedPreferences(TAG,Context.MODE_PRIVATE).edit();
        mLastUri=uri;
        edit.putString(KEY_LAST_IMAGE, uri.toString());
        edit.apply();
    }

    /**
     * 生成缩略图
     * @param limit 最大限制
     * @param quality 质量
     * @param path 原图路径
     * @param parent 压缩图像存储的父路径
     * @return 图像文件（压缩前后更小的那个图像文件）
     * @throws IOException
     */
    public static File getThumbImageFile(int limit,int quality,String path,String parent)throws IOException{
        return getThumbImageFile(false,true,limit,quality,path,parent, Bitmap.CompressFormat.JPEG);
    }

    /**
     * 图像缩略
     * @param deleteBigger 是否删除占空间大的图像
     * @param resultSmaller 是否返回占更小的图像
     * @param limit 限制最大宽高
     * @param quality 图像质量
     * @param path 源图像路径
     * @param parent 压缩图像存储父路径
     * @param format 存储压缩文件的格式
     * @return 图像文件（指定压缩后或是更小的图像文件）
     * @throws IOException
     */
    public static File getThumbImageFile(boolean deleteBigger, boolean resultSmaller, int limit, int quality, String path, String parent,Bitmap.CompressFormat format)throws IOException{
        File f=new File(path);

        if(checkImageBigerLimit(f,limit)&&quality>75) return f;
        f.setExecutable(true); //7.0文件安全权限
        if(f.exists())
            if(BuildConfig.isShowLog)
                System.out.println("压缩前:"+f.length());
            else
            if(BuildConfig.isShowLog)
                System.out.println("file not exists");
        String suffix;
        if(Bitmap.CompressFormat.JPEG==format)
            suffix=".jpg";
        else if(Bitmap.CompressFormat.PNG==format)
            suffix=".png";
        else suffix=".webp";
        File smallerFile,biggerFile;
        Bitmap bitmap=getThumbBitmap(path,limit);
        File file=new File(parent,"compress"+f.getName()+suffix);
        if(!file.exists()) f.createNewFile();
        FileOutputStream fos =new FileOutputStream(file);
        bitmap.compress(format, quality,fos);
        if(f.length()<file.length()){
            smallerFile=f;
            biggerFile=file;
        }
        else{
            smallerFile=file;
            biggerFile=f;
        }
        if(deleteBigger)
            biggerFile.delete();
        if(BuildConfig.isShowLog)
            System.out.println("压缩后:"+file.length());
        bitmap.recycle();
        fos.close();
        if(resultSmaller)
            return smallerFile;
        return file;
    }

    /**
     * 检测图像是否大于限制值
     * @param file 图像文件
     * @param limit 限制值
     * @return true大于限制值，false不大于
     */
    private static boolean checkImageBigerLimit(File file,int limit){
        BitmapFactory.Options options=new BitmapFactory.Options();

        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(file.getAbsolutePath(),options);
        return options.outWidth<limit&&options.outHeight<limit;
    }

    /**
     * 取得缩放后的图像，默认低质量
     * @param path 路径
     * @param limit 限制大小
     * @return
     */
    public static Bitmap getThumbBitmap(String path,int limit){
        return getThumbBitmap(path, limit, false);
    }

    /**
     * 取得缩放后的图像 开启renderscriptTargetApi 15 renderscriptSupportModeEnabled true
     * @param path 路径
     * @param limit 限制大小
     * @param highQuality 质量
     * @return
     */
    public static Bitmap getThumbBitmap(String path,int limit,boolean highQuality){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(path,options);
        int max=Math.max(options.outHeight,options.outWidth);

        options.inJustDecodeBounds=false;
        if(max>limit)
            options.inSampleSize = max / limit+1;
        if(!highQuality)
            options.inPreferredConfig= Bitmap.Config.ARGB_4444;
        return BitmapFactory.decodeFile(path,options);
    }

    /**
     * 写图片文件 文件保存在 /data/data/PACKAGE_NAME/files 目录下
     *
     * @throws IOException
     */
    public static void saveImage(Context context, String fileName, Bitmap bitmap) throws IOException {
        saveImage(context, fileName, bitmap, 100);
    }

    public static void saveImage(Context context, String fileName, Bitmap bitmap, int quality) throws IOException {
        if (bitmap == null || fileName == null || context == null)
            return;

        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
        byte[] bytes = stream.toByteArray();
        fos.write(bytes);
        fos.close();
    }

    /**
     * Bitmap转Drawable
     * @param bitmap
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap bitmap){
        Drawable drawable=new BitmapDrawable(bitmap);
        return drawable;
    }

    /**
     * Drawable转Bitmat
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap;
    }

    public static void openAlbum(Fragment fragment){
        openAlbum(null, fragment, false);
    }

    /**
     * 预览相册
     * @param activity
     */
    public static void openAlbum(Activity activity){
        openAlbum(activity, null, false);
    }

    @TargetApi(18)
    public static void openAlbum(Activity activity,Fragment fragment,boolean multiChoose){
        Intent intent = new Intent();
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
//            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//        else
//            intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.CATEGORY_OPENABLE, true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiChoose);
        if(activity!=null) activity.startActivityForResult(intent, REQUEST_FROM_ALBUM);
        else fragment.startActivityForResult(intent,REQUEST_FROM_ALBUM);
    }

    /**
     * 开启相机，不指定生成照片位置，默认为存储卡根部
     * @param fragment
     */
    public static void openCamera(Fragment fragment){
        openCamera(fragment, Uri.fromFile(getTempFile(null)));
    }

    /**
     * 开启相机，不指定生成照片位置，默认为存储卡根部
     * @param activity
     */
    public static void openCamera(Activity activity){
        openCamera(activity, Uri.fromFile(getTempFile(null))); //默认写在存储卡内
    }

    public static void openCamera(Activity activity,Uri outPut){
        openCamera(activity,null,outPut);
    }

    public static void openCamera(Fragment fragment,Uri outPut){
        openCamera(null,fragment,outPut);
    }

    /**
     * 指定照片存储位置启动相机
     * @param activity
     * @param outPut
     */
    private static void openCamera(Activity activity,Fragment fragment,Uri outPut){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //如果version大于22,包装uri
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1){
            ContentValues cv=new ContentValues(1);
            Context context=activity==null?fragment.getContext():activity;
            cv.put(MediaStore.Images.Media.DATA,outPut.getPath());
            outPut=context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT,outPut);
        if(activity==null){
            saveLastImage(fragment.getContext(),outPut);
            fragment.startActivityForResult(intent,REQUEST_FROM_CAMERA);
        }
        else{
            saveLastImage(activity,outPut);
            activity.startActivityForResult(intent,REQUEST_FROM_CAMERA);
        }
    }

    /**
     * 裁剪图片
     * @param activity
     * @param data
     * @param crop
     */
    public static void startActionCrop(Activity activity,Uri data,int crop){
        startActionCrop(activity, data, crop,Uri.fromFile(getTempFile(null)));
    }

    /**
     * 裁剪图片
     * @param activity
     * @param data
     * @param crop
     * @param outPut
     */
    public static void startActionCrop(Activity activity, Uri data, int crop, Uri outPut){
        Intent intent = new Intent("com.android.camera.action.CROP");
        saveLastImage(activity,outPut);
        intent.setDataAndType(data, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,outPut);
        intent.putExtra("circleCrop",false);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", crop);// 输出图片大小
        intent.putExtra("outputY", crop);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        activity.startActivityForResult(intent,REQUEST_FROM_CROP);
    }

    /**
     * 创建可指定父级的临时文件
     * @param parent 父级目录
     */
    public static File getTempFile(@Nullable File parent){
        return getTempFile(null,null,parent);
    }

    /**
     * 创建可指定父级和前缀的临时文件
     * @param prefix 前缀
     * @param suffix 后缀
     * @param parent 父级目录
     * @return
     */
    public static File getTempFile(@Nullable String prefix,@Nullable String suffix,@Nullable File parent){
        File file=null;

        if(prefix==null) prefix="";
        if(suffix==null) suffix="";
        if(parent!=null&&parent.exists())
            file=new File(parent,prefix+System.currentTimeMillis()+suffix);
        else{
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                file=new File(Environment.getExternalStorageDirectory(),prefix+System.currentTimeMillis()+suffix);
        }
        if(file!=null)
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        return file;
    }

    /**
     * 获取来自某种动作的照片uri.如果授权失败或者没有选择任何照片，返回null
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    public static Uri getImageFromActive(Context context,int requestCode, int resultCode, Intent data){
        if(resultCode!=Activity.RESULT_OK)
            return null;
        switch (requestCode){
            case REQUEST_FROM_CROP:
            case REQUEST_FROM_CAMERA:
                SharedPreferences sp=context.getSharedPreferences(TAG,Context.MODE_PRIVATE);
                return mLastUri==null?Uri.parse(sp.getString(KEY_LAST_IMAGE,"")):mLastUri;
            case REQUEST_FROM_ALBUM:
                return data.getData();
            default:
                return null;
        }
    }

    public static String getVideoPath(Context context,Uri uri){
        return getImageVideoPathForNewSdk(context,uri,false);
    }

    /**
     * 获取图片路径
     * @param context
     * @param uri
     * @return
     */
    public static String getImagePath(Context context,Uri uri){
        if(uri==null)
            return null;
        boolean isKitKat=Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT;

        if(isKitKat) return getImageVideoPathForNewSdk(context,uri,true);
        else return getImagePathForOldSdk(context,uri);
    }

    /**
     * 获取图片路径(老sdk)
     *
     * @param context
     * @param uri
     */
    private static String getImagePathForOldSdk(Context context,Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String ImagePath = cursor.getString(columnIndex);
            cursor.close();
            return ImagePath;
        }

        return uri.getPath();
    }

    /**
     * 获取图片路径（新sdk）
     * @param uri
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getImageVideoPathForNewSdk(Context context, Uri uri, boolean isImage){
        String projectField=isImage?MediaStore.Images.Media.DATA:MediaStore.Video.Media.DATA;
        if(DocumentsContract.isDocumentUri(context,uri)){
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            Uri contentUri = isImage?MediaStore.Images.Media.EXTERNAL_CONTENT_URI:MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            final String selection = "_id=?";
            final String type = split[0];
            final String[] selectionArgs = new String[] {split[1]};
            Cursor cursor = null;

            if ("primary".equalsIgnoreCase(type))//小米兼容方案
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            try {
                cursor = context.getContentResolver().query(contentUri,new String[]{projectField}, selection, selectionArgs,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(projectField);
                    return cursor.getString(index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }else if(uri.toString().startsWith("content://")){
            Cursor cursor=context.getContentResolver().query(uri,new String[]{projectField},null,null,null);
            int indexColumn=cursor.getColumnIndexOrThrow(projectField);
            cursor.moveToFirst();
            String path=cursor.getString(indexColumn);
            cursor.close();
            return path;
        }
        else{
            String scheme=uri.getScheme();
            if(scheme.equals("file"))
                return uri.getPath();
        }
        return null;
    }

    /**
     * 高斯模糊，默认值25
     * @param context
     * @param raw
     * @return
     */
    public static Bitmap blurBitmap(Context context,Bitmap raw){
        return blurBitmap(context,raw,25);
    }
    /**
     * 高斯模糊
     * @param context
     * @param raw
     * @param radius
     * @return
     */
    public static Bitmap blurBitmap(Context context,Bitmap raw,int radius){
        RenderScript rs= RenderScript.create(context);
        Bitmap bitmap=Bitmap.createBitmap(raw.getWidth(), raw.getHeight(), Bitmap.Config.ARGB_8888);
        Allocation allocIn=Allocation.createFromBitmap(rs, raw);
        Allocation allocOut=Allocation.createFromBitmap(rs,bitmap);
        ScriptIntrinsicBlur blur=ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        if(radius<0)
            radius=0;
        else if(radius>25)
            radius=25;
        blur.setRadius(radius);
        blur.setInput(allocIn);
        blur.forEach(allocOut);
        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    }

    /**
     * mp4文件取首帧
     * @param filePath
     * @return
     */
    public static Bitmap getVideoFirstFrame(String filePath) {
        Bitmap bitmap;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        bitmap = retriever.getFrameAtTime();
        retriever.release();
        return bitmap;
    }

    public static void saveVideoFrame(Context context,String srcFilePath,String name) throws IOException {
        Bitmap bitmap= getVideoFirstFrame(srcFilePath);
        if(bitmap!=null)
            saveImage(context,name,bitmap);
    }

    /**
     * 保存view截图到文件中,保存格式为png
     * @param v 要保存的视图
     * @param f 目标文件，可以是不存在的
     */
    public static void saveViewToFile(View v,File f){
        saveViewToFile(v,f,1);
    }

    /**
     * 保存view截图到文件中，保存格式为png
     * @param v 要保存的视图
     * @param f 目标文件，可以是不存在的
     * @param scale 缩放级别(1原图，0.5缩放一半，2放大一倍等等)
     */
    public static void saveViewToFile(View v,File f,float scale){
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        if(!f.exists())
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        Bitmap bitmap=v.getDrawingCache();
        if(bitmap!=null)
            try {
                Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*scale),(int)(bitmap.getHeight()*scale),true);
                OutputStream out=new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.PNG,100,out);
                bitmap.recycle();
                scaledBitmap.recycle();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
        if(BuildConfig.isShowLog)
            System.out.println("保存view到文件中失败");
    }
}
