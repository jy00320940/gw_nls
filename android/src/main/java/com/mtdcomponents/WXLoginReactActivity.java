package com.mtdcomponents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mtdcomponents.Util.Constants;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.List;

import javax.annotation.Nonnull;


public class WXLoginReactActivity extends ReactContextBaseJavaModule   {
    public static ReactApplicationContext mContext;
    private Promise promiseToReactNative;
    public WXLoginReactActivity(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }
    @Nonnull
    @Override
    public String getName() {
        return "SocietyManager";
    }


    @ReactMethod
    public void isAppInstalled(int platform,Promise promise){
        this.promiseToReactNative = promise;
        if (!api.isWXAppInstalled()) {
            promise.reject("","您还未安装微信客户端");
        }else{
            promise.resolve(true);
        }
    }
    @ReactMethod
    public void registerApp(String appid,int platform,Promise promise){
        this.promiseToReactNative = promise;
        Constants.APP_ID = appid;
        regToWx(promise);
    }
    @ReactMethod
    public void sendAuthRequest(int platform,Promise promise){
//        this.promiseToReactNative = promise;
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "MTDC_wx_login";
        boolean  isSuccess = api.sendReq(req);
        if(isSuccess){
            promise.resolve(isSuccess);
        }else{
            promise.reject("","调取认证失败");
        }

        PayLoop.getIntstance().add(new ICallback() {
            @Override
            public void OnSuccess( SendAuth.Resp resp) {
                //定义发送事件的函数
                WritableMap map = Arguments.createMap();
                map.putInt("errCode", resp.errCode);
                map.putString("errStr", resp.errStr);
                map.putString("lang", resp.lang);
                map.putString("type", "SendAuth.Resp");
                map.putString("code", resp.code);
                map.putString("country", resp.country);
                map.putString("platform", "0");
                String eventName = "SocietyLogin_Resp";
                mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, map);
            }
            @Override
            public void OnFailure() {

            }
        });
    }

    /**
     * Native调用RN
     * @param msg
     */
    @ReactMethod
    public void SocietyManagerClick(String msg) {
        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onclick",msg);
    }
    public static boolean isWeixinInstalled(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }
    // APP_ID 替换为你的应用从官方网站申请到的合法appID

    // IWXAPI 是第三方app和微信通信的openApi接口
    public IWXAPI api;
    private void regToWx(Promise promise) {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(mContext,   Constants.APP_ID, false);

        // 将应用的appId注册到微信
        boolean isSuccess = api.registerApp(  Constants.APP_ID);
        //建议动态监听微信启动广播进行注册到微信
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // 将该app注册到微信
                api.registerApp(  Constants.APP_ID);
            }
        }, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));
        if (isSuccess){
            promise.resolve(isSuccess);
    }else{
        promise.reject(""," 注册失败");
    }}

}
