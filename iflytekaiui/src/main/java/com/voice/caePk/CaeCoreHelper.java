package com.voice.caePk;

import android.util.Log;

import com.iflytek.iflyos.cae.CAE;
import com.iflytek.iflyos.cae.ICAEListener;


public class CaeCoreHelper {
    final static String TAG = "CaeCoreHelper";
    //测试版key for 6mic
    private String osAutn = "dff33519-5e50-4aab-b1aa-99b9eef513b8";

    private boolean isUseOsV2 = false;

    private OnCaeOperatorlistener caeOperatorlistener;

    public CaeCoreHelper(OnCaeOperatorlistener listener, boolean is2Mic) {
        this.caeOperatorlistener = listener;
        EngineInit(is2Mic);
    }


    public boolean EngineInit(boolean is2MIc) {
        boolean rst = false;
        Log.d(TAG, "EngineInit in");
        CAE.loadLib();

        int isInit = CAE.CAENew(VoiceFileUtil.getModeFilePath(), mCAEListener);

        //iflyos 正式版本
        //int isInit = CAE.CAENew(mResPath,hlw,mCAEListener);
        String ver = CAE.CAEGetVersion();
        Log.d(TAG, "EngineInit  result:  " + isInit + "version:" + ver);
        rst = isInit == 0;
        CAE.CAEAuth(osAutn);
        CAE.CAESetShowLog(0);
        return rst;
    }

    public void reLoadResource(String modeFilePath) {
        CAE.CAEReloadResource(modeFilePath);
    }

    //送入原始音频到算法中
    public void writeAudio(byte[] audio) {
        CAE.CAEAudioWrite(audio, audio.length);
    }

    //重置引擎，需要初始化引擎
    public void ResetEngine() {

    }

    public void DestoryEngine() {
        CAE.CAEDestory();
    }

    //os yue kuo banben
    private ICAEListener mCAEListener = new ICAEListener() {
        @Override
        public void onWakeup(final int angle, final int beam) {
            CAE.CAESetRealBeam(beam);
            if (caeOperatorlistener != null) {

                caeOperatorlistener.onWakeup(angle, beam);
            }
        }

        @Override
        public void onAudioCallback(byte[] audioData, int dataLen) {
            //Log.d(TAG,"onAudioCallback  "+dataLen);
            if (caeOperatorlistener != null) {
                caeOperatorlistener.onAudio(audioData, dataLen);
            }
        }

        @Override
        public void onIvwAudioCallback(byte[] bytes, int i) {

        }
    };


  /*iflyos banben
    private ICAEListener mCAEListener = new ICAEListener() {
        @Override
        public void onWakeup(float power, int angle, int beam) {
            CAE.CAESetRealBeam(beam);
            if (caeOperatorlistener!=null){

                caeOperatorlistener.onWakeup(angle,beam);
            }
        }

        @Override
        public void onAudioCallback(byte[] audioBuffer, int length) {
            if (caeOperatorlistener!=null) {
                caeOperatorlistener.onAudio(audioBuffer, length);
            }
        }

        @Override
        public void onIvwAudioCallback(byte[] audioBuffer, int length) {

        }
    };
    */

}
