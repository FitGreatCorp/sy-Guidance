package com.fitgreat.airfacerobot.speech.observer;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.utils.CashUtils;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.AskAnswerDataEvent;
import com.fitgreat.airfacerobot.model.CommandDataEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.DialogStateEvent;
import com.fitgreat.airfacerobot.model.RobotSignalEvent;
import com.fitgreat.airfacerobot.speech.model.CommonProblemEvent;
import com.fitgreat.airfacerobot.speech.model.MessageBean;
import com.fitgreat.airfacerobot.speech.model.ShowContent;
import com.fitgreat.airfacerobot.speech.model.ShowContentMain;
import com.fitgreat.airfacerobot.speech.model.WeatherBean;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.fitgreat.airfacerobot.constants.Constants.COMMON_PROBLEM_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_FIVE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.JUMP_COMMON_PROBLEM_PAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MAIN_PAGE_WHETHER_SHOW;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

/**
 * 客户端MessageObserver, 用于处理客户端动作的消息响应.
 */
public class DuiMessageObserver implements MessageObserver {

    private boolean mIsFirstVar = true;
    private boolean mHasvar = false;
    private Gson mGson;
    private String[] mSubscribeKeys = new String[]{
            "sys.dialog.state",
            "context.output.text",
            "context.input.text",
            "context.widget.content",
            "context.widget.list",
            "context.widget.web",
            "context.widget.media",
            "context.widget.custom",
            "sys.dialog.start",
            "sys.resource.updated",
    };
    private static final String TAG = "DuiMessageObserver";
    private String currentLanguage;
    private AskAnswerDataEvent askAnswerDataEvent;
    private CommandDataEvent commandDataEvent;


    public DuiMessageObserver() {
        mGson = new Gson();
    }

    public void regist() {
        DDS.getInstance().getAgent().subscribe(mSubscribeKeys, this);
    }

    // 注销当前更新消息
    public void unregist() {
        DDS.getInstance().getAgent().unSubscribe(this);
    }

    /**
     * @param message context.output.text
     * @param data    {"skillName":"宣教","skillId":"2020071500000056","intentName":"识别宣教任务","taskName":"执行任务","text":"好的，我马上到二号床去做入院宣教"}
     */
    @Override
    public void onMessage(String message, String data) {
//        try {
//            LogUtils.d(DEFAULT_LOG_FIVE, "  |message| : " + message +"  |data| : " + data+ "  oneshot模式  " + (DDS.getInstance().getAgent().getWakeupEngine().getOneshotState()));
//        } catch (DDSNotInitCompleteException e) {
//            e.printStackTrace();
//        }
        boolean startCommonProblemModelTag = SpUtils.getBoolean(MyApp.getContext(), JUMP_COMMON_PROBLEM_PAGE, false);
        boolean mainPageShowTag = SpUtils.getBoolean(MyApp.getContext(), MAIN_PAGE_WHETHER_SHOW, false);
        //当前设备语言
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, null);
        MessageBean bean = null;
        switch (message) {
            case "context.output.text":
                bean = new MessageBean();
                String txt = "";
                try {
                    JSONObject jo = new JSONObject(data);
                    txt = jo.optString("text", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bean.setText(txt);
                bean.setType(MessageBean.TYPE_OUTPUT);
                LogUtils.d(DEFAULT_LOG_TAG, "context.output.text = " + bean.getText());
                EventBus.getDefault().post(bean);
                break;
            case "context.input.text":
                bean = new MessageBean();
                ShowContent showContent = new ShowContent();
                try {
                    JSONObject jo = new JSONObject(data);
                    if (jo.has("var")) {
                        String var = jo.optString("var", "");
                        if (mIsFirstVar) {
                            mIsFirstVar = false;
                            mHasvar = true;
                            bean.setText(var);
                            showContent.setContent1(var);
                            bean.setType(MessageBean.TYPE_INPUT);
                        } else {
                            bean.setText(var);
                            showContent.setContent1(var);
                            bean.setType(MessageBean.TYPE_INPUT);
                        }
                    }
                    if (jo.has("text")) {
                        if (mHasvar) {
                            mHasvar = false;
                            mIsFirstVar = true;
                        }
                        String text = jo.optString("text", "");
                        bean.setText(text);
                        bean.setType(MessageBean.TYPE_INPUT);
                        showContent.setContent1(text);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LogUtils.d(DEFAULT_LOG_TAG, "context.input.text = " + bean.getText());
                if (startCommonProblemModelTag) {
                    EventBus.getDefault().post(showContent);
                } else {
                    EventBus.getDefault().post(bean);
                }
                if (currentLanguage != null && currentLanguage.equals("en") && bean.getText().length() > 3) {
                    commandDataEvent = new CommandDataEvent();
                    askAnswerDataEvent = new AskAnswerDataEvent();
                    CommonProblemEntity commonProblemEntity = CashUtils.getProblemOne(bean.getText().toLowerCase());
                    if (mainPageShowTag && commonProblemEntity != null) {
                        commandDataEvent.setCommandType(COMMON_PROBLEM_TAG);
                        commandDataEvent.setCommonProblemEntity(commonProblemEntity);
                        EventBus.getDefault().post(commandDataEvent);
                    } else {
                        if (commonProblemEntity != null) {  //英文识别常见问题
                            askAnswerDataEvent.setCommandType(COMMON_PROBLEM_TAG);
                            askAnswerDataEvent.setCommonProblemEntity(commonProblemEntity);
                            EventBus.getDefault().post(askAnswerDataEvent);
                        }
                    }
                    LogUtils.d(DEFAULT_LOG_TAG, "英文模式下问题识别 = " + bean.getText());
                }
                break;
            case "sys.dialog.state":
                EventBus.getDefault().post(new DialogStateEvent(data));
                break;
            case "sys.dialog.start":
                EventBus.getDefault().post(new RobotSignalEvent(RobotConfig.WAKE_WORD_DIALOG, ""));
                break;
            default:
        }
    }

}
