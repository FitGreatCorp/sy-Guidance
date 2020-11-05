package com.fitgreat.headeractuator.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Motor implements Parcelable {
    //方向
    int mOrientaion = 0;
    //角度
    int mAngle = 0;
    //速度
    int mSpeed = 10;
    //扭力
    int mTorque = 100;
    //速率
    int mRate = 1;

    protected Motor(Parcel in) {
        mOrientaion = in.readInt();
        mAngle = in.readInt();
        mSpeed = in.readInt();
        mTorque = in.readInt();
        mRate = in.readInt();
    }

    /**
     * 反序列化
     */
    public static final Creator<Motor> CREATOR = new Creator<Motor>() {
        @Override
        public Motor createFromParcel(Parcel in) {
            return new Motor(in);
        }

        @Override
        public Motor[] newArray(int size) {
            return new Motor[0];
        }
    };

    /**
     * 描述
     *
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 序列化
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mOrientaion);
        dest.writeInt(mAngle);
        dest.writeInt(mSpeed);
        dest.writeInt(mTorque);
        dest.writeInt(mRate);
    }

}
