package com.fitgreat.headeractuator.model;

import android.util.Log;

import com.fitgreat.headeractuator.ActuatorConstant;

/**
 * Byte1、 Header：0XFE/0xFD (固定不变)；
 * Byte2、 ID：0x01 代表水平舵机；0x02 代表垂直舵机；
 * Byte3和Byte4、 舵机角度 =（Byte3）+（Byte4）* 0xFF；数值范围：0°~359°
 * Byte5和Byte6、 舵机速度 =（Byte5）+（Byte6）* 0xFF；数值范围：0°/s~100°/s
 * Byte7和Byte8、 舵机扭力 =（Byte7）+（Byte8）* 0xFF；数值范围：0%~100%
 * Byte9、 设置MCU到安卓的反馈信息刷新频率；0~100HZ，0HZ为不反馈
 * Byte10、：校验和
 * 对Byte2~8进行校验和，取低字节。
 * <p>
 * 例如：FE 01 10 00 10 00 10 00 06 37
 * 表示水平舵机按照速度16°/s、扭力16%运行到绝对坐标16°位置。舵机反馈频率为6hz，其他控制同理。
 * FE 02 10 00 10 00 10 00 06 38
 * 表示垂直舵机按照速度16°/s、扭力16%运行到绝对坐标16°位置。舵机反馈频率为6hz，其他控制同理。
 * 注：
 * 机器人极限角度：
 * 水平头部：180°正中（复位），范围0°~359°，小值为顺时针，大值为逆时针。
 * 垂直头部：90°正中（复位），范围20°~160°，小值为低头，大值为抬头。
 */
public class SendPacket {
    public static final String TAG = "Motor";

    public SendPacket(Motor motor) {
        this.setOrientaion(motor.mOrientaion);
        this.setAngle(motor.mAngle);
        this.setSpeed(motor.mSpeed);
        this.setTorque(motor.mTorque);
        this.setRate(motor.mRate);
    }

    public SendPacket() {
        this.setOrientaion(ActuatorConstant.MOTOR_VERTICAL);
        this.setAngle(25);
        this.setSpeed(50);
        this.setTorque(100);
        this.setRate(1);
    }

    /**
     * 由于java的byte的数字表现方式是-127 到128，所以需要强转格式（二进制表示方式是一样的）
     */
    private byte[] sendData = {ActuatorConstant.HEAD_OUT,
            ActuatorConstant.MOTOR_HORIZONTAL,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public void setOrientaion(int orientaion) {
        if (orientaion != ActuatorConstant.MOTOR_HORIZONTAL && orientaion != ActuatorConstant.MOTOR_VERTICAL) {
            Log.e(TAG, "setOrientaion error , return");
            return;
        }
        sendData[1] = (byte) orientaion;
    }

    /**
     * * Byte3和Byte4、 舵机角度 =（Byte3）+（Byte4）* 0xFF；数值范围：0°~359°
     *
     * @param angel Byte3和Byte4、 舵机角度 =（Byte3）+（Byte4）* 0xFF；数值范围：0°~359°
     */
    public void setAngle(int angel) {
        int newAngel = angel;
        if (newAngel > ActuatorConstant.MAX_ANGLE) {
            newAngel = ActuatorConstant.MAX_ANGLE;
        }
        if (newAngel < ActuatorConstant.MIN_ANGLE) {
            newAngel = ActuatorConstant.MIN_ANGLE;
        }
        if (newAngel > 255) {
            sendData[3] = 0x1;//高位
            sendData[2] = (byte) (newAngel - 255);//低位 angel - 255
        } else {
            sendData[3] = 0x0;//高位
            sendData[2] = (byte) (newAngel);//低位 angel
        }
    }

    /**
     * Byte5和Byte6、 舵机速度 =（Byte5）+（Byte6）* 0xFF；
     *
     * @param speed 数值范围：0°/s~100°/s
     */
    public void setSpeed(int speed) {
        int newSpeed = speed;
        if (newSpeed > ActuatorConstant.MAX_SPEED) {
            newSpeed = ActuatorConstant.MAX_SPEED;
        }
        if (newSpeed < ActuatorConstant.MIN_SPEED) {
            newSpeed = ActuatorConstant.MIN_SPEED;
        }

        //最大值不大于255，故直接强转
        sendData[4] = (byte) newSpeed;
        sendData[5] = 0x00;
    }

    /**
     * Byte7和Byte8、 舵机扭力 =（Byte7）+（Byte8）* 0xFF；数值范围：0%~100%
     *
     * @param torque 数值范围：0~100
     */
    public void setTorque(int torque) {
        int newTorque = torque;
        if (newTorque > ActuatorConstant.MAX_TORQUE) {
            newTorque = ActuatorConstant.MAX_TORQUE;
        }
        if (newTorque < ActuatorConstant.MIN_TORQUE) {
            newTorque = ActuatorConstant.MIN_TORQUE;
        }
        //最大值不大于255，故强转低位即可
        sendData[6] = (byte) newTorque;
        sendData[7] = 0x00;
    }

    /**
     * 设置MCU到安卓的反馈信息刷新频率；0~100HZ，0HZ为不反馈
     *
     * @param rate 0~100HZ，0HZ为不反馈
     */
    public void setRate(int rate) {
        int newRate = rate;
        if (newRate > ActuatorConstant.MAX_RATE) {
            newRate = ActuatorConstant.MAX_RATE;
        }
        if (newRate < ActuatorConstant.MIN_RATE) {
            newRate = ActuatorConstant.MIN_RATE;
        }
        sendData[8] = (byte) rate;
    }


    public byte[] encodeBytes() {
        return sendData;
    }


    public SendPacket decodeBytes(byte[] rawData) {
        if (rawData.length != sendData.length) {
            return null;
        }
        for (int i = 1; i < sendData.length; i++) {
            sendData[i] = rawData[i];
        }
        return this;
    }
}
