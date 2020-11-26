package com.fitgreat.airfacerobot.launcher.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import java.util.Locale;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;

public class LanguageUtil {
    /**
     * 修改设备语言
     *
     * @param context 上下文
     * @return
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static void changeAppLanguage(Context context) {
        Locale locale=null;
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "null");
        switch (currentLanguage){
            case "null":
                locale=Resources.getSystem().getConfiguration().locale;
                break;
            case "zh":
                locale=Locale.CHINESE;
                break;
            case "en":
                locale=Locale.UK;
                break;
        }
        LogUtils.d("changeAppLanguage","当前语言：" + currentLanguage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, dm);
    }
}
