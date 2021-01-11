package com.fitgreat.airfacerobot.launcher.utils;

import android.text.TextUtils;

public class MyStringUtils {

    public static String replaceAllNewline(String lineContent) {
        String afterReplaceLineContent = null;
        if (!TextUtils.isEmpty(lineContent)) {
            afterReplaceLineContent = lineContent.replaceAll("\r|\n", "");
        }
        return afterReplaceLineContent;
    }
}