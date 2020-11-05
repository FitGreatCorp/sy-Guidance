package com.fitgreat.archmvp.base.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 代码描述<p>
 *
 * @author anhuiqing
 * @since 2019/4/1 13:24
 */
public class SpUtils {
    private static final String FILE_NAME = "config_data";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        return editor;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getInt(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getBoolean(key, defaultValue);
    }

    public static void putFloat(Context context, String key, float value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putFloat(key, value);
        editor.apply();
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getFloat(key, defaultValue);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getLong(key, defaultValue);
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(key);
        editor.apply();
    }

    /**
     * 清除所有数据
     *
     * @param
     */
    public static void clear(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.clear();
        editor.apply();
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param
     * @return
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getAll();
    }

    public static Set<String> getStringSet(Context context, String key) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getStringSet(key, new HashSet<>());
    }

    public static void putStringSet(Context context, String key, Set<String> value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putStringSet(key, value);
        editor.apply();
    }

}