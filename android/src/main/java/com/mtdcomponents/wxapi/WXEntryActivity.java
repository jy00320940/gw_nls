package com.mtdcomponents.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mtdcomponents.PayLoop;
import com.mtdcomponents.Util.Constants;
import com.mtdcomponents.WXLoginReactActivity;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
/**
   * 微信登录页面
   * @author kevin_chen 2016-12-10 下午19:03:45
   * @version v1.0
   */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
   private IWXAPI mWeixinAPI;
   private static String uuid;

          @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mWeixinAPI = WXAPIFactory.createWXAPI(this,   Constants.APP_ID, true);
      mWeixinAPI.handleIntent(this.getIntent(), this);
   }
   
          @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      setIntent(intent);
      mWeixinAPI.handleIntent(intent, this);//必须调用此句话
   }
   
          //微信发送的请求将回调到onReq方法
          @Override
   public void onReq(BaseReq req) {
   }
   
          //发送到微信请求的响应结果
          @Override
   public void onResp(BaseResp resp) {
      switch (resp.errCode) {
      case BaseResp.ErrCode.ERR_OK:
          Log.d("TAG","ERR_OK");
        //发送成功
        SendAuth.Resp sendResp = (SendAuth.Resp) resp;
        if (sendResp != null) {

            PayLoop.getIntstance().OnSuccess(sendResp);
        }
        break;
      case BaseResp.ErrCode.ERR_USER_CANCEL:
          Log.d("TAG","ERR_USER_CANCEL");
        //发送取消
        break;
      case BaseResp.ErrCode.ERR_AUTH_DENIED:
          Log.d("TAG","ERR_AUTH_DENIED");
        //发送被拒绝
        break;
      default:
        //发送返回
        break;
      }
   }
}