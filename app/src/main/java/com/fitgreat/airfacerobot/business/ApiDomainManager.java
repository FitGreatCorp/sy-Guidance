package com.fitgreat.airfacerobot.business;

import android.text.TextUtils;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.archmvp.base.util.SpUtils;

/**
 * APP接口域名管理<p>
 *
 * @author zixuefei
 * @since 2019/10/23 17:42
 */
public class ApiDomainManager {
    public static final String ENVIRONMENT_CONFIG_KEY = "environment";
    /**
     * 默认接口域名环境
     */
    private final static ServerEnvironment DEFAULT_ENVIRONMENT = TextUtils.equals(SpUtils.getString(MyApp.getContext(), ENVIRONMENT_CONFIG_KEY, "debug"), "debug") ?
            ServerEnvironment.Test : ServerEnvironment.Product;

    public enum ServerEnvironment {
        Dev, Test, Uat, Product
    }

    private static ServerEnvironment getServerApiEnvironment() {
        return DEFAULT_ENVIRONMENT;
    }

    /**
     * 获取机器人认证接口域名
     */
    public static String getRobotDomain() {
        String userDomain = null;
        switch (getServerApiEnvironment()) {
            case Dev:
                userDomain = ApiDomain.URL_DEV.APP_SIGNAL_DOMAIN;
                break;
            case Test:
                userDomain = ApiDomain.URL_TEST.APP_SIGNAL_DOMAIN;
                break;
            case Uat:
                userDomain = ApiDomain.URL_UAT.APP_SIGNAL_DOMAIN;
                break;
            case Product:
            default:
                userDomain = ApiDomain.URL_PRODUCT.APP_SIGNAL_DOMAIN;
                break;
        }
        return userDomain;
    }


    /**
     * 获取fitgreat接口域名
     */
    public static String getFitgreatDomain() {
        String userDomain = null;
        switch (getServerApiEnvironment()) {
            case Dev:
                userDomain = ApiDomain.URL_DEV.APP_FITGREAT_DOMAIN;
                break;
            case Test:
                userDomain = ApiDomain.URL_TEST.APP_FITGREAT_DOMAIN;
                break;
            case Uat:
                userDomain = ApiDomain.URL_UAT.APP_FITGREAT_DOMAIN;
                break;
            case Product:
            default:
                userDomain = ApiDomain.URL_PRODUCT.APP_FITGREAT_DOMAIN;
                break;
        }
        return userDomain;
    }
}
