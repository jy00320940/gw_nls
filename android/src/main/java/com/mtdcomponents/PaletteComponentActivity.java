package com.mtdcomponents;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;
import java.util.Map;

import static com.mtdcomponents.ColorUtil.int2Rgb;
import static com.mtdcomponents.ColorUtil.rgb2Hex;

public class PaletteComponentActivity extends ReactContextBaseJavaModule{
    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";
    private Context mContext;
    public PaletteComponentActivity( ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }


    //定义react-native调用的key值
    @Override
    public String getName() {
        return "PaletteComponent";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String,Object> map = new HashMap<>();
        map.put(DURATION_SHORT_KEY, Toast.LENGTH_LONG);
        map.put(DURATION_LONG_KEY, Toast.LENGTH_SHORT);
        return map;
    }

    //定义方法提供给React调用
    @ReactMethod
    public  void PaletteColor(ReadableMap params,  Promise promise){
        String rnImageUri;
        try {
            rnImageUri = params.getString("uri");
            Log.i("showRNImage", "uri : " + rnImageUri);
            Drawable mDrawable = BitmapUtil.loadImage(rnImageUri,mContext);
            if(mDrawable != null){
                Bitmap bitmap = ((BitmapDrawable)mDrawable).getBitmap();
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {

                        if (palette != null) {
                            Palette.Swatch vibrant = palette.getVibrantSwatch();//有活力的
                            Palette.Swatch vibrantDark = palette.getDarkVibrantSwatch();//有活力的，暗色
                            Palette.Swatch vibrantLight = palette.getLightVibrantSwatch();//有活力的，亮色
                            Palette.Swatch muted = palette.getMutedSwatch();//柔和的
                            Palette.Swatch mutedDark = palette.getDarkMutedSwatch();//柔和的，暗色
                            Palette.Swatch mutedLight = palette.getLightMutedSwatch();//柔和的,亮色
                            if(vibrant != null)
                            promise.resolve(int2Hex2(vibrant.getRgb()));
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**Color的Int整型转Color的16进制颜色值【方案二】
     * colorInt - -12590395
     * return Color的16进制颜色值——#3FE2C5
     * */
    public static String int2Hex2(int colorInt){
        String hexCode = "";
        int[] rgb = int2Rgb(colorInt);
        hexCode = rgb2Hex(rgb);
        return hexCode;
    }
}

