package com.mtdcomponents;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;

import java.io.IOException;
import java.net.URL;

public  class BitmapUtil {
    public static Drawable loadImage(String iconUri,Context mContext) {
        if (TextUtils.isEmpty(iconUri)) {
            return null;
        }
        if (isApkInDebug(mContext)) {
            try {
                return tryLoadIcon(iconUri,mContext);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Uri uri = Uri.parse(iconUri);
//            if (isLocalFile(uri)) {
//                // 本地文件
//                return loadFile(uri);
//            } else {
                return loadResource(iconUri,mContext);
//            }
        }
        return null;
    }

    private static Drawable tryLoadIcon(String iconDevUri,Context mContext) throws IOException {
        URL url = new URL(iconDevUri);
        Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
        BitmapDrawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
        Log.e("JsDevImageLoader", "bitmap drawable width：" + bitmapDrawable.getIntrinsicWidth());
        return bitmapDrawable;
    }

    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 加载手机本地目录图片
     * @param uri
     * @return
     */
//    private static Drawable loadFile(Uri uri) {
//        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
//        return new BitmapDrawable(mc.getResources(), bitmap);
//    }

    /**
     * 加载drawable目录下的图片
     * @param iconUri
     * @return
     */
    private static Drawable loadResource(String iconUri,Context mContext) {
        return ResourceDrawableIdHelper.getInstance().getResourceDrawable(mContext, iconUri);
    }

}
