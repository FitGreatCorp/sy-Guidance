package com.fitgreat.airfacerobot.floatball;

import com.fitgreat.airfacerobot.BuildConfig;

/**
 * <b>Package:</b> com.zh.touchassistant <br>
 * <b>FileName:</b> Const <br>
 * <b>Create Date:</b> 2018/12/7  下午2:57 <br>
 * <b>Author:</b> zihe <br>
 * <b>Description:</b>  <br>
 */
public interface AccessibilityConstant {
    boolean isDebug = BuildConfig.DEBUG;

    interface Config {
        /**
         * SP文件名
         */
        String APP_SP_FILE_NAME = "float_window_setting";
        /**
         * 悬浮窗坐标
         */
        String KEY_FLOAT_BUTTON_X = "float_button_x";
        String KEY_FLOAT_BUTTON_Y = "float_button_y";
        String KEY_FLOAT_PANEL_X = "float_panel_x";
        String KEY_FLOAT_PANEL_Y = "float_panel_y";
        /**
         * 上一次移动时，悬浮面板是否在左边
         */
        String KEY_FLOAT_WINDOW_IS_LEFT = "float_window_is_left";
        /**
         * 自定义菜单Json数据
         */
        String KEY_CUSTOM_MENU_DATA = "custom_menu_data";
        /**
         * 是否开启悬浮窗
         */
        String KEY_ENABLE = "enabled";
        /**
         * 显示的Alpha值
         */
        float ALPHA_SHOW = 1.0f;
        /**
         * 隐藏的Alpha值
         */
        float ALPHA_HIDDEN = 0.2f;
    }

    interface Action {
        /**
         * Action操作图标位置改变
         */
        String ACTION_UPDATE_PANEL_ACTIONS = "action_update_panel_actions";
        /**
         * 前台Activity改变
         */
        String ACTION_FOREGROUND_APP_CHANGE = "action_foreground_app_change";
        /**
         * 当悬浮球打开时
         */
        String ACTION_FLOAT_BUTTON_OPEN = "action_float_button_open";
        /**
         * 当悬浮球关闭
         */
        String ACTION_FLOAT_BUTTON_CLOSE = "action_float_button_close";
        /**
         * 返回键
         */
        String ACTION_DO_BACK = "action_do_back";
        /**
         * 下拉状态栏
         */
        String ACTION_PULL_DOWN_NOTIFICATION_BAR = "action_pull_down_notification_bar";
        /**
         * 返回桌面
         */
        String ACTION_DO_GO_HOME = "action_do_go_home";
        /**
         * 打开任务
         */
        String ACTION_DO_GO_TASK = "action_do_go_task";
    }

    /**
     * Bundle数据Key
     */
    interface Extras {
        /**
         * 前台App信息
         */
        String EXTRAS_FOREGROUND_APP_DATA = "extras_foreground_app_data";
    }
}