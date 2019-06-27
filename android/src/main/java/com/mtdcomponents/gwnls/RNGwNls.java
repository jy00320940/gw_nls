package com.mtdcomponents.gwnls;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechRecognizerWithRecorder;
import com.alibaba.idst.util.SpeechRecognizerWithRecorderCallback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.mtdcomponents.Util.JsonUtils;
import com.mtdcomponents.Util.RNUtils;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

public class RNGwNls extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = "RNGwNls";

    public static final int MSG_SUCCESS = 100;
    public static final int MSG_FAIL = 101;
    public static final int MSG_CLOSE = 102;

    private ReactApplicationContext mContext;
    private NlsClient client;
    private SpeechRecognizerWithRecorder speechRecognizer;

    public RNGwNls(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        reactContext.addLifecycleEventListener(this);

        //第一步，创建client实例，client只需要创建一次，可以用它多次创建recognizer
        client = new NlsClient();
    }

    @Nonnull
    @Override
    public String getName() {
        return "RNGwNls";
    }

    @ReactMethod
    public void startRecognizerwithRecorderWithToken(String token, String appKey) {
        startRecognizer(token, appKey);
    }

    @ReactMethod
    public void stopRecognizerwithRecorder() {
        stopRecognizer();
    }

    // 此方法内启动录音和识别逻辑，为了代码简单便于理解，没有加防止用户重复点击的逻辑，用户应该在真实使用场景中注意
    public void startRecognizer(String token, String appKey) {

        //UI在主线程更新
        Handler handler = new MyHandler(mContext);
        // 第二步，新建识别回调类
        SpeechRecognizerWithRecorderCallback callback = new MyCallback(handler);

        // 第三步，创建识别request
        speechRecognizer = client.createRecognizerWithRecorder(callback);

        // 第四步，设置相关参数
        // Token有有效期，请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        speechRecognizer.setToken(token);
        // 请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        speechRecognizer.setAppkey(appKey);
        // 设置返回中间结果，更多参数请参考官方文档
        // 开启ITN
        speechRecognizer.enableInverseTextNormalization(true);
        // 开启标点
        speechRecognizer.enablePunctuationPrediction(false);
        // 不返回中间结果
        speechRecognizer.enableIntermediateResult(false);
        // 设置打开服务端VAD
        speechRecognizer.enableVoiceDetection(true);
        speechRecognizer.setMaxStartSilence(3000);
        speechRecognizer.setMaxEndSilence(600);
        // 设置定制模型和热词
        // speechRecognizer.setCustomizationId("yourCustomizationId");
        // speechRecognizer.setVocabularyId("yourVocabularyId");
        speechRecognizer.start();
    }

    public void stopRecognizer() {
        // 第八步，停止本次识别
        speechRecognizer.stop();
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        if (client != null) {
            client.release();
            client = null;
        }
    }

    // 语音识别回调类，得到语音识别结果后在这里处理
    // 注意不要在回调方法里执行耗时操作
    private static class MyCallback implements SpeechRecognizerWithRecorderCallback {

        private Handler handler;

        public MyCallback(Handler handler) {
            this.handler = handler;
        }

        // 识别开始
        @Override
        public void onRecognizedStarted(String msg, int code) {
            Log.d(TAG, "OnRecognizedStarted " + msg + ": " + String.valueOf(code));
        }

        // 请求失败
        @Override
        public void onTaskFailed(String msg, int code) {
            Log.d(TAG, "OnTaskFailed " + msg + ": " + String.valueOf(code));
            Message message = Message.obtain();
            message.what = MSG_FAIL;
            message.arg1 = code;
            message.obj = msg;
            handler.sendMessage(message);
        }

        // 识别返回中间结果，只有开启相关选项时才会回调
        @Override
        public void onRecognizedResultChanged(final String msg, int code) {
            Log.d(TAG, "OnRecognizedResultChanged " + msg + ": " + String.valueOf(code));
//            Message message = Message.obtain();
//            message.obj = msg;
//            handler.sendMessage(message);
        }

        // 第七步，识别结束，得到最终完整结果
        @Override
        public void onRecognizedCompleted(final String msg, int code) {
            Log.d(TAG, "OnRecognizedCompleted " + msg + ": " + String.valueOf(code));
            Message message = Message.obtain();
            message.what = MSG_SUCCESS;
            message.arg1 = code;
            message.obj = msg;
            handler.sendMessage(message);
        }

        // 请求结束，关闭连接
        @Override
        public void onChannelClosed(String msg, int code) {
            Log.d(TAG, "OnChannelClosed " + msg + ": " + String.valueOf(code));
            Message message = Message.obtain();
            message.what = MSG_CLOSE;
            message.arg1 = code;
            message.obj = msg;
            handler.sendMessage(message);
        }

        // 手机采集的语音数据的回调
        @Override
        public void onVoiceData(byte[] bytes, int i) {

        }

        // 手机采集的语音音量大小的回调
        @Override
        public void onVoiceVolume(int i) {

        }
    }


    // 根据识别结果更新界面的代码
    private static class MyHandler extends Handler {

        private final WeakReference<ReactContext> mContext;

        public MyHandler(ReactContext activity) {
            mContext = new WeakReference<ReactContext>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null) {
                Log.i(TAG, "Empty message received.");
                return;
            }

            String eMsg = (String) msg.obj;
            int statusCode = msg.arg1;
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = mContext.get().
                    getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);

            Log.i(TAG, "rawResult ：" + eMsg);
            WritableMap result = Arguments.createMap();
            result.putInt("statusCode", statusCode);

            switch (msg.what) {
                case MSG_FAIL:
                    result.putString("eMsg", eMsg);
                    emitter.emit("OnTaskFailed", result);
                    break;
                case MSG_SUCCESS:
                    result.putMap("eMsg", RNUtils.parseMapToWritable(JsonUtils.jsonToMap(eMsg)));
                    emitter.emit("OnRecognizedCompleted", result);
                    break;
                case MSG_CLOSE:
                    result.putString("eMsg", eMsg);
                    emitter.emit("OnChannelClosed", result);
                    break;
            }
//            if (!rawResult.equals("")) {
//                JSONObject jsonObject = JSONObject.parseObject(rawResult);
//                if (jsonObject.containsKey("payload")) {
//                    result = jsonObject.getJSONObject("payload").getString("result");
//                }
//            }
        }
    }
}
