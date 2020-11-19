package com.fitgreat.airfacerobot.launcher.utils;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import java.util.List;

/**
 * 导航点  执行任务操作工具类
 */
public class LocalCashUtils {
    private static final String TAG = "LocationOperationUtils";

    /**
     * 获取导航点信息列表
     */
    public static List<LocationEntity> getLocationList() {
        List<LocationEntity> locationList = null;
        String stringLocationList = SpUtils.getString(MyApp.getContext(), "locationList", null);
        if (stringLocationList != null) {
            locationList = JSON.parseArray(stringLocationList, LocationEntity.class);
            LogUtils.json(TAG, JSON.toJSONString(locationList));
        }
        return locationList;
    }

    /**
     * 获取单个导航点信息
     */
    public static LocationEntity getLocationOne(String fNameText) {
        LocationEntity locationEntity = null;
        List<LocationEntity> locationList = getLocationList();
        for (LocationEntity mLocationEntity :
                locationList) {
            if (mLocationEntity.getF_Name().equals(fNameText)) {
                locationEntity = mLocationEntity;
            }
        }
        return locationEntity;
    }

    /**
     * 获取执行任务信息列表
     */
    public static List<OperationInfo> getOperationList() {
        List<OperationInfo> operationList = null;
        String stringLocationList = SpUtils.getString(MyApp.getContext(), "operationList", null);
        if (stringLocationList != null) {
            operationList = JSON.parseArray(stringLocationList, OperationInfo.class);
            LogUtils.json(TAG, JSON.toJSONString(operationList));
        }
        return operationList;
    }

    /**
     * 获取单个执行任务信息
     */
    public static OperationInfo getOperationOne(String fNameData) {
        OperationInfo operationInfo = null;
        List<OperationInfo> operationList = getOperationList();
        for (OperationInfo mOperationInfo :
                operationList) {
            if (mOperationInfo.getF_Name().equals(fNameData)) {
                operationInfo = mOperationInfo;
            }
        }
        return operationInfo;
    }
}
