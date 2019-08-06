package com.mtdcomponents.gwnls;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNGwNlsEventEmitter extends Handler {

    private static final int RNGWNLS_EVENT = -999;
    private DeviceEventManagerModule.RCTDeviceEventEmitter mEmitter;

    public RNGwNlsEventEmitter(ReactContext context) {
        mEmitter = context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    /**
     * 发射阿里语音事件
     *
     * @param eventName
     * @param data
     */
    public void emit(String eventName, WritableMap data) {
        Log.i("RNGwNlsEventEmitter", "eventName :" + eventName + ", data ： " + data);

        if (TextUtils.isEmpty(eventName)) {
            Log.e("RNGwNlsEventEmitter", "eventName is null");
            return;
        }

        Message msg = obtainMessage();
        Object[] params = {eventName, data};
        msg.what = RNGWNLS_EVENT;
        msg.obj = params;
        sendMessage(msg);
    }

    /**
     * 发射阿里语音事件
     *
     * @param eventName
     * @param data
     */
    public void emit(String eventName, int data) {
        if (TextUtils.isEmpty(eventName)) {
            Log.e("RNGwNlsEventEmitter", "eventName is null");
            return;
        }

        Message msg = obtainMessage();
        Object[] params = {eventName, data};
        msg.what = RNGWNLS_EVENT;
        msg.obj = params;
        sendMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        // 确保是阿里语音的事件
        if (msg.what == RNGWNLS_EVENT) {
            Object[] params = (Object[]) msg.obj;
            mEmitter.emit((String) params[0], params[1]);
        }
    }
}

