package com.mtdcomponents.gwnls;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public class RNGwNlsUtils {

    public static WritableMap buildEventData(int code, String msg) {
        WritableMap result = Arguments.createMap();
        result.putInt("statusCode", code);
        result.putString("eMsg", msg);
        return result;
    }

    public static WritableMap buildEventData(int code, WritableMap msg) {
        WritableMap result = Arguments.createMap();
        result.putInt("statusCode", code);
        result.putMap("eMsg", msg);
        return result;
    }

    public static String buildPlayFilePath(String name){
        return String.format("%s/%s%s", RNGwNlsConstants.AUDIO_RECORD_FILE_PATH, name, RNGwNlsConstants.AUDIO_RECORD_PLAY_SUFFIX);
    }

    public static String buildSaveFilePath(String name){
        return String.format("%s/%s%s", RNGwNlsConstants.AUDIO_RECORD_FILE_PATH, name, RNGwNlsConstants.AUDIO_RECORD_SAVE_SUFFIX);
    }
}
