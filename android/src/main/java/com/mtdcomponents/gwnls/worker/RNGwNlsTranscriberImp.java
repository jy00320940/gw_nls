package com.mtdcomponents.gwnls.worker;

import android.util.Log;

import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechTranscriberWithRecorder;
import com.alibaba.idst.util.SpeechTranscriberWithRecorderCallback;
import com.facebook.react.bridge.WritableMap;
import com.mtdcomponents.Util.JsonUtils;
import com.mtdcomponents.Util.RNUtils;
import com.mtdcomponents.gwnls.RNGwNlsEvent;
import com.mtdcomponents.gwnls.RNGwNlsEventEmitter;
import com.mtdcomponents.gwnls.RNGwNlsUtils;

/**
 * 语音实时识别功能的具体实现类
 */
public class RNGwNlsTranscriberImp implements RNGwNlsTranscriber, SpeechTranscriberWithRecorderCallback {
    private static final String TAG = "RNGwNlsTranscriberImp";
    private NlsClient mClient;
    private RNGwNlsEventEmitter mEmitter;
    private SpeechTranscriberWithRecorder speechTranscriber;

    public RNGwNlsTranscriberImp(NlsClient client, RNGwNlsEventEmitter emitter) {
        this.mClient = client;
        this.mEmitter = emitter;
    }

    @Override
    public void startTranscribeWithToken(String token, String appKey) {
        // 第三步，创建识别request
        speechTranscriber = mClient.createTranscriberWithRecorder(this);

        // 第四步，设置相关参数
        // Token有有效期，请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        speechTranscriber.setToken(token);
        // 请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        speechTranscriber.setAppkey(appKey);
        // 设置返回中间结果，更多参数请参考官方文档
        // 返回中间结果
        speechTranscriber.enableIntermediateResult(true);
        // 开启标点
        speechTranscriber.enablePunctuationPrediction(true);
        // 开启ITN
        speechTranscriber.enableInverseTextNormalization(true);
        // 设置静音断句长度
//        speechTranscriber.setMaxSentenceSilence(500);
        // 设置定制模型和热词
        // speechTranscriber.setCustomizationId("yourCustomizationId");
        // speechTranscriber.setVocabularyId("yourVocabularyId");
        speechTranscriber.start();
    }

    @Override
    public void stopTranscribe() {
        // 第八步，停止本次识别
        speechTranscriber.stop();
    }

    public void destory() {
        if (speechTranscriber != null)
            speechTranscriber.stop();
    }

    @Override
    public void onVoiceData(byte[] bytes, int code) {

    }

    @Override
    public void onVoiceVolume(int code) {

    }

    @Override
    public void onTranscriptionStarted(String msg, int code) {
        Log.d(TAG,"OnTranscriptionStarted " + msg + ": " + String.valueOf(code));

    }

    @Override
    public void onTranscriptionCompleted(String msg, int code) {
        Log.d(TAG,"OnTranscriptionCompleted " + msg + ": " + String.valueOf(code));
        WritableMap data = RNGwNlsUtils.buildEventData(code, RNUtils.parseMapToWritable(JsonUtils.jsonToMap(msg)));
        mEmitter.emit(RNGwNlsEvent.OnTranscriptionCompleted, data);
    }

    @Override
    public void onTranscriptionResultChanged(String msg, int code) {
        Log.d(TAG,"OnTranscriptionResultChanged " + msg + ": " + String.valueOf(code));
        WritableMap data = RNGwNlsUtils.buildEventData(code, RNUtils.parseMapToWritable(JsonUtils.jsonToMap(msg)));
        mEmitter.emit(RNGwNlsEvent.OnTranscriptionResultChanged, data);
    }

    @Override
    public void onSentenceBegin(String msg, int code) {
        Log.i(TAG, "Sentence begin");
    }

    @Override
    public void onSentenceEnd(String msg, int code) {
        Log.d(TAG,"OnSentenceEnd " + msg + ": " + String.valueOf(code));
        WritableMap data = RNGwNlsUtils.buildEventData(code, RNUtils.parseMapToWritable(JsonUtils.jsonToMap(msg)));
        mEmitter.emit(RNGwNlsEvent.OnTranscriptionSentenceEnd, data);
    }

    @Override
    public void onTaskFailed(String msg, int code) {
        mEmitter.emit(RNGwNlsEvent.OnTaskFailed, RNGwNlsUtils.buildEventData(code, msg));
    }

    @Override
    public void onChannelClosed(String msg, int code) {
        mEmitter.emit(RNGwNlsEvent.OnChannelClosed, RNGwNlsUtils.buildEventData(code, msg));
    }
}
