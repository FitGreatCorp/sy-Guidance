package com.fitgreat.headeractuator;

public class Utils {

    public static int byteToUnsignedInt(byte val) {
        int newVal = 0;
        newVal = val & 0x0ff;
        System.out.println("还原为int为：" + newVal);
        return newVal;
    }

    public static byte intToByte(int val) {
        if (val < 0) {
            return ((byte) 0);
        }
        if (val > 255) {
            return ((byte) 255);
        }
        return ((byte) val);
    }
}
