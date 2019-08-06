package com.mtdcomponents.gwnls.worker;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.idst.util.NlsClient;
import com.alibaba.idst.util.SpeechTranscriber;
import com.alibaba.idst.util.SpeechTranscriberWithRecorderCallback;
import com.facebook.react.bridge.WritableMap;
import com.mtdcomponents.Util.AudioRecorder;
import com.mtdcomponents.Util.JsonUtils;
import com.mtdcomponents.Util.RNUtils;
import com.mtdcomponents.Util.WavUtils;
import com.mtdcomponents.gwnls.RNGwNlsConstants;
import com.mtdcomponents.gwnls.RNGwNlsEvent;
import com.mtdcomponents.gwnls.RNGwNlsEventEmitter;
import com.mtdcomponents.gwnls.RNGwNlsUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 录音识别功能的具体实现
 */
public class RNGwNlsRecorderImp implements RNGwNlsRecorder, SpeechTranscriberWithRecorderCallback, AudioRecorder.OnVolumeChangeListener {
    private static final String TAG = "RNGwNlsRecorderImp";
    private NlsClient mClient;
    private RNGwNlsEventEmitter mEmitter;
    private SpeechTranscriber speechTranscriber;
    private RecordTask recordTask;
    private MediaPlayer mPlayer;

    public RNGwNlsRecorderImp(NlsClient client, RNGwNlsEventEmitter emitter) {
        this.mClient = client;
        this.mEmitter = emitter;
    }

    private String getPlayFilePath(String name) {
        String playPath = RNGwNlsUtils.buildPlayFilePath(name);
        File playFile = new File(playPath);
        if(!playFile.exists())
            createPlayFile(name);
        return playPath;
    }

    private void createPlayFile(String name) {
        String savePath = RNGwNlsUtils.buildSaveFilePath(name);
        File saveFile = new File(savePath);
        if (saveFile.exists())
            WavUtils.convertPcm2Wav(savePath, RNGwNlsUtils.buildPlayFilePath(name), 16000, 1, 16);
    }

    @Override
    public void startRecordVideo(String name) {
        AudioRecorder.getInstance().startRecord(RNGwNlsUtils.buildSaveFilePath(name), this);
    }

    @Override
    public void stopRecordVideo() {
        AudioRecorder.getInstance().stopRecord();
    }

    @Override
    public void playVideo(String name) {
        pauseVideo();
        String filePath = getPlayFilePath(name);
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(fis.getFD());
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, filePath + "  播放失败", e);
        }
    }

    @Override
    public void pauseVideo() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void startTranscribeRecorderWithToken(String token, String appKey, String name) {
        // 第三步，创建识别request
        speechTranscriber = mClient.createTranscriberRequest(this);

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

        //启动录音线程
        recordTask = new RecordTask();
        recordTask.execute(RNGwNlsUtils.buildSaveFilePath(name));
    }

    @Override
    public void stopTranscribeRecorder() {
        recordTask.stop();
        speechTranscriber.stop();
    }

    @Override
    public String getVideoFilePath() {
        return RNGwNlsConstants.AUDIO_RECORD_FILE_PATH;
    }

    public void destory() {
        if (speechTranscriber != null) {
            speechTranscriber.stop();
        }
        if (mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        AudioRecorder.getInstance().destory();
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
        Log.d(TAG, "onRecognizedCompleted " + msg + ": " + String.valueOf(code));
        WritableMap data = RNGwNlsUtils.buildEventData(code, RNUtils.parseMapToWritable(JsonUtils.jsonToMap(msg)));
        mEmitter.emit(RNGwNlsEvent.OnRecorderCompleted, data);
    }

    @Override
    public void onTranscriptionResultChanged(String msg, int code) {
        Log.d(TAG,"OnTranscriptionResultChanged " + msg + ": " + String.valueOf(code));
        WritableMap data = RNGwNlsUtils.buildEventData(code, RNUtils.parseMapToWritable(JsonUtils.jsonToMap(msg)));
        mEmitter.emit(RNGwNlsEvent.OnRecorderResultChanged, data);
    }

    @Override
    public void onSentenceBegin(String msg, int code) {
        Log.i(TAG, "Sentence begin");
    }

    @Override
    public void onSentenceEnd(String msg, int code) {
        Log.d(TAG,"OnSentenceEnd " + msg + ": " + String.valueOf(code));
        WritableMap data = RNGwNlsUtils.buildEventData(code, RNUtils.parseMapToWritable(JsonUtils.jsonToMap(msg)));
        mEmitter.emit(RNGwNlsEvent.OnRecorderSentenceEnd, data);
    }

    @Override
    public void onTaskFailed(String msg, int code) {
        mEmitter.emit(RNGwNlsEvent.OnTaskFailed, RNGwNlsUtils.buildEventData(code, msg));
    }

    @Override
    public void onChannelClosed(String msg, int code) {
        mEmitter.emit(RNGwNlsEvent.OnChannelClosed, RNGwNlsUtils.buildEventData(code, msg));
    }

    @Override
    public void onVolumeChange(int level) {
        mEmitter.emit(RNGwNlsEvent.OnRecorderVolumeChanged, level);
    }

    class RecordTask extends AsyncTask<String, Integer, Void> {

        final static int SAMPLES_PER_FRAME = 640;
        private boolean readRecord = true;

        void stop() {
            readRecord = false;
        }

        @Override
        protected Void doInBackground(String... params) {
            String filePath = params[0];
            if (TextUtils.isEmpty(filePath)) {
                Log.e(TAG, "RecordTask params is null");
                return null;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                Log.e(TAG, filePath + " is not exists");
                return null;
            }

            try {
                Thread.sleep(500);

                InputStream input = new FileInputStream(file);
                byte[] bytes = new byte[SAMPLES_PER_FRAME];
                while (readRecord && input.read(bytes, 0, SAMPLES_PER_FRAME) != -1) {
                    Log.d(TAG, String.valueOf(bytes));
                    int code = speechTranscriber.sendAudio(bytes, bytes.length);
                    if (code < 0) {
                        Log.i(TAG, "Failed to send audio!");
                        speechTranscriber.stop();
                        break;
                    }
                    Log.d(TAG, "Send audio data length: " + bytes.length);

                    Thread.sleep(10);
                }

                speechTranscriber.stop();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
