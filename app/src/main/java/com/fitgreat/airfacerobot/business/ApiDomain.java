package com.fitgreat.airfacerobot.business;

/**
 * 接口域名配置<p>
 *
 * @author zixuefei
 * @since 2019/6/30 18:34
 */
public interface ApiDomain {

    interface URL_DEV {//开发环境
        //远程服务器域名
        String APP_FITGREAT_DOMAIN = "https://signal-dev.fitgreat.cn";
        //机器人认证服务器域名
        String APP_SIGNAL_DOMAIN = "https://login.fitgreat.cn";
    }

    interface URL_TEST {//测试环境
        //远程服务器域名
        String APP_FITGREAT_DOMAIN = "https://signal-test.fitgreat.cn";
        //机器人认证服务器域名
        String APP_SIGNAL_DOMAIN = "https://login-test.fitgreat.cn";
    }

    interface URL_UAT {//预发布环境
        //远程服务器域名
        String APP_FITGREAT_DOMAIN = "https://signal.fitgreat.cn";
        //机器人认证服务器域名
        String APP_SIGNAL_DOMAIN = "https://login.fitgreat.cn";
    }

    interface URL_PRODUCT {//生产环境
        //远程服务器域名
        String APP_FITGREAT_DOMAIN = "https://signal.fitgreat.cn";
        //机器人认证服务器域名
        String APP_SIGNAL_DOMAIN = "https://login.fitgreat.cn";
    }
}