package com.mtdcomponents.gwnls.worker;

/**
 * 语音合成功能
 */
public interface RNGwNlsSynthesizer {
    /**
     * 开始语音合成并播放
     *
     * @param token
     * @param appKey
     * @param text   要合成的文字
     */
    void startSynthesizerWithToken(String token, String appKey, String text);

    /**
     * 结束语音合成并停止播放
     */
    void stopSynthesizer();
}
