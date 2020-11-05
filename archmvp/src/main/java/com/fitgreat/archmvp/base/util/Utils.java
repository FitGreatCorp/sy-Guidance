package com.fitgreat.archmvp.base.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * 通用工具类<p>
 *
 * @author zixuefei
 * @since 2019/4/4 22:38
 */

public final class Utils {
    private static final String TAG = "Utils";
    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;
    private static Pattern pattern = Pattern.compile("[0-9]*");

    public static boolean isNumeric(String str) {
        if (str == null || str.equals("")) {
            return false;
        }
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }


    public static int getNumber(String str, String tip) {
        int num;
        if (str != null) {
            String sub = str.substring(0, str.indexOf(tip));
            if (isNumeric(sub)) {
                num = Integer.parseInt(sub);
                return num;
            }
        }
        return 0;
    }

    /**
     * 校验用户昵称
     *
     * @param str
     * @return
     */
    public static boolean checkNickName(String str) {
        String regex = "^[a-zA-Z0-9\u4E00-\u9FA5]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(str);
        boolean b = match.matches();
        return b;
    }

    public static String bytesToHuman(final long value) {
        final long[] dividers = new long[]{T, G, M, K, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        if (value < 1) {
            return 0 + " " + units[units.length - 1];
        }
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#.##").format(result) + " " + unit;
    }

    /**
     * [1,2] 转成整形数组
     */
    public static int[] getIntArray(String str) {
        int[] postions = null;
        if (!TextUtils.isEmpty(str) && str.contains("[") && str.contains("]") && str.contains(",")) {
            int left = str.indexOf('[');
            int right = str.indexOf(']');
            str = str.substring(left + 1, right - left);
            String[] array = str.split(",");
            if (array != null) {
                postions = new int[array.length];
                for (int i = 0; i < array.length; i++) {
                    int num = Integer.parseInt(array[i]);
                    postions[i] = num;
                }
                LogUtils.d("str--->", postions.length + "" + postions[0] + "" + str);
            }
        }
        return postions;
    }


    /**
     * 获取广告位置 1,2
     *
     * @param str
     * @return
     */
    public static int[] getAdIntArray(String str) {
        int[] postions = null;
        if (!TextUtils.isEmpty(str) && str.contains(",")) {
            String[] array = str.split(",");
            if (array != null) {
                postions = new int[array.length];
                for (int i = 0; i < array.length; i++) {
                    int num = Integer.parseInt(array[i]);
                    postions[i] = num;
                }
                LogUtils.d("str--->", postions.length + "" + postions[0] + "" + str);
            }
        } else if (!TextUtils.isEmpty(str) && isNumeric(str)) {
            postions = new int[]{Integer.parseInt(str)};
        }

        return postions;
    }


    public static boolean isEmptyOrNull(String str) {
        return TextUtils.isEmpty(str) || TextUtils.equals(str, "null");
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(150);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
//            Log.i("zxf", "serviceInfo:" + serviceInfo.service.getClassName());
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isForeground(Activity activity) {
        return isForeground(activity, activity.getClass().getName());
    }

    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            LogUtils.d("LauncherActivity", "------top activity---" + cpn.getClassName());
            return className.equals(cpn.getClassName());
        }
        return false;
    }

    /**
     * 获取字符长度
     *
     * @param s
     * @return
     */
    public static double getLength(String s) {
        double valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < s.length(); i++) {
            // 获取一个字符
            String temp = s.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为1
                valueLength += 1;
            } else {
                // 其他字符长度为0.5
                valueLength += 0.5;
            }
        }
        //进位取整
        return Math.ceil(valueLength);
    }

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {       //500毫秒内按钮无效，这样可以控制快速点击，自己调整频率
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 获取随机页数
     *
     * @return
     */
    public static int getRandomPage() {
        Random random = new Random();
        int page = 1 + random.nextInt(49);
        return page;
    }

    public static boolean isEmailValid(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }


    /**
     * 获取格式化内容
     *
     * @param data
     * @return
     */
    public static String getFormatString(List<String> data) {
        if (data != null && data.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            for (int i = 0; i < data.size(); i++) {
                builder.append((i + 1) + ";\"" + data.get(i) + "\"");
                if (i != data.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append("}");
            return builder.toString();
        }
        return "";
    }

    /**
     * 配置曝光视频id  title
     *
     * @param ids
     * @param titles
     * @return
     */
    public static String[] getFormatStringArray(List<String> ids, List<String> titles) {
        String[] array = new String[2];
        StringBuilder idsBuilder = new StringBuilder();
        StringBuilder titlesBuilder = new StringBuilder();
        idsBuilder.append("{");
        titlesBuilder.append("{");
        for (int i = 0; i < ids.size(); i++) {
            if (i != ids.size() - 1) {
                idsBuilder.append("\"" + (i + 1) + "\":\"" + ids.get(i) + "\",");
                titlesBuilder.append("\"" + ids.get(i) + "\":\"" + titles.get(i) + "\",");
            } else {
                idsBuilder.append("\"" + (i + 1) + "\":\"" + ids.get(i) + "\"");
                titlesBuilder.append("\"" + ids.get(i) + "\":\"" + titles.get(i) + "\"");
            }
        }
        idsBuilder.append("}");
        titlesBuilder.append("}");
        array[0] = idsBuilder.toString();
        array[1] = titlesBuilder.toString();
        return array;
    }


    /**
     * 格式观看次数
     *
     * @param num
     * @return
     */
    public static String getFormatNumber(long num) {
        String str = "";
        if (num >= 10000) {
            long f1 = num / 10000;
            str = f1 + "万";
        } else {
            str = num + "";
        }
        return str;
    }

    public static boolean isChinaPhoneLegal(String str) {
        String regExp = "^[1][3,4,5,7,8][0-9]{9}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }


    /**
     * 压缩数据
     *
     * @param string
     * @return
     */
    public static String CompressToBase64(String string) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(string.getBytes());
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();


            String result = Base64.encodeToString(compressed, Base64.DEFAULT);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {

        }
        return "";
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }


    public static String getProcessName(Context cxt, int pid) {
        try {
            ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
            if (runningApps == null) {
                return null;
            }
            for (ActivityManager.RunningAppProcessInfo processInfo : runningApps) {
                if (processInfo.pid == pid) {
                    return processInfo.processName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
