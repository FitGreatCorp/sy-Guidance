package com.fitgreat.airfacerobot.launcher.utils;

import android.content.Context;
import android.content.Intent;

import com.fitgreat.airfacerobot.launcher.ui.activity.WebPageActivity;
import com.geek.webpage.entity.WebPageEntity;
import com.geek.webpage.utils.WebPageConstants;

/**
 * 网页跳转工具<p>
 *
 * @author zixuefei
 * @since 2020/4/1 0001 17:17
 */
public class WebPageUtils {

    public static void goToWebActivity(Context context, String title, String url, boolean isShowTitleBar, boolean isDarkFontStatus) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.title = title;
        webPageEntity.url = url;
        webPageEntity.isShowTitleBar = isShowTitleBar;
        webPageEntity.isDarkFont = isDarkFontStatus;
        startWebPageActivity(context, webPageEntity);
    }

    public static void goToWebActivity(Context context, String title, String url, boolean isShowTitleBar, boolean isDarkFontStatus, int flag) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.title = title;
        webPageEntity.url = url;
        webPageEntity.isShowTitleBar = isShowTitleBar;
        webPageEntity.isDarkFont = isDarkFontStatus;
        startWebPageActivity(context, webPageEntity, flag);
    }

    public static void startWebPageActivity(Context context, WebPageEntity webPageEntity) {
        if (context == null || webPageEntity == null) {
            return;
        }
        Intent intent = new Intent(context, WebPageActivity.class);
        intent.putExtra(WebPageConstants.WEBPAGE_ENTITY, webPageEntity);
        context.startActivity(intent);
    }

    public static void startWebPageActivity(Context context, WebPageEntity webPageEntity, int flags) {
        if (context == null || webPageEntity == null) {
            return;
        }
        Intent intent = new Intent(context, WebPageActivity.class);
        intent.addFlags(flags);
        intent.putExtra(WebPageConstants.WEBPAGE_ENTITY, webPageEntity);
        context.startActivity(intent);
    }
}
