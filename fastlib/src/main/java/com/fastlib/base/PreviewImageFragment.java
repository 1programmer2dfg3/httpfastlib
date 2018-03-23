package com.fastlib.base;

import android.graphics.Point;
import android.os.Bundle;
import android.renderscript.Float2;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.fastlib.utils.DensityUtils;
import com.fastlib.utils.ScreenUtils;
import com.fastlib.widget.PinchImageView;

import java.util.List;

/**
 * Created by sgfb on 16/3/18.
 * 预览图片Fragment.加载图像方式由使用者定义
 */
@Deprecated
public abstract class PreviewImageFragment extends Fragment{
    //缩放图片的位置
    public static final String KEY_LOCATION="LOCATION";
    //缩放图片前的大小
    public static final String KEY_SIZE="SIZE";
    public static final String KEY_REMOTE="REMOTE";

    private ViewPager mViewPager;
    private List<Object> mData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        PinchImageView iv=new PinchImageView(getContext());
        loadImage(iv,mData.get(0));
        overlayAnimation(iv);
        return iv;
    }

    /**
     * 叠加动画。当视图一边触屏幕边时变换中心点
     * @param view
     */
    private void overlayAnimation(View view){
        Bundle arg=getArguments();
        int[] location=arg.getIntArray(KEY_LOCATION);
        int[] size=arg.getIntArray(KEY_SIZE);
        Point screenSize= ScreenUtils.getScreenSize();

        //初始化位置和尺寸
        if(size==null||size.length<2)
            size=new int[]{0,0};
        if(location==null||location.length<2)
            location=new int[]{0,0};
//        if(location[0]>size[0])
        ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(size[0],size[1]);
        view.setX(location[0]);
        view.setY(location[1]);
        view.setLayoutParams(lp);
        Float2 center=new Float2((location[0]+size[0])/2,(location[1]+size[1])/2);
        float top=center.y-size[1]/2;
        float left=center.x-size[0]/2;
        float right=screenSize.x-location[0]+size[0];
        float bottom=screenSize.y-location[1]+size[1];

        Wrapper min=min(new Wrapper(true,top),new Wrapper(true,bottom),new Wrapper(false,left),new Wrapper(false,right));
        float scale=min.value2/(min.value1?size[1]:size[0]);
        float scaleX=min.value1?1:scale;
        float scaleY=min.value1?scale:1;
        ScaleAnimation anim=new ScaleAnimation(1,scaleX,1,scaleY,center.x,center.y);
        anim.setFillAfter(true);
        anim.setDuration(250);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    private void anim(final View view, final Point screenSize,Point viewSize,Point location){
        ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(viewSize.x,viewSize.y);
        view.setX(location.x);
        view.setY(location.y);
        view.setLayoutParams(lp);
        Float2 center=new Float2((location.x+viewSize.x)/2,(location.y+viewSize.y)/2);
        float top=center.y-viewSize.y/2;
        float left=center.x-viewSize.x/2;
        float right=screenSize.x-location.x+viewSize.x;
        float bottom=screenSize.y-location.y+viewSize.y;

        Wrapper min=min(new Wrapper(true,top),new Wrapper(true,bottom),new Wrapper(false,left),new Wrapper(false,right));
        float scale=min.value2/(min.value1?viewSize.y:viewSize.x);
        float scaleX=min.value1?1:scale;
        float scaleY=min.value1?scale:1;
        ScaleAnimation anim=new ScaleAnimation(1,scaleX,1,scaleY,center.x,center.y);
        anim.setFillAfter(true);
        anim.setDuration(250);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup.LayoutParams afterLp=view.getLayoutParams();
                if(view.getX()>10||view.getY()>10||afterLp.width<screenSize.x-10||afterLp.height<screenSize.y-10){
                    Float2 center=new Float2(view.getX()+afterLp.width/2,view.getY()+afterLp.height/2);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    private class Wrapper{
        boolean value1;
        float value2;

        public Wrapper(boolean v,float v2){
            value1=v;
            value2=v2;
        }
    }

    private Wrapper min(Wrapper...  arg){
        Wrapper value=arg[0];
        for(int i=0;i<arg.length;i++)
            if (value.value2 > arg[i].value2)
                value = arg[i];
        return value;
    }

    public abstract void loadImage(ImageView imageView,Object data);

    public class PreviewAdapter extends PagerAdapter{

        @Override
        public int getCount(){
            return mData==null?0:mData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            PinchImageView piv=new PinchImageView(getContext());
            loadImage(piv,mData.get(position));
            return piv;
        }
    }
}