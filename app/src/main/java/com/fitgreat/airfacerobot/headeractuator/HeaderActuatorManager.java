package com.fitgreat.airfacerobot.headeractuator;

import android.os.Handler;

import com.fitgreat.archmvp.base.util.ExecutorManager;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.headeractuator.ActuatorConnector;
import com.fitgreat.headeractuator.ActuatorConstant;
import com.fitgreat.headeractuator.model.RecvPacket;
import com.fitgreat.headeractuator.model.SendPacket;

import java.util.Arrays;

/**
 * 头部舵机管理器<p>
 *
 * @author zixuefei
 * @since 2020/3/26 0020 11:46
 */
public class HeaderActuatorManager {
    private static final String TAG = "HeaderActuatorManager";
    private ActuatorConnector actuatorConnector;
    private Handler handler = new Handler();
    private InitListener initListener;

    public void initActuator() {
        actuatorConnector = new ActuatorConnector(dataListener);
        int init_status = actuatorConnector.initSerialPort();
        if (init_status != -1) {
            // 初始化成功
            LogUtils.d("initNode", "--------HeaderActuator init success---------");
            handler.postDelayed(() -> {
                sendAnglePacket(45, ActuatorConstant.MOTOR_VERTICAL);
                if (initListener != null) {
                    initListener.initState(true);
                }
            }, 500);
//                sendAnglePacket(180, ActuatorConstant.MOTOR_HORIZONTAL);
        } else {
            //初始化失败
            LogUtils.e("initNode", "--------HeaderActuator init failed---------");
            if (initListener != null) {
                initListener.initState(false);
            }
        }
    }

    public void setInitListener(InitListener initListener) {
        this.initListener = initListener;
    }

    /**
     * 发送给串口的数据
     */
    public void sendAnglePacket(int angle, int direction) {
        ExecutorManager.getInstance().executeTask(() -> {
            if (actuatorConnector == null) {
                return;
            }
            SendPacket packet = new SendPacket();
            if (direction != -1) {
                packet.setOrientaion(direction);
            }
            packet.setAngle(angle);
            byte[] encodeBytes = packet.encodeBytes();
            int status = actuatorConnector.send(encodeBytes);
            LogUtils.d("", "send packet bytes" + Arrays.toString(encodeBytes) + " status: " + status);
        });
    }


    private ActuatorConnector.DataListener dataListener = new ActuatorConnector.DataListener() {
        /**
         * DataListener 接口实现方法,用于connector 收到消息后的回调
         *
         * @param data
         */
        @Override
        public void onReceive(byte[] data) {
            if (data == null) {
                return;
            }
            //检查data数据是否合法
            RecvPacket recvPacket = new RecvPacket();
            recvPacket.decodeBytes(data);
            LogUtils.d(TAG, "recvPacket:" + recvPacket.toString());
        }
    };

    public void reset() {
        if (actuatorConnector != null) {
            actuatorConnector.destroy();
            actuatorConnector = null;
        }
    }

    public interface InitListener {
        void initState(boolean isSuccess);
    }
}
