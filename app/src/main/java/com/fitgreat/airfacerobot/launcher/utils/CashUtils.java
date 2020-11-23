package com.fitgreat.airfacerobot.launcher.utils;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import java.util.List;

/**
 * 导航点  执行任务操作工具类
 */
public class CashUtils {
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
        if (locationList != null) {
            for (LocationEntity mLocationEntity : locationList) {
                String f_name = mLocationEntity.getF_Name();
                if (f_name.equals(fNameText)) {
                    locationEntity = mLocationEntity;
                }
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
        if (operationList != null) {
            for (OperationInfo mOperationInfo : operationList) {
                if (mOperationInfo.getF_Name().equals(fNameData)) {
                    operationInfo = mOperationInfo;
                }
            }
        }
        return operationInfo;
    }


    /**
     * 获取常见问题信息列表
     */
    public static List<CommonProblemEntity> getProblemList() {
        List<CommonProblemEntity> problemList = null;
        String stringProblemList = SpUtils.getString(MyApp.getContext(), "problemList", null);
        if (stringProblemList != null) {
            problemList = JSON.parseArray(stringProblemList, CommonProblemEntity.class);
            LogUtils.json(TAG, JSON.toJSONString(problemList));
        }
        return problemList;
    }

    /**
     * 获取单个常见问题信息
     */
    public static CommonProblemEntity getProblemOne(String fNameText) {
        CommonProblemEntity commonProblemEntity = null;
        List<CommonProblemEntity> problemList = getProblemList();
        if (problemList != null) {
            for (CommonProblemEntity mCommonProblemEntity : problemList) {
                String F_Question = mCommonProblemEntity.getF_Question();
                if (F_Question.equals(fNameText)) {
                    commonProblemEntity = mCommonProblemEntity;
                }
            }
        }
        return commonProblemEntity;
    }
}
