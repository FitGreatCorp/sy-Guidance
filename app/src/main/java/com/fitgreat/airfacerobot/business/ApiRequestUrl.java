package com.fitgreat.airfacerobot.business;


/**
 * APP服务接口管理<p>
 *
 * @author zixuefei
 * @since 2019/10/23 17:42
 */
public interface ApiRequestUrl {
    /**
     * HEADER 认证参数
     */
    String AUTH_STR = "fU459arccaBJLfygiCGXBHrb:T5GOi1CRVi8WFsjCzxDzNQBmksEBB7uml47Q7ojtP4kCJW5Z";


    /* 连接服务器相关接口*/
    /**
     * 服务器认证获取token接口
     */
    String ROBOT_AUTH = ApiDomainManager.getRobotDomain() + "/oauth/v2/token";

    /**
     * 获取机器人信息接口
     */
    String ROBOT_INFO = ApiDomainManager.getFitgreatDomain() + "/api/robot/info";

    /**
     * airface app 版本更新接口
     */
    String CHECK_APP_VERSION = ApiDomainManager.getFitgreatDomain() + "/api/airface/v1/app/version";

    /**
     * ros 硬件版本更新接口
     */
    String CHECK_HARDWARE_VERSION = ApiDomainManager.getFitgreatDomain() + "/api/airface/hardware/v2/app/version";

    /**
     * app升级结果
     */
    String APP_UPDATE_RESULT = ApiDomainManager.getFitgreatDomain() + "/api/app/update/result/add";

    /**
     * 硬件升级结果
     */
    String HARDWARE_UPDATE_RESULT = ApiDomainManager.getFitgreatDomain() + "/api/hardware/update/insert";

    /**
     * SignalR 连接接口
     */
    String SIGNALR_URL = ApiDomainManager.getFitgreatDomain() + "/signalr/";

    /**
     * 通过id获取成员信息
     */
    String GET_USERINFO_BY_ID = ApiDomainManager.getFitgreatDomain() + "/api/signalr/room/member/info";

    /**
     * 网络服务器时间同步
     */
    String SYNC_SERVER_TIME = ApiDomainManager.getFitgreatDomain() + "/api/synctime";

    /**
     * 获取下一步操作任务
     */
    String GET_NEXT_STEP = ApiDomainManager.getFitgreatDomain() + "/api/robot/instruction/nextstep";

    /**
     * 更新活动流程状态
     */
    String UPDATE_INSTRUCTION = ApiDomainManager.getFitgreatDomain() + "/api/activities/activity/instruction/update";
    /**
     * 更新操作状态
     */
    String UPDATE_OPERATION_STATUS = ApiDomainManager.getFitgreatDomain() + "/api/activities/update";

    /**
     * 强行终止任务
     */
    String STOP_OPERATE_TASK = ApiDomainManager.getFitgreatDomain() + "/api/robot/activities/stop";

    /**
     * 定时更新机器人信息
     */
    String UPLOAD_ROBOT_STATUS = ApiDomainManager.getFitgreatDomain() + "/api/robot/state/update";


    /**
     * 获取声网token
     */
    String AGORA_LOGIN = ApiDomainManager.getRobotDomain() + "/oauth/v2/agora/token";

    /**
     * 绑定声网UId
     */
    String BIND_UID = ApiDomainManager.getFitgreatDomain() + "/api/signalr/room/member/bind/uid";

    /**
     * 更新位置信息
     */
    String UPDATE_POSITION = ApiDomainManager.getFitgreatDomain() + "/api/robot/position/update";

    /**
     * 添加任务
     */
    String CREATE_TASK = ApiDomainManager.getFitgreatDomain() + "/api/activities/add";


    /**
     * 获取通讯组信息
     */
    String GET_GROUP_MESSAGE = ApiDomainManager.getFitgreatDomain() + "/api/signalr/room/members/list";

    /**
     * 判断是否在组中
     */
    String IS_EXIT = ApiDomainManager.getFitgreatDomain() + "/api/signalr/room/members/exist";

    /**
     * 获取机器人地图,导航地点数据
     */
    String GET_ONE_MAP = ApiDomainManager.getFitgreatDomain() + "/api/robot/map/info";
    /**
     * 获取医院操作任务数据
     */
    String GET_OPERATION_LIST = ApiDomainManager.getFitgreatDomain() + "/api/hospital/operation/list";

    /**
     * 新增活动
     */
    String CREATE_ACTION = ApiDomainManager.getFitgreatDomain() + "/api/activities/add";
    /**
     * 获取特定工作流
     */
    String SPECIFIC_WORK_FLOM = ApiDomainManager.getFitgreatDomain() + "/api/workflow/info";
    /**
     * 发起活动
     */
    String INITIATE_ACTION = ApiDomainManager.getFitgreatDomain() + "/api/activities/add/v1";

    /**
     * 验证设置模块密码
     */
    String VERIFY_MODULE_PASSWORD = ApiDomainManager.getFitgreatDomain() + "/api/robot/check/pwd";

    /**
     * 定时上传本地日志文件
     */
    String UPLOAD_LOCAL_LOG = ApiDomainManager.getFitgreatDomain() + "/api/upload/log";
}
