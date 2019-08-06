package com.mtdcomponents.gwnls.worker;

import android.util.Log;

import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechSynthesizer;
import com.alibaba.idst.util.SpeechSynthesizerCallback;
import com.mtdcomponents.Util.AudioPlayer;
import com.mtdcomponents.gwnls.RNGwNlsEvent;
import com.mtdcomponents.gwnls.RNGwNlsEventEmitter;
import com.mtdcomponents.gwnls.RNGwNlsUtils;

/**
 * 语音合成功能具体实现类
 */
public class RNGwNlsSynthesizerImp implements RNGwNlsSynthesizer, SpeechSynthesizerCallback {

    private static final String TAG = "RNGwNlsSynthesizerImp";
    private NlsClient mClient;
    private RNGwNlsEventEmitter mEmitter;
    private SpeechSynthesizer speechSynthesizer;
    private AudioPlayer audioPlayer;


    public RNGwNlsSynthesizerImp(NlsClient client, RNGwNlsEventEmitter emitter) {
        this.mClient = client;
        this.mEmitter = emitter;
        this.audioPlayer = new AudioPlayer();
    }

    @Override
    public void startSynthesizerWithToken(String token, String appKey, String text) {
        speechSynthesizer = mClient.createSynthesizerRequest(this);

        // 第四步，设置token和appkey
        // Token有有效期，请使用https://help.aliyun.com/document_detail/72153.html 动态生成token
        speechSynthesizer.setToken(token);
        // 请使用阿里云语音服务管控台(https://nls-portal.console.aliyun.com/)生成您的appkey
        speechSynthesizer.setAppkey(appKey);

        // 第五步，设置相关参数，以下选项都会改变最终合成的语音效果，可以按文档调整试听效果
        // 设置人声
        speechSynthesizer.setVoice(SpeechSynthesizer.VOICE_AMEI);
        // 设置语速
        speechSynthesizer.setSpeechRate(0);
        // 设置要转为语音的文本
        speechSynthesizer.setText(text);
        // 设置语音数据采样率
        speechSynthesizer.setSampleRate(SpeechSynthesizer.SAMPLE_RATE_16K);
        // 设置语音编码，pcm编码可以直接用audioTrack播放，其他编码不行
        speechSynthesizer.setFormat(SpeechSynthesizer.FORMAT_PCM);
        // 设置音量
        // speechSynthesizer.setVolume(50);
        // 设置语速
        // speechSynthesizer.setSpeechRate(0);
        // 设置语调
        // speechSynthesizer.setPitchRate(0);

        // 第六步，开始合成
        speechSynthesizer.start();
        Log.d(TAG,"speechSynthesizer start done");
    }

    @Override
    public void stopSynthesizer() {
        if (speechSynthesizer != null)
            speechSynthesizer.cancel();
        audioPlayer.stopPlay();
    }

    public void destory() {
        audioPlayer.stop();
        if (speechSynthesizer != null) {
            speechSynthesizer.stop();
            speechSynthesizer.cancel();
        }
    }

    @Override
    public void onSynthesisStarted(String msg, int code) {
        Log.d(TAG,"OnSynthesisStarted " + msg + ": " + String.valueOf(code));
    }

    @Override
    public void onSynthesisCompleted(String msg, int code) {
        Log.d(TAG,"OnSynthesisCompleted " + msg + ": " + String.valueOf(code));
        speechSynthesizer.stop();
        mEmitter.emit(RNGwNlsEvent.OnSynthesizerCompleted, RNGwNlsUtils.buildEventData(code, msg));
    }

    @Override
    public void onBinaryReceived(byte[] data, int code) {
        Log.i(TAG, "binary received length: " + data.length);
        audioPlayer.setAudioData(data);
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
