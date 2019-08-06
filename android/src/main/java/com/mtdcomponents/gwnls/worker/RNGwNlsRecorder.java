package com.mtdcomponents.gwnls.worker;

/**
 * 录音识别功能
 */
public interface RNGwNlsRecorder {
    /**
     * 开始录音
     * @param name 录音文件保存名称
     */
    void startRecordVideo(String name);

    /**
     * 结束录音
     */
    void stopRecordVideo();

    /**
     * 播放录音
     * @param name 录音文件保存名称
     */
    void playVideo(String name);

    /**
     * 停止播放录音
     */
    void pauseVideo();

    /**
     *  开始识别录音文件
     * @param token
     * @param appKey
     * @param filePath 录音文件路径
     */
    void startTranscribeRecorderWithToken(String token, String appKey, String filePath);

    /**
     * 结束识别录音文件
     */
    void stopTranscribeRecorder();

    /**
     * 获取录音文件存放目录
     */
    String getVideoFilePath();
}
