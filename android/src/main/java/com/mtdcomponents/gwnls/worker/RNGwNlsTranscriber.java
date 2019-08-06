package com.mtdcomponents.gwnls.worker;

/**
 * 语音实时识别功能
 */
public interface RNGwNlsTranscriber {
    /**
     * 开始语音实时识别
     * @param token
     * @param appKey
     */
    void startTranscribeWithToken(String token, String appKey);

    /**
     * 结束语音实时识别
     */
    void stopTranscribe();
}
