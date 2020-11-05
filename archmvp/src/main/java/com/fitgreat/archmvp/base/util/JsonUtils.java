package com.fitgreat.archmvp.base.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Json数据解析类<p>
 *
 * @author zixuefei
 * @since 2019/4/4 14:38
 */
public class JsonUtils {

    public static boolean isJsonFormat(String json) {
        return !TextUtils.isEmpty(json) && (json.startsWith("{") || json.startsWith("[")
                && (json.endsWith("]") || json.endsWith("}")));
    }

    public static String encode(Object json) {
        return new Gson().toJson(json);
    }

    public static String encode(Object json, boolean isPretty) {
        return (isPretty ? new GsonBuilder().setPrettyPrinting().create() : new Gson()).toJson(json);
    }

    public static Object decode(String json) {
        return new JsonParser().parse(json);
    }

    public static <T> T decode(String json, Type typeOfT) {
        return new Gson().fromJson(json, typeOfT);
    }

    public static <T> T decode(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    public static <T> ArrayList<T> jsonToArrayList(String json, Class<T> clazz) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();

        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        ArrayList<T> arrayList = new ArrayList<>();

        for (JsonObject jsonObject : jsonObjects) {
            arrayList.add(new Gson().fromJson(jsonObject, clazz));
        }
        return arrayList;
    }
}
