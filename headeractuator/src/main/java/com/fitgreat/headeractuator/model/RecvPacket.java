package com.fitgreat.headeractuator.model;


import com.fitgreat.headeractuator.ActuatorConstant;

/**
 * 读取到的信息
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
 */
public class RecvPacket {

    private byte[] msgData = {ActuatorConstant.HEAD_IN,
            ActuatorConstant.MOTOR_HORIZONTAL,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public int getOrientaion() {
        return msgData[1];
    }

    public int getAngle() {
        int angle = 0;
        angle += msgData[2] & 0xFF;
        angle += msgData[3] & 0xFF << 8;
        return angle;
    }

    public int getSpeed() {
        int speed = 0;
        speed += msgData[4] & 0xFF;
        speed += msgData[5] & 0xFF << 8;
        return speed;
    }

    public int getTorque() {
        int torque = 0;
        torque += msgData[6] & 0xFF;
        torque += msgData[7] & 0xFF << 8;
        return torque;
    }

    public int getRate() {
        int rate = 0;
        rate += msgData[8] & 0xFF;
        return rate;
    }


    public byte[] encodeBytes() {
        return msgData;
    }


    public RecvPacket decodeBytes(byte[] rawData) {
        if (rawData.length != msgData.length) {
            return null;
        }
        for (int i = 1; i < msgData.length; i++) {
            msgData[i] = rawData[i];
        }
        return this;
    }

    @Override
    public String toString() {
        return "orientation = " + getOrientaion() + ",Angle = " + getAngle() + ",speed ="
                + getSpeed() + ",torque =" + getTorque() + ",rate ="
                + getRate();
    }
}
