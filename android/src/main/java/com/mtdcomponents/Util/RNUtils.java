package com.mtdcomponents.Util;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;
import java.util.Map;

public class RNUtils {


    /**
     * 将原生的Map转换成RN的WritableMap
     * @param map
     * @return
     */
    public static WritableMap parseMapToWritable(Map<String, Object> map) {
        WritableMap result = Arguments.createMap();
        if (map == null || map.isEmpty())
            return result;

        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value == null) {
                result.putNull(key);
            } else if (value instanceof Integer) {
                result.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                result.putDouble(key, (Double) value);
            } else if (value instanceof Boolean) {
                result.putDouble(key, (Double) value);
            } else if (value instanceof String) {
                result.putString(key, (String) value);
            } else if (value instanceof Map) {
                result.putMap(key, parseMapToWritable((Map<String, Object>) value));
            } else if (value instanceof List) {
                result.putArray(key, parseArrayToWritable((List<Object>) value));
            } else {
                result.putString(key, (String) value);
            }
        }
        return result;
    }

    /**
     * 将原生的List转换成RN的WritableArray
      * @param value
     * @return
     */
    public static WritableArray parseArrayToWritable(List<Object> value) {
        WritableArray result = Arguments.createArray();
        if (value == null || value.isEmpty())
            return result;

        for (Object elem : value) {
            if (elem == null) {
                result.pushNull();
            } else if (elem instanceof Boolean) {
                result.pushBoolean((Boolean) elem);
            } else if (elem instanceof Integer) {
                result.pushInt((Integer) elem);
            } else if (elem instanceof Double) {
                result.pushDouble((Double) elem);
            } else if (elem instanceof String) {
                result.pushString((String) elem);
            } else if (elem instanceof Map) {
                result.pushMap(parseMapToWritable((Map<String, Object>) elem));
            } else if (elem instanceof WritableArray) {
                result.pushArray((WritableArray) elem);
            } else if (elem instanceof WritableMap) {
                result.pushMap((WritableMap) elem);
            } else {
                result.pushString((String) elem);
            }
        }
        return result;
    }
}