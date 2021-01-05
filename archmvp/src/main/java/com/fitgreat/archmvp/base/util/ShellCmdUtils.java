package com.fitgreat.archmvp.base.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 用于执行shell命令的工具类<p>
 *
 * @author zixuefei
 * @since 2020/3/17 0017 16:22
 */
public class ShellCmdUtils {
    private static final String TAG = ShellCmdUtils.class.getSimpleName();
    public final static String[] REBOOT = {"su", "-c", "reboot"};
    public final static String[] SHUTDOWN = {"su", "-c", "reboot -p"};

    public final static String[] COMMAND_AIRPLANE_ON = {"su", "-c", "settings put global airplane_mode_on 1 \n " +
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true\n "};
    public final static String[] COMMAND_AIRPLANE_OFF = {"su", "-c", "settings put global airplane_mode_on 0 \n" +
            " am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false\n "};
    /*配置虚拟网卡*/
    public final static String[] ADD_RULE = {"su", "-c", "busybox ip rule add from all table 1 pref 9000"};
    public final static String[] ADD_NETMASK = {"su", "-c", "busybox ifconfig eth0 192.168.88.9 netmask 255.255.255.0 up"};
    public final static String[] ADD_ROUTE = {"su", "-c", "busybox ip route add 192.168.88.0/24 via 192.168.88.9 dev eth0 table 1"};


    /*网卡配置结果查询*/
    public final static String[] IFCONFIG = {"su", "-c", "busybox ifconfig"};


    /**
     * 用Runtime模拟按键操作
     *
     * @param keyCode 按键事件(KeyEvent)的按键值
     */
    public static void sendKeyCode(int keyCode) {
        try {
            String keyCommand = "input keyevent " + keyCode;
            // 调用Runtime模拟按键操作
            Runtime.getRuntime().exec(keyCommand);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "exec Exception:" + e.getMessage());
        }
    }

    /**
     * busybox ifconfig eth0 192.168.88.9 netmask 255.255.255.0 up
     * busybox ip route add table 3 via 192.168.88.9 dev eth0
     * busybox ip rule add to 192.168.88.8/24 table 3
     */

    public static int execCmd(String[] cmd) {
        try {
            //("ps -P|grep bg")执行失败，PC端adb shell ps -P|grep bg执行成功
            //Process process = Runtime.getRuntime().exec("ps -P|grep tv");
            //-P 显示程序调度状态，通常是bg或fg，获取失败返回un和er
            // Process process = Runtime.getRuntime().exec("ps -P");
            //打印进程信息，不过滤任何条件
            Process process = Runtime.getRuntime().exec(cmd);
            String content = consumeInputStream(process.getInputStream());
            int result = process.waitFor();
            LogUtils.d(TAG, "exec result:" + content + " result:" + result);
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "exec IOException:" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "exec InterruptedException:" + e.getMessage());
        }
        return -1;
    }


    /**
     * 消费inputstream，并返回
     */
    public static String consumeInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer output = new StringBuffer();
        int read;
        char[] buffer = new char[4096];
        while ((read = reader.read(buffer)) > 0) {
            output.append(buffer, 0, read);
        }
        reader.close();
        return output.toString();
    }

    /**
     * OS error code 0: Success 操作系统错误代码0：成功
     * OS error code 1: Operation not permitted 操作系统错误代码1：操作不允许
     * OS error code 2: No such file or directory 操作系统错误代码2：没有这样的文件或目录
     * OS error code 3: No such process 操作系统错误代码3：没有这样的过程
     * OS error code 4: Interrupted system call 操作系统错误代码4：中断的系统调用
     * OS error code 5: Input/output error 操作系统错误代码5：输入/输出错误
     * OS error code 6: No such device or address 操作系统错误代码6：没有这样的设备或地址
     * OS error code 7: Argument list too long 操作系统错误代码7：参数列表太长
     * OS error code 8: Exec format error 操作系统错误代码8：执行格式错误
     * OS error code 9: Bad file descriptor 操作系统错误代码9：坏的文件描述符
     * OS error code 10: No child processes 操作系统错误代码10：无子过程
     * OS error code 11: Resource temporarily unavailable 操作系统错误代码11：资源暂时不可用
     * OS error code 12: Cannot allocate memory 操作系统错误代码12：无法分配内存
     * OS error code 13: Permission denied 操作系统错误代码13：权限被拒绝
     * OS error code 14: Bad address 操作系统错误代码14：错误的地址
     * OS error code 15: Block device required 操作系统错误代码15：需要块设备
     * OS error code 16: Device or resource busy 操作系统错误代码16：设备或资源忙
     * OS error code 17: File exists 操作系统错误代码17：文件已经存在
     * OS error code 18: Invalid cross-device link 操作系统错误代码18：无效的跨设备链接
     * OS error code 19: No such device 操作系统错误代码19：没有这样的设备
     * OS error code 20: Not a directory 操作系统错误代码20：不是一个目录
     * OS error code 21: Is a directory 操作系统错误代码21：是一个目录
     * OS error code 22: Invalid argument 操作系统错误代码22：无效参数
     * OS error code 23: Too many open files in system 操作系统错误代码23：打开太多的文件系统
     * OS error code 24: Too many open files 操作系统错误代码24：打开的文件太多
     * OS error code 25: Inappropriate ioctl for device 操作系统错误代码25：不适当的设备ioctl使用
     * OS error code 26: Text file busy 操作系统错误代码26：文本文件忙
     * OS error code 27: File too large 操作系统错误代码27：文件太大
     * OS error code 28: No space left on device 操作系统错误代码28：设备上没有空间
     * OS error code 29: Illegal seek 操作系统错误代码29：非法搜索
     * OS error code 30: Read-only file system 操作系统错误代码30：只读文件系统
     * OS error code 31: Too many links 操作系统错误代码31：链接过多
     * OS error code 32: Broken pipe 操作系统错误代码32：管道破裂
     * OS error code 33: Numerical argument out of domain 操作系统错误代码33：超出域的数值参数
     * OS error code 34: Numerical result out of range 操作系统错误代码34：数值结果超出范围
     * OS error code 35: Resource deadlock avoided 操作系统错误代码35：避免资源死锁
     * OS error code 36: File name too long 操作系统错误代码36：文件名太长
     * OS error code 37: No locks available 操作系统错误代码37：没有可用锁
     * OS error code 38: Function not implemented 操作系统错误代码38：功能没有实现
     * OS error code 39: Directory not empty 操作系统错误代码39：目录非空
     * OS error code 40: Too many levels of symbolic links 操作系统错误代码40：符号链接层次太多
     * OS error code 42: No message of desired type 操作系统错误代码42：没有期望类型的消息
     * OS error code 43: Identifier removed 操作系统错误代码43：标识符删除
     * OS error code 44: Channel number out of range 操作系统错误代码44：通道数目超出范围
     * OS error code 45: Level 2 not synchronized 操作系统错误代码45：2级不同步
     * OS error code 46: Level 3 halted 操作系统错误代码46：3级终止
     * OS error code 47: Level 3 reset 操作系统错误代码47：3级复位
     * OS error code 48: Link number out of range 操作系统错误代码48：链接数超出范围
     * OS error code 49: Protocol driver not attached 操作系统错误代码49：协议驱动程序没有连接
     * OS error code 50: No CSI structure available 操作系统错误代码50：没有可用的CSI结构
     * OS error code 51: Level 2 halted 操作系统错误代码51：2级中断
     * OS error code 52: Invalid exchange 操作系统错误代码52：无效的交换
     * OS error code 53: Invalid request descriptor 操作系统错误代码53：无效的请求描述符
     * OS error code 54: Exchange full 操作系统错误代码54：交换空间满
     * OS error code 55: No anode 操作系统错误代码55：阳极不存在
     * OS error code 56: Invalid request code 操作系统错误代码56：无效的请求代码
     * OS error code 57: Invalid slot 操作系统错误代码57：无效的槽
     * OS error code 59: Bad font file format 操作系统错误代码59：错误的字体文件格式
     * OS error code 60: Device not a stream 操作系统错误代码60：设备不属于流类型
     * OS error code 61: No data available 操作系统错误代码61：无可用数据
     * OS error code 62: Timer expired 操作系统错误代码62：超时
     * OS error code 63: Out of streams resources 操作系统错误代码63：超出流资源范围
     * OS error code 64: Machine is not on the network 操作系统错误代码64：主机不在网络上
     * OS error code 65: Package not installed 操作系统错误代码65：软件包没有安装
     * OS error code 66: Object is remote 操作系统错误代码66：对象是远程的
     * OS error code 67: Link has been severed 操作系统错误代码67：链接被切断
     * OS error code 68: Advertise error 操作系统错误代码68：广告错误
     * OS error code 69: Srmount error 操作系统错误代码69：srmount错误
     * OS error code 70: Communication error on send 操作系统错误代码70：发送数据时通讯错误
     * OS error code 71: Protocol error 操作系统错误代码71：协议错误
     * OS error code 72: Multihop attempted 操作系统错误代码72：企图进行多次跳转
     * OS error code 73: RFS specific error 操作系统错误代码73：RFS类型错误
     * OS error code 74: Bad message 操作系统错误代码74：坏消息
     * OS error code 75: Value too large for defined data type 操作系统错误代码75：数值超过对于给定的数据类型
     * OS error code 76: Name not unique on network 操作系统错误代码76：主机名在网络上不是唯一
     * OS error code 77: File descriptor in bad state 操作系统错误代码77：坏状态的文件描述符
     * OS error code 78: Remote address changed 操作系统错误代码78：远端地址改变
     * OS error code 79: Can not access a needed shared library 操作系统错误代码79：无法访问需要的共享库
     * OS error code 80: Accessing a corrupted shared library 操作系统错误代码80：访问了一个损坏的共享库
     * OS error code 81: .lib section in a.out corrupted 操作系统错误代码81： a. out文件中的.lib段损坏。
     * OS error code 82: Attempting to link in too many shared libraries 操作系统错误代码82：试图链接太多的共享库
     * OS error code 83: Cannot exec a shared library directly 操作系统错误代码83：不能直接执行一个共享库
     * OS error code 84: Invalid or incomplete multibyte or wide character 操作系统错误代码84：无效或不完整的多字节以及宽字符
     * OS error code 85: Interrupted system call should be restarted 操作系统错误代码85：中断的系统调用需要重新启动
     * OS error code 86: Streams pipe error 操作系统错误代码86：流管道错误
     * OS error code 87: Too many users 操作系统错误代码87：太多用户
     * OS error code 88: Socket operation on non-socket 操作系统错误代码88：在非套接字接口进行套接字操作
     * OS error code 89: Destination address required 操作系统错误代码89：需要目标地址
     * OS error code 90: Message too long 操作系统错误代码90：消息太长
     * OS error code 91: Protocol wrong type for socket 操作系统错误代码91：socket协议错误类型
     * OS error code 92: Protocol not available 操作系统错误代码92：协议不可用
     * OS error code 93: Protocol not supported 操作系统错误代码93：协议不支持
     * OS error code 94: Socket type not supported 操作系统错误代码94：socket类型不支持
     * OS error code 95: Operation not supported 操作系统错误代码95：操作不支持
     * OS error code 96: Protocol family not supported 操作系统错误代码96：协议族不支持
     * OS error code 97: Address family not supported by protocol 操作系统错误代码97：协议不支持地址族
     * OS error code 98: Address already in use 操作系统错误代码98：地址已在使用
     * OS error code 99: Cannot assign requested address 操作系统错误代码99：无法分配请求的地址
     * OS error code 100: Network is down 操作系统错误代码100：网络瘫痪
     * OS error code 101: Network is unreachable 操作系统错误代码101：网络不可达
     * OS error code 102: Network dropped connection on reset 操作系统错误代码102：网络复位时连接丢失
     * OS error code 103: Software caused connection abort 操作系统错误代码103：软件导致连接中断
     * OS error code 104: Connection reset by peer 操作系统错误代码104：连接被重置
     * OS error code 105: No buffer space available 操作系统错误代码105：没有可用的缓冲空间
     * OS error code 106: Transport endpoint is already connected 操作系统错误代码106：传输端点已连接
     * OS error code 107: Transport endpoint is not connected 操作系统错误代码107：运输端点没有连接上
     * OS error code 108: Cannot send after transport endpoint shutdown 操作系统错误代码108：运输终点关闭后无法发送数据
     * OS error code 109: Too many references: cannot splice 操作系统错误代码109：引用太多：不能接合
     * OS error code 110: Connection timed out 操作系统错误代码110：连接超时
     * OS error code 111: Connection refused 操作系统错误代码111：连接被拒绝
     * OS error code 112: Host is down 操作系统错误代码112：主机已关闭
     * OS error code 113: No route to host 操作系统错误代码113：没有路由到主机
     * OS error code 114: Operation already in progress 操作系统错误代码114：进程已运行
     * OS error code 115: Operation now in progress 操作系统错误代码115：正在进行操作
     * OS error code 116: Stale NFS file handle 操作系统错误代码116：陈旧的NFS文件句柄
     * OS error code 117: Structure needs cleaning 操作系统错误代码117：结构需要清除
     * OS error code 118: Not a XENIX named type file 操作系统错误代码118：不是一个XENIX命名类型的文件
     * OS error code 119: No XENIX semaphores available 操作系统错误代码119：没有XENIX信号量可用
     * OS error code 120: Is a named type file 操作系统错误代码120：是一个指定类型的文件
     * OS error code 121: Remote I/O error 操作系统错误代码121：远程输入/输出错误
     * OS error code 122: Disk quota exceeded 操作系统错误代码122：超出磁盘配额
     * OS error code 123: No medium found 操作系统错误代码123：没有发现介质
     * OS error code 124: Wrong medium type 操作系统错误代码124：错误的介质类型
     * OS error code 125: Operation canceled 操作系统错误代码125：操作取消
     * OS error code 126: Required key not available 操作系统错误代码126：所需的Key不可用
     * OS error code 127: Key has expired 操作系统错误代码127：Key已过期
     * OS error code 128: Key has been revoked 操作系统错误代码128：Key被撤销
     * OS error code 129: Key was rejected by service 操作系统错误代码129：Key被拒绝服务
     * OS error code 130: Owner died 操作系统错误代码130：属主死亡
     * OS error code 131: State not recoverable 操作系统错误代码131：状态不可恢复
     */

    public interface ExeResult {
        void onResult(int code, String msg);
    }


    /**
     * 检测虚拟网卡是否配置成功
     */
    public static boolean checkNetConfig() {
        try {

            LogUtils.d(TAG, "checkNetConfig !!!!!!!!!!!!!!!!!!!!");
            Process process = Runtime.getRuntime().exec(IFCONFIG);
            String content = consumeInputStream(process.getInputStream());
            int result = process.waitFor();
            LogUtils.d(TAG, "checkNetConfig content:  " + content);
            LogUtils.d(TAG, "checkNetConfig result:   " + result);
//            boolean res = result == 0 && content.contains("192.168.88.9");
//            LogUtils.d(TAG,"res = "+res);
//            return res;
            return result == 0 && content.contains("192.168.88.9");
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "checkNetConfig IOException:" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "checkNetConfig InterruptedException:" + e.getMessage());
        }
        return false;
    }

    public static void execInstallCmd(String appPath, ExeResult exeResult) {
        try {
            //("ps -P|grep bg")执行失败，PC端adb shell ps -P|grep bg执行成功
            //Process process = Runtime.getRuntime().exec("ps -P|grep tv");
            //-P 显示程序调度状态，通常是bg或fg，获取失败返回un和er
            // Process process = Runtime.getRuntime().exec("ps -P");
            //打印进程信息，不过滤任何条件
            String[] installAirFaceRobot = {"su", "-c", "pm install -r -d " + appPath};
            Process process = Runtime.getRuntime().exec(installAirFaceRobot);
            String content = consumeInputStream(process.getInputStream());
            int result = process.waitFor();
            LogUtils.d(TAG, "exec result:" + content + " result:" + result);
            if (exeResult != null) {
                exeResult.onResult(result, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "exec IOException:" + e.getMessage());
            if (exeResult != null) {
                exeResult.onResult(-1, e.getMessage());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "exec InterruptedException:" + e.getMessage());
            if (exeResult != null) {
                exeResult.onResult(-1, e.getMessage());
            }
        }
    }
}
