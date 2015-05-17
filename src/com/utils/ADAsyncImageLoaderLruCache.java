package com.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

/**
 * 异步加载下载图片Lrucache方式
 * @author miaowei
 *
 */
@SuppressLint("NewApi")
public class ADAsyncImageLoaderLruCache {
	
	//LruCache 最近最少使用的存储策略
	 private LruCache<String, Bitmap> imageCache;
	//SoftReference是软引用，是为了更好的为了系统回收变量
	 //从 Android 2.3 (API Level 9)开始，垃圾回收器会更倾向于回收持有软引用或弱引用的对象，这让软引用和弱引用变得不再可靠
    //private HashMap<String, SoftReference<Drawable>> imageCache;
    private Context ctx;
    private ExecutorService executorService = Executors.newFixedThreadPool(3);    //固定三个线程来执行任务
    public ADAsyncImageLoaderLruCache(Context ctx) {
   	   //imageCache = new HashMap<String, SoftReference<Drawable>>();
    	
    	//获取系统分配给每个应用程序的最大内存，每个应用系统分配32M  
        int maxMemory = (int) Runtime.getRuntime().maxMemory(); 
        Log.i("ad","maxMemory = " + maxMemory);
        int mCacheSize = maxMemory / 8;
      //给LruCache分配1/8 4M  
       this.imageCache = new LruCache<String, Bitmap>(mCacheSize){
    	 //必须重写此方法，来测量Drawable的大小 
    	   @Override
    	protected int sizeOf(String key, Bitmap value) {
    		   
    		return  value.getRowBytes() * value.getHeight();
    	}
    	   
       };
   	   this.ctx=ctx;
    }
    /**
     * 加载图片，并请求下载
     * @param imageUrl 图片广告URL
     * @param ADid广告ID，用于生成png图片时命名
     * @param imageCallback 回调接口
     * @return
     */
    public Bitmap loadDrawable(final String imageUrl,final String ADid, final ImageCallback imageCallback){
    	if (imageCache.get(imageUrl) != null) {
        	//从缓存中获取
            Bitmap softReference = imageCache.get(imageUrl);
            if (softReference != null) {
            	
                return softReference;
            } 
        }
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback.imageLoaded((Bitmap) message.obj,imageUrl);
            }
        };
        //建立新一个新的线程下载图片
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap drawable = loadImageFromUrl(ctx, imageUrl,"ad_img_"+ADid+".png");
                if (drawable != null) {
				
                	imageCache.put(imageUrl,drawable);
                    Message message = handler.obtainMessage(0, drawable);
                    handler.sendMessage(message);
				}
                
            }
        });
        return null;
    }
       /**
        * 网络图片先下载到本地cache目录保存，以imagUrl的图片文件名保存。如果有同名文件在cache目录就从本地加载
        * @param context
        * @param imageUrl 广告URl
        * @param imgName 图片名称
        * @return
        */
 		public static Bitmap loadImageFromUrl(Context context, String imageUrl,String imgName) {
 			
 			 if(imageUrl == null ){
  	        	
  	        	return null;  
  	        }
 			Bitmap drawable = null;  
 	        String imagePath = "";  
 	        String   fileName   = "";  
 	              
 	    // 获取url中图片的文件名与后缀  
 	        if(imageUrl!=null&&imageUrl.length()!=0){   
 	            fileName  = imgName;  
 	        }  
 	          
 	    // 图片在手机本地的存放路径,注意：fileName为空的情况  
 	        imagePath = context.getCacheDir() + "/" + fileName;  
 	  
 	        Log.i("ad","icon_imagePath = " + imagePath);  
 	        File file = new File(context.getCacheDir(),fileName);// 保存文件  
 	        
 	        if(!file.exists()&&!file.isDirectory()){
 	        	Log.i("ad","icon_file.toString()=" + file.toString());  
 	            try {  
 	                // 可以在这里通过文件名来判断，是否本地有此图片   
 	                FileOutputStream fos= new FileOutputStream( file );
 	                //开始下载图片
 	                InputStream is = new URL(imageUrl).openStream();  
 	                int   data = is.read();   
 	                while(data!=-1){   
 	                        fos.write(data);   
 	                        data=is.read();;   
 	                }   
 	                fos.close();  
 	                is.close();  
 	                
 	               drawable = BitmapFactory.decodeFile(file.toString());  
// 	                Log.i(TAG, "file.exists()不文件存在，网上下载:" + drawable.toString());   
 	            } catch (IOException e) {  
 	                Log.e("ad", e.toString() + "图片下载及保存时出现异常！");  
 	            }  
 	        }else  
 	        {  
 	        	drawable = BitmapFactory.decodeFile(file.toString());
// 	            Log.i("test", "file.exists()文件存在，本地获取");   
 	        } 
 	        return drawable ;
 		}
  
    /**
     * 回调函数，用来回调显示广告图片
     * @author miaowei
     *
     */
	  public interface ImageCallback {
	         public void imageLoaded(Bitmap imageDrawable, String imageUrl);
	     }
}