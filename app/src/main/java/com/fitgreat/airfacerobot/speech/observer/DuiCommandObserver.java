package com.fitgreat.airfacerobot.speech.observer;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dsk.duiwidget.CommandObserver;
import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.launcher.model.CommandDataEvent;
import com.fitgreat.airfacerobot.launcher.model.LocationEntity;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.airfacerobot.launcher.utils.LocalCashUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import static com.fitgreat.airfacerobot.constants.RobotConfig.CLICK_EMERGENCY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

/**
 * 客户端CommandObserver, 用于处理客户端动作的执行以及快捷唤醒中的命令响应.
 * 返回数据
 */
public class DuiCommandObserver implements CommandObserver {
    private final Gson gson;
    private String[] mSubscribeCommands = new String[]{
            "sys.dialog.state",
            "context.output.text",
            "context.input.text",
            "context.widget.content",
            "context.widget.list",
            "context.widget.web",
            "context.widget.media",
            "context.widget.custom",
            "robot.action.navigation.operation.task", //先导航,后执行任务
            "robot.action.operation.task", //执行任务
            "robot.action.navigation", //导航
            "robot.action.termination", //终止   terminationTag
            "robot.action.charge",//冲电
            "robot.action.ruijin.timeout",//瑞金学术大厅暂停当前任务
    };

    public DuiCommandObserver() {
        gson = new Gson();
    }

    // 注册当前更新消息
    public void regist() {
        DDS.getInstance().getAgent().subscribe(mSubscribeCommands,
                this);
    }

    // 注销当前更新消息
    public void unregist() {
        DDS.getInstance().getAgent().unSubscribe(this);
    }

    /**
     * @param command robot.action.task
     * @param data    {"do":"入院宣教","to":"二号床","taskName":"执行任务","skillId":"2020071500000056","skillName":"宣教","intentName":"识别宣教任务"}
     */
    @Override
    public void onCall(String command, String data) {
        boolean emergencyTag = SpUtils.getBoolean(MyApp.getContext(), CLICK_EMERGENCY_TAG, false);
        LogUtils.d("CommandTodo", "command: " + command + " data: " + data + "  急停按钮是否被按下  " + emergencyTag);
        if (emergencyTag) { //急停按钮被按下
            EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, "抱歉我的急停按钮已启动，无法执行该任务"));
        } else { //急停按钮没有被按下
            emergencyNotClickOn(data);
        }
    }

    /**
     * 急停按钮没有按下时,根据不同的指令下发不同的任务
     *
     * @param data
     */
    private void emergencyNotClickOn(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            CommandDataEvent commandDataEvent = new CommandDataEvent();
            if (jsonObject.has("terminationTag")) { //指令停止
                commandDataEvent.setCommandType(1);
                LogUtils.d("CommandTodo", "取消指令");
            }
            EventBus.getDefault().post(commandDataEvent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
