package com.fitgreat.archmvp.base.okhttp;

/**
 * 接口请求头配置<p>
 *
 * @author zixuefei
 * @since 2019/7/2 16:58
 */
public class HttpHeaderConfig {
    public static class HEADER_PARAM_DEV {//开发环境
        public static final String PLATFORM_ID = "VNxH42JjQPz8tr0YYdpl+ZqMYRG3zWaPP2JMiFS5FLXvAzoTBGNrC1i/C0LfZVXJJ04JmHdjWiuS6OwrUr+jRA==";
    }

    public static class HEADER_PARAM_TEST {//测试环境
        public static final String PLATFORM_ID = "VNxH42JjQPz8tr0YYdpl+ZqMYRG3zWaPP2JMiFS5FLXvAzoTBGNrC1i/C0LfZVXJJ04JmHdjWiuS6OwrUr+jRA==";
    }

    public static class HEADER_PARAM_UAT {//预发布环境
        public static final String PLATFORM_ID = "B2oKX1eRZuh+v6jvDkvb1Ryu6iXQcRDbuB+hqo4IqHuHMpVow6YvOOWFqqnwYH9Gh+2cbH+EbXzqOJ1NOwIOUQ==";
    }

    public static class HEADER_PARAM_PRODUCT {//生产环境
        public static final String PLATFORM_ID = "B2oKX1eRZuh+v6jvDkvb1Ryu6iXQcRDbuB+hqo4IqHuHMpVow6YvOOWFqqnwYH9Gh+2cbH+EbXzqOJ1NOwIOUQ==";
    }

//    public static String getHeaderEnvGroupPlatform() {
//        String platformId = null;
//        switch (AppEnvironment.getServerApiEnvironment()) {
//            case Dev:
//                platformId = HEADER_PARAM_DEV.PLATFORM_ID;
//                break;
//            case Test:
//                platformId = HEADER_PARAM_TEST.PLATFORM_ID;
//                break;
//            case Uat:
//                platformId = HEADER_PARAM_UAT.PLATFORM_ID;
//                break;
//            case Product:
//                platformId = HEADER_PARAM_PRODUCT.PLATFORM_ID;
//                break;
//            default:
//                platformId = HEADER_PARAM_PRODUCT.PLATFORM_ID;
//                break;
//        }
//        return platformId;
//    }
}

