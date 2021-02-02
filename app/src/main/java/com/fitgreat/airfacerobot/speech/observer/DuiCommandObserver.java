package com.fitgreat.airfacerobot.speech.observer;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dsk.duiwidget.CommandObserver;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AskAnswerDataEvent;
import com.fitgreat.airfacerobot.model.CommandDataEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.LocationEntity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_BY_INSTRUCTION;
import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.SINGLE_POINT_NAVIGATION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CLICK_EMERGENCY_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_WHETHER_SHOW;
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
            "common.problem.one",
            "common.problem.two",
            "common.problem.three",
            "common.problem.four",
            "common.problem.five",
            "common.problem.six",
            "common.problem.seven",
            "single.point.navigation.sy",
            "single.operation.task.sy",
    };
    private CommandDataEvent commandDataEvent;
    private AskAnswerDataEvent askAnswerDataEvent;

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
        LogUtils.d("CommandTodo", "  data: " + data);
        if (emergencyTag) { //急停按钮被按下
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.emergency_click_prompt)));
        } else {
            //急停按钮没有被按下
            try {
                JSONObject jsonObject = new JSONObject(data);
                commandDataEvent = new CommandDataEvent();
                askAnswerDataEvent = new AskAnswerDataEvent();
                boolean mainPageShowTag = SpUtils.getBoolean(MyApp.getContext(), MAIN_PAGE_WHETHER_SHOW, false);
                if (jsonObject.has("address")) { //识别单点导航任务指令
                    String address = jsonObject.getString("address");
                    LocationEntity locationEntity = CashUtils.getLocationOne(address);
                    if (locationEntity == null) { //指令识别地点不在设置地点范围内
                        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.prompt_no_navigation)));
                        return;
                    }
                    if (mainPageShowTag) {  //常见问题模块通过指令实现问题对答
                        commandDataEvent.setCommandType(SINGLE_POINT_NAVIGATION);
                        commandDataEvent.setLocationEntity(locationEntity);
                        EventBus.getDefault().post(commandDataEvent);
                    } else {  //首页通过指令进入常见问题模块
                        askAnswerDataEvent.setCommandType(SINGLE_POINT_NAVIGATION);
                        askAnswerDataEvent.setLocationEntity(locationEntity);
                        EventBus.getDefault().post(askAnswerDataEvent);
                    }
                    LogUtils.d(DEFAULT_LOG_TAG, "address:   " + address + "  mainPageShowTag  " + mainPageShowTag);
                } else if (jsonObject.has("problem")) { //识别单个常见问题操作指令
                    String problem = jsonObject.getString("problem");
                    CommonProblemEntity commonProblemEntity = CashUtils.getProblemOne(problem);
                    if (commonProblemEntity == null) {  //指令识别常见问题不在设置问题范围内
                        EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, MvpBaseActivity.getActivityContext().getString(R.string.prompt_problem_has_no)));
                        return;
                    }
                    if (mainPageShowTag) {  //常见问题模块通过指令实现问题对答
                        commandDataEvent.setCommandType(COMMON_PROBLEM_TAG);
                        commandDataEvent.setCommonProblemEntity(commonProblemEntity);
                        EventBus.getDefault().post(commandDataEvent);
                    } else {  //首页通过指令进入常见问题模块
                        askAnswerDataEvent.setCommandType(COMMON_PROBLEM_TAG);
                        askAnswerDataEvent.setCommonProblemEntity(commonProblemEntity);
                        EventBus.getDefault().post(askAnswerDataEvent);
                    }
                    LogUtils.d(DEFAULT_LOG_TAG, "problem:   " + problem + "  mainPageShowTag  " + mainPageShowTag);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
