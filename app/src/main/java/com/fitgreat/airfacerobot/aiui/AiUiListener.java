package com.fitgreat.airfacerobot.aiui;

import android.text.TextUtils;

import com.fitgreat.airfacerobot.launcher.model.IflytekAnswerData;
import com.fitgreat.archmvp.base.util.JsonUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * 讯飞语音识别结果回调处理<p>
 *
 * @author zixuefei
 * @since 2020/4/9 0009 16:59
 */
public class AiUiListener implements AIUIListener {
    private final String TAG = AiUiListener.class.getSimpleName();
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    private AiuiManager aiuiManager;

    public AiUiListener(AiuiManager aiuiManager) {
        this.aiuiManager = aiuiManager;
    }

    @Override
    public void onEvent(AIUIEvent event) {
        LogUtils.d(TAG, "event.eventTyp = " + event.eventType);
        switch (event.eventType) {
            case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                LogUtils.d(TAG, "已连接服务器");
                break;
            case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                LogUtils.d(TAG, "与服务器断连");
                break;
            case AIUIConstant.EVENT_WAKEUP:
                LogUtils.d(TAG, "EVENT_WAKEUP: 进入识别状态");
                if (aiuiManager != null) {
                    aiuiManager.onPlayLineTTS("我在呢");
                }
                IflytekAnswerData wake = new IflytekAnswerData();
                wake.setText("我在呢");
                EventBus.getDefault().post(wake);
                break;
            case AIUIConstant.EVENT_SLEEP:
                LogUtils.d(TAG, "EVENT_SLEEP 进入睡眠状态");
//                if (aiuiManager != null) {
//                    aiuiManager.onPlayLineTTS("有事再叫我");
//                }
//                EventBus.getDefault().post(new VoiceEvent("answer", "有事再叫我"));
                break;
            case AIUIConstant.EVENT_TTS:
//                LogUtils.d(TAG, "云端TTS PLAY");

                break;
            case AIUIConstant.EVENT_RESULT:
                try {
                    JSONObject bizParamJson = new JSONObject(event.info);
                    JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                    JSONObject params = data.getJSONObject("params");
                    JSONObject content = data.getJSONArray("content").getJSONObject(0);
                    LogUtils.d(TAG, "aiui Result:" + content);

                    if (content.has("cnt_id")) {
                        String cnt_id = content.getString("cnt_id");
                        String cntStr = new String(event.data.getByteArray(cnt_id), StandardCharsets.UTF_8);
                        // 获取该路会话的id，将其提供给支持人员，有助于问题排查
                        // 也可以从Json结果中看到
                        String sid = event.data.getString("sid");
                        String tag = event.data.getString("tag");

                        // 获取从数据发送完到获取结果的耗时，单位：ms
                        // 也可以通过键名"bos_rslt"获取从开始发送数据到获取结果的耗时
                        long eosRsltTime = event.data.getLong("eos_rslt", -1);
                        if (TextUtils.isEmpty(cntStr)) {
                            return;
                        }

                        JSONObject cntJson = new JSONObject(cntStr);
                        String sub = params.optString("sub");
                        LogUtils.d(TAG, "sub = :" + sub);
                        if ("nlp".equals(sub)) {
                            // 解析得到语义结果
                            String resultStr = cntJson.optString("intent");
                            //语义解析
                            if (!TextUtils.isEmpty(resultStr) && resultStr.length() > 300) {
                                LogUtils.d(TAG, "whole answer:" + resultStr.substring(0, resultStr.length() / 3));
                                LogUtils.d(TAG, "whole answer:" + resultStr.substring(resultStr.length() / 3, resultStr.length() * 2 / 3));
                                LogUtils.d(TAG, "whole answer:" + resultStr.substring(resultStr.length() * 2 / 3));
                            } else {
                                LogUtils.d(TAG, "whole answer:" + resultStr);
                            }
                            JSONObject answerJson = new JSONObject(resultStr);
                            String answer = answerJson.optString("answer");
                            LogUtils.d(TAG, "answer:" + answer);
                            IflytekAnswerData iflytekAnswerData = JsonUtils.decode(answer, IflytekAnswerData.class);
                            if (iflytekAnswerData != null) {
                                if (aiuiManager != null) {
                                    aiuiManager.onPlayLineTTS(iflytekAnswerData.getText());
                                }
                                LogUtils.d(TAG, "answer text:" + iflytekAnswerData.getText());
                            }
                        } else if ("tts".equals(sub)) {
                            sid = event.data.getString("sid");
                            cnt_id = content.getString("cnt_id");
                            byte[] audio = event.data.getByteArray(cnt_id); //合成音频数据
                            /**
                             *
                             * 音频块位置状态信息，取值：
                             * - 0（合成音频开始块）
                             * - 1（合成音频中间块，可出现多次）
                             * - 2（合成音频结束块)
                             * - 3（合成音频独立块,在短合成文本时出现）
                             *
                             * 举例说明：
                             * 一个正常语音合成可能对应的块顺序如下：
                             *   0 1 1 1 ... 2
                             * 一个短的语音合成可能对应的块顺序如下:
                             *   3
                             **/
                            int dts = content.getInt("dts");
                            int frameId = content.getInt("frame_id");// 音频段id，取值：1,2,3,...
                            int percent = event.data.getInt("percent"); //合成进度
                            boolean isCancel = "1".equals(content.getString("cancel"));  //合成过程中是否被取消

                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case AIUIConstant.EVENT_ERROR:
                LogUtils.e(TAG, "错误: " + event.arg1 + "\n" + event.info);
                break;
            case AIUIConstant.EVENT_VAD: {
                if (AIUIConstant.VAD_BOS == event.arg1) {
                    //setText("找到vad_bos");
                } else if (AIUIConstant.VAD_EOS == event.arg1) {
                    //setText("找到vad_eos");
                } else {//录音音量
                    //showTip("" + event.arg2);
                }
            }
            break;
            case AIUIConstant.EVENT_START_RECORD:
                //setText("已开始录音");
                break;
            case AIUIConstant.EVENT_STOP_RECORD:
                //setText("已停止录音");
                break;
            // 状态事件
            case AIUIConstant.EVENT_STATE:
                LogUtils.d(TAG, "STATE:" + event.arg1);
                setAIUIState(event.arg1);
                if (AIUIConstant.STATE_IDLE == mAIUIState) {
                    // 闲置状态，AIUI未开启
                    //	showTip("STATE_IDLE");
                } else if (AIUIConstant.STATE_READY == mAIUIState) {
                    // AIUI已就绪，等待唤醒
                    //showTip("STATE_READY");
                } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                    // AIUI工作中，可进行交互
                    //showTip("STATE_WORKING");
                }
                break;
            case AIUIConstant.EVENT_CMD_RETURN:
                break;
            default:
                break;
        }
    }

    public int getAIUIState() {
        return mAIUIState;
    }

    public void setAIUIState(int mAIUIState) {
        this.mAIUIState = mAIUIState;
    }
}
