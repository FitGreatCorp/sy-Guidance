package com.fitgreat.headeractuator;


public class ActuatorConstant {
    //Byte1
    public static final byte HEAD_OUT = Utils.intToByte(0XFE);
    public static final byte HEAD_IN = Utils.intToByte(0XFD);


    //Byte2
    public static final byte MOTOR_HORIZONTAL = 0X01;
    public static final byte MOTOR_VERTICAL = 0X02;

    //Byte3 Byte4
    public static final int MAX_ANGLE = 359;
    public static final int MIN_ANGLE = 0;

    //Byte5 Byte6
    public static final int MAX_SPEED = 100;
    public static final int MIN_SPEED = 0;

    //Byte7 Byte8
    public static final int MAX_TORQUE = 100;
    public static final int MIN_TORQUE = 0;

    //Byte9
    public static final int MAX_RATE = 100;
    public static final int MIN_RATE = 0;
}
