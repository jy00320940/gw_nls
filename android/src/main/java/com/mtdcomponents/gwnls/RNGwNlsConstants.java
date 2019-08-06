package com.mtdcomponents.gwnls;

import android.os.Environment;

public class RNGwNlsConstants {
    /**
     * 录音文件存放的目录路径名称
     */
    public static final String AUDIO_RECORD_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudiioRecordFile";

    /**
     * 录音文件保存后缀名
     */
    public static final String AUDIO_RECORD_SAVE_SUFFIX = ".pcm";

    /**
     * 录音文件播放后缀名
     */
    public static final String AUDIO_RECORD_PLAY_SUFFIX = ".wav";
}
