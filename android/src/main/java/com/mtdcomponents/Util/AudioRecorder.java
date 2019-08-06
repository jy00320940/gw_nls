package com.mtdcomponents.Util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.mtdcomponents.gwnls.RNGwNlsConstants;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okio.ByteString;

/**
 * 录音工具类
 */
public class AudioRecorder implements Runnable {
    private static final String TAG = "AudioRecorder";

    //指定音频源 这个和MediaRecorder是相同的 MediaRecorder.AudioSource.MIC指的是麦克风
    private static final int mAudioSource = MediaRecorder.AudioSource.MIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz = 16000;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mBufferSizeInBytes;

    private File mRecordingFile;//储存AudioRecord录下来的文件
    private boolean isRecording = false; //true表示正在录音
    private AudioRecord mAudioRecord = null;
    private File mFileRoot = null;//文件目录
    //保存的音频文件名
    private String mFileName; //本来是pcm，我改成wav了
    //缓冲区中数据写入到数据，因为需要使用IO操作，因此读取数据的过程应该在子线程中执行。
    private Thread mThread;
    private DataOutputStream mDataOutputStream;
    //录音音量变化监听
    private OnVolumeChangeListener mOnVolumeChangeListener;

    private AudioRecorder() {
        initDatas();
    }

    private static class Holder {
        private static AudioRecorder singleton = new AudioRecorder();
    }

    public static AudioRecorder getInstance() {
        return Holder.singleton;
    }

    //初始化数据
    private void initDatas() {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);//计算最小缓冲区
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig,
                mAudioFormat, mBufferSizeInBytes);//创建AudioRecorder对象

        //TODO 这里最好通过下面的文件名解析出文件路径
        mFileRoot = new File(RNGwNlsConstants.AUDIO_RECORD_FILE_PATH);
        if (!mFileRoot.exists())
            mFileRoot.mkdirs();//创建文件夹
    }

    /**
     * 开始录音
     */
    public void startRecord(String fileName, OnVolumeChangeListener listener) {
        this.mFileName = fileName;
        this.mOnVolumeChangeListener = listener;
        //AudioRecord.getMinBufferSize的参数是否支持当前的硬件设备
        if (AudioRecord.ERROR_BAD_VALUE == mBufferSizeInBytes || AudioRecord.ERROR == mBufferSizeInBytes) {
            throw new RuntimeException("Unable to getMinBufferSize");
        } else {
            destroyThread();
            isRecording = true;
            if (mThread == null) {
                mThread = new Thread(this);
                mThread.start();//开启线程
            }
        }
    }

    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            isRecording = false;
            if (null != mThread && Thread.State.RUNNABLE == mThread.getState()) {
                try {
                    Thread.sleep(500);
                    mThread.interrupt();
                } catch (Exception e) {
                    mThread = null;
                }
            }
            mThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mThread = null;
        }
    }

    //停止录音
    public void stopRecord() {
        isRecording = false;
        //停止录音，回收AudioRecord对象，释放内存
        if (mAudioRecord != null) {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                mAudioRecord.stop();
            }
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
        }
    }

    private int getVolume(int r, byte[] bytes) {
        int v = 0;
        // 将 buffer 内容取出，进行平方和运算
        for (byte b : bytes) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            v += b * b;
        }
        // 平方和除以数据总长度，得到音量大小。可以获取白噪声值，然后对实际采样进行标准化。
        // 如果想利用这个数值进行操作，建议用 sendMessage 将其抛出，在 Handler 里进行处理。
        return (int) (v / (float) r);
    }

    /**
     * 根据音量获取音量等级。目前最大7级
     * @param volume
     * @return
     */
    private int getVolumeLevel(int volume){
        int levelStep = 600;
        int maxLevel = 7;
        if(volume < levelStep)
            return 1;
        else if(volume >= levelStep * maxLevel)
            return maxLevel;
        return volume / levelStep;
    }

    @Override
    public void run() {

        //标记为开始采集状态
        isRecording = true;
        //创建一个流，存放从AudioRecord读取的数据
        mRecordingFile = new File(mFileName);
        if (mRecordingFile.exists()) {//音频文件保存过了删除
            mRecordingFile.delete();
        }
        try {
            mRecordingFile.createNewFile();//创建新文件
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "创建储存音频文件出错", e);
        }

        try {
            //获取到文件的数据流
            mDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecordingFile)));
            byte[] buffer = new byte[mBufferSizeInBytes];

            //判断AudioRecord未初始化，停止录音的时候释放了，状态就为STATE_UNINITIALIZED
            if (mAudioRecord.getState() == mAudioRecord.STATE_UNINITIALIZED) {
                initDatas();
            }

            mAudioRecord.startRecording();//开始录音
            //getRecordingState获取当前AudioReroding是否正在采集数据的状态
            while (isRecording && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int position = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);
                mDataOutputStream.write(buffer, 0, buffer.length);

                //从内存获取数据
                byte[] bytes_pkg = buffer.clone();
                //音量
                int v = getVolume(position, bytes_pkg);
                Log.e(TAG, "Recording Volume: " + v);
                if(mOnVolumeChangeListener != null)
                    mOnVolumeChangeListener.onVolumeChange(getVolumeLevel(v));
            }
            mDataOutputStream.close();
        } catch (Throwable t) {
            Log.e(TAG, "Recording Failed " + t.getMessage(), t);
            stopRecord();
        }
    }

    public void destory() {
        destroyThread();
        stopRecord();
    }

    public interface OnVolumeChangeListener{
        void onVolumeChange(int level);
    }
}
