package com.mtdcomponents.gwnls;

import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechRecognizerWithRecorder;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.mtdcomponents.gwnls.worker.RNGwNlsRecongnizer;
import com.mtdcomponents.gwnls.worker.RNGwNlsRecongnizerImp;
import com.mtdcomponents.gwnls.worker.RNGwNlsRecorder;
import com.mtdcomponents.gwnls.worker.RNGwNlsRecorderImp;
import com.mtdcomponents.gwnls.worker.RNGwNlsSynthesizer;
import com.mtdcomponents.gwnls.worker.RNGwNlsSynthesizerImp;
import com.mtdcomponents.gwnls.worker.RNGwNlsTranscriber;
import com.mtdcomponents.gwnls.worker.RNGwNlsTranscriberImp;

import javax.annotation.Nonnull;

public class RNGwNls extends ReactContextBaseJavaModule implements LifecycleEventListener, RNGwNlsRecongnizer
        , RNGwNlsTranscriber, RNGwNlsRecorder, RNGwNlsSynthesizer {
    private static final String TAG = "RNGwNls";

    private ReactApplicationContext mContext;

    private NlsClient client;
    private RNGwNlsEventEmitter emitter;
    private RNGwNlsRecongnizerImp recongnizer;
    private RNGwNlsTranscriberImp transcriber;
    private RNGwNlsRecorderImp recorder;
    private RNGwNlsSynthesizerImp synthesizer;

    public RNGwNls(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        reactContext.addLifecycleEventListener(this);

        //第一步，创建client实例，client只需要创建一次，可以用它多次创建recognizer
        client = new NlsClient();
    }

    private void checkEmitter() {
        if (emitter == null)
            this.emitter = new RNGwNlsEventEmitter(mContext);
    }

    private void checkRecongnizer() {
        checkEmitter();
        if (recongnizer == null)
            recongnizer = new RNGwNlsRecongnizerImp(client, emitter);
    }

    private void checkTranscriber() {
        checkEmitter();
        if (transcriber == null)
            transcriber = new RNGwNlsTranscriberImp(client, emitter);
    }

    private void checkRecorder() {
        checkEmitter();
        if (recorder == null)
            recorder = new RNGwNlsRecorderImp(client, emitter);
    }

    private void checkSynthesizer() {
        checkEmitter();
        if (synthesizer == null)
            synthesizer = new RNGwNlsSynthesizerImp(client, emitter);
    }

    @Nonnull
    @Override
    public String getName() {
        return "RNGwNls";
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        if (recongnizer != null) {
            recongnizer.destory();
            recongnizer = null;
        }
        if (transcriber != null) {
            transcriber.destory();
            transcriber = null;
        }
        if (recorder != null) {
            recorder.destory();
            recorder = null;
        }
        if (client != null) {
            client.release();
            client = null;
        }
    }

    @ReactMethod
    @Override
    public void startRecongnizerWithToken(String token, String appKey) {
        checkRecongnizer();
        recongnizer.startRecongnizerWithToken(token, appKey);
    }

    @ReactMethod
    @Override
    public void stopRecognizer() {
        checkRecongnizer();
        recongnizer.stopRecognizer();
    }

    @ReactMethod
    @Override
    public void startTranscribeWithToken(String token, String appKey) {
        checkTranscriber();
        transcriber.startTranscribeWithToken(token, appKey);
    }

    @ReactMethod
    @Override
    public void stopTranscribe() {
        checkTranscriber();
        transcriber.stopTranscribe();
    }

    @ReactMethod
    @Override
    public void startRecordVideo(String name) {
        checkRecorder();
        recorder.startRecordVideo(name);
    }

    @ReactMethod
    @Override
    public void stopRecordVideo() {
        checkRecorder();
        recorder.stopRecordVideo();
    }

    @ReactMethod
    @Override
    public void playVideo(String name) {
        checkRecorder();
        recorder.playVideo(name);
    }

    @ReactMethod
    @Override
    public void pauseVideo() {
        checkRecorder();
        recorder.pauseVideo();
    }

    @ReactMethod
    @Override
    public void startTranscribeRecorderWithToken(String token, String appKey, String filePath) {
        checkRecorder();
        recorder.startTranscribeRecorderWithToken(token, appKey, filePath);
    }

    @ReactMethod
    @Override
    public void stopTranscribeRecorder() {
        checkRecorder();
        recorder.stopTranscribeRecorder();
    }

    //这里用同步
    @ReactMethod(isBlockingSynchronousMethod = true)
    @Override
    public String getVideoFilePath() {
        checkRecorder();
        return recorder.getVideoFilePath();
    }

    @ReactMethod
    @Override
    public void startSynthesizerWithToken(String token, String appKey, String text) {
        checkSynthesizer();
        synthesizer.startSynthesizerWithToken(token, appKey, text);
    }

    @ReactMethod
    @Override
    public void stopSynthesizer() {
        checkSynthesizer();
        synthesizer.stopSynthesizer();
    }
}
