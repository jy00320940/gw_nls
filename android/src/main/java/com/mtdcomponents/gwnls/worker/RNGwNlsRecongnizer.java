package com.mtdcomponents.gwnls.worker;

/**
 * 一句话语音功能
 */
public interface RNGwNlsRecongnizer {
    /**
     * 开始一句话语音
     * @param token
     * @param appKey
     */
    void startRecongnizerWithToken(String token, String appKey);

    /**
     * 结束一句话语音
     */
    void stopRecognizer();
}
