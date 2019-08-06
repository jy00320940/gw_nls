package com.mtdcomponents.gwnls;

/**
 * RN的通讯事件
 */
public interface RNGwNlsEvent {
    /**
     * 通道关闭
     */
    String OnChannelClosed = "OnChannelClosed";

    /**
     * 识别失败
     */
    String OnTaskFailed = "OnTaskFailed";

    /////////////////////////////////////////////////////////////////////////////
    //
    // 一句话语音
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * 一句话语音识别完成
     */
    String OnRecognizedCompleted = "OnRecognizedCompleted";

    /////////////////////////////////////////////////////////////////////////////
    //
    // 实时语音识别
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * 识别中间结果，只有开启相关选项时才会回调
     */
    String OnTranscriptionResultChanged = "OnTranscriptionResultChanged";

    /**
     * 当前橘子识别结束，得到完整的句子文本
     */
    String OnTranscriptionSentenceEnd = "OnTranscriptionSentenceEnd";

    /**
     * 实时语音识别结束
     */
    String OnTranscriptionCompleted = "OnTranscriptionCompleted";

    /////////////////////////////////////////////////////////////////////////////
    //
    // 录音识别
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * 录音音量变化回调
     */
    String OnRecorderVolumeChanged = "OnRecorderVolumeChanged";

    /**
     * 识别中间结果，只有开启相关选项时才会回调
     */
    String OnRecorderResultChanged = "OnRecorderResultChanged";

    /**
     * 当前句子识别结束，得到完整的句子文本
     */
    String OnRecorderSentenceEnd = "OnRecorderSentenceEnd";

    /**
     * 录音识别结束
     */
    String OnRecorderCompleted = "OnRecorderCompleted";

    /////////////////////////////////////////////////////////////////////////////
    //
    // 语音合成
    //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * 语音合成完成
     */
    String OnSynthesizerCompleted = "OnSynthesizerCompleted";
}
