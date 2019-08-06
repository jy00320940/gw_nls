package com.mtdcomponents.gwnls.worker;

import android.util.Log;

import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechRecognizerWithRecorder;
import com.alibaba.idst.util.SpeechRecognizerWithRecorderCallback;
import com.facebook.react.bridge.WritableMap;
import com.mtdcomponents.Util.JsonUtils;
import com.mtdcomponents.Util.RNUtils;
import com.mtdcomponents.gwnls.RNGwNlsEvent;
import com.mtdcomponents.gwnls.RNGwNlsEventEmitter;
import com.mtdcomponents.gwnls.RNGwNlsUtils;

/**
 * 一句话语音功能具体实现类
 */
public class RNGwNlsRecongnizerImp implements RNGwNlsRecongnizer, SpeechRecognizerWithRecorderCallback {
    private static final String TAG = "RNGwNlsRecongnizerImp";
    private NlsClient mClient;
    private RNGwNlsEventEmitter mEmitter;
    private SpeechRecognizerWithRecorder speechRecognizer;

    public RNGwNlsRecongnizerImp(NlsClient client, RNGwNlsEventEmitter emitter) {
        this.mClient = client;
        this.mEmitter = emitter;
    }

    @Override
    public void startRecongnizerWithToken(String token, String appKey) {
        // 第三步，创建识别request
        speechRecognizer = mClient.createRecognizerWithRecorder(this);

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

    @Override
    public void stopRecognizer() {
        // 第八步，停止本次识别
        speechRecognizer.stop();
    }

    public void destory(){
        if (speechRecognizer != null){
            speechRecognizer.stop();
        }
    }

    @Override
    public void onVoiceData(byte[] bytes, int code) {

    }

    @Override
    public void onVoiceVolume(int code) {

    }

    @Override
    public void onTaskFailed(String msg, int code) {
        mEmitter.emit(RNGwNlsEvent.OnTaskFailed, RNGwNlsUtils.buildEventData(code, msg));
    }

    @Override
    public void onRecognizedStarted(String msg, int code) {
        Log.d(TAG, "OnRecognizedStarted " + msg + ": " + String.valueOf(code));
    }

    @Override
    public void onRecognizedCompleted(String msg, int code) {
        Log.d(TAG,"onRecognizedCompleted " + msg + ": " + String.valueOf(code));
        WritableMap data = RNGwNlsUtils.buildEventData(code, RNUtils.parseMapToWritable(JsonUtils.jsonToMap(msg)));
        mEmitter.emit(RNGwNlsEvent.OnRecognizedCompleted, data);
    }

    @Override
    public void onRecognizedResultChanged(String msg, int code) {

    }

    @Override
    public void onChannelClosed(String msg, int code) {
        mEmitter.emit(RNGwNlsEvent.OnChannelClosed, RNGwNlsUtils.buildEventData(code, msg));
    }
}
