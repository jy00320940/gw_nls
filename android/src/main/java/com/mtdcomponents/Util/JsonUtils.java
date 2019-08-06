package com.mtdcomponents.Util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    private static final String TAG = "JsonUtils";

    /**
     * 将json字符串转为Map对象
     *
     * @return
     */
    public static Map<String, Object> jsonToMap(String jsonStr) {
        if(!isJsonObject(jsonStr))
            return null;

        Map<String, Object> result = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(jsonStr);
            Iterator<String> keys = obj.keys();
            while(keys.hasNext()){
                String key = keys.next();
                Object value = obj.get(key);
                addValueToMap(result, key, value);
            }
        } catch (JSONException e) {
            Log.e(TAG, "jsonToMap Error: " + jsonStr, e);
        }
        return result;
    }

    /**
     * 将一个json数组组装成一个list
     * @param jsonStr
     * @return
     */
    public static List<Object> jsonToArray(String jsonStr){
        if(!isJsonArray(jsonStr))
            return null;

        List<Object> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(jsonStr);
            for(int i=0; i<array.length(); i++)
                addValueToArray(list, array.get(i));
        } catch (JSONException e) {
            Log.e(TAG, "arrayToMap Error: " + jsonStr, e);
        }
        return list;
    }

    private static void addValueToMap(Map<String, Object> result, String key, Object value) {
        if(null == value){
            result.put(key, null);
            return;
        }

        if(isJsonObject(value.toString())){
            Map<String, Object> temp = jsonToMap(value.toString());
            result.put(key, temp);
            return;
        }

        if(isJsonArray(value.toString())){
            List<Object> temp = jsonToArray(value.toString());
            result.put(key, temp);
            return;
        }

        result.put(key, value);
    }

    private static void addValueToArray(List<Object> array, Object value) {
        if(null == value)
            return;

        if(isJsonObject(value.toString())){
            Map<String, Object> temp = jsonToMap(value.toString());
            array.add(temp);
            return;
        }

        array.add(value);
    }

    public static boolean isJsonObject(String jsonStr) {
        if (null == jsonStr || jsonStr.length() < 2)
            return false;
        return jsonStr.startsWith("{") && jsonStr.endsWith("}");
    }

    public static boolean isJsonArray(String jsonStr){
        if (null == jsonStr || jsonStr.length() < 2)
            return false;
        return jsonStr.startsWith("[") && jsonStr.endsWith("]");
    }

    public static void test() {
        try {
            JSONObject object = new JSONObject();
            object.put("aaa", 1);
            object.put("bbb", "222");

            JSONObject object2 = new JSONObject();
            object2.put("aaa", 1);
            object2.put("bbb", "222");
            object2.put("ccc", "333");

            JSONArray array = new JSONArray();
            array.put(object);
            array.put(object2);

            JSONArray array2 = new JSONArray();
            array2.put("222");
            array2.put("hahaha");

            JSONObject result = new JSONObject();
            result.put("xxx", 888);
            result.put("object", object);
            result.put("array", array);
            result.put("array2", array2);
            result.put("emptyObject", new JSONObject());
            result.put("null", null);

            Map<String, Object> map = jsonToMap(result.toString());
            Log.d(TAG, "map : " + map);
//            Log.d(TAG, "JsonObject : " + object.toString());
//            Log.d(TAG, "isObject:" + isJsonObject(object.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
