package com.voice.caePk;

import android.util.Log;

import com.iflytek.alsa.AlsaRecorder;

import org.json.JSONObject;

/**
 * rk3288
 */

public class CaeOperator {

    private static final String TAG = CaeOperator.class.getSimpleName();

    // pcm录音设备号，根据实际情况设置
    //tinycap /sdcard/test.pcm -D 0 -d 0 -c 4 -r 48000 -b 32 -p 768 -n 10

    /**
     * pcm 声卡号 -D
     */
    //  private final static int mPcmCard = 3 ;//usb snd
    private final static int mPcmCard = 2;

    /**
     * pcm 声卡设备号
     */
    private final static int mPcmDevice = 0;
    /**
     * 通道数量
     */
    private final static int mPcmChannel = 8;//6mic
    //  private final static int mPcmChannel = 4;//2mic
    /**
     * 采样率
     */
    private final static int mPcmSampleRate = 16000;
    /**
     * 一次中断的帧数 一般不同修改，某些不支持这么大数字时会报错，可以尝试减小改值，例如 1023
     */
    //  private final static int mPcmChannel = 4;//2mic

    private final static int mPcmPeriodSize = 1536;
    /**
     * 周期数 一般不同修改
     */
    private final static int mPcmPeriodCount = 8;
    /**
     * pcm 位宽 0-PCM_FORMAT_S16_LE、<br>1-PCM_FORMAT_S32_LE、<br>2-PCM_FORMAT_S8、<br>3-PCM_FORMAT_S24_LE、<br>4-PCM_FORMAT_MAX
     */
    private final static int mPcmFormat = 0;

    private AlsaRecorder mAlsaRecorder;

    // 是否魏2mic 算法库
    private boolean is2Mic = false;

    // 唤醒成功后抛出的音频保存路径
    String mAsrAudioDir = "/sdcard/airface/CAEAsrAudio/";

    // 多通道原始音频保存路径
    String mRawAudioDir = "/sdcard/airface/CAERawAudio/";
    private String mPcmAudioDir = "/sdcard/airface/PcmAudio/";  // 从alsa里面录到的音频

    private PcmFileUtil mPcmFileUtil;

    private CaeCoreHelper mcaeCoreHelper;

    private PcmFileUtil mAsrFileUtil;  // 保存唤醒降噪后录音

    private PcmFileUtil mRawFileUtil;   // 保存多通道原始录音数据

    JSONObject mSyn = new JSONObject();

    //原始音频保存，true：保存音频
    public boolean isSaveAudio = false;

    //mic数量
    public enum MicType {
        DulMic,//2mic
        FMic,//4mic
        FMic32Bit,
        SMic//6mic
    }

    //阵列类型默认为6mic
    MicType micType = MicType.SMic;
    // 音频监听器

    AlsaRecorder.PcmListener mPcmListener = new AlsaRecorder.PcmListener() {
        @Override
        public void onPcmData(byte[] bytes, int i) {
            //  Log.d(TAG,"bytes  lenght="+String.valueOf(bytes.length));

            if (isSaveAudio) {
                mPcmFileUtil.write(bytes, 0, bytes.length);
            }
            byte[] data = null;
            if (MicType.DulMic == micType) {//2mic
                data = addCnFor2Mic(bytes);
            } else if (MicType.SMic == micType) {//6mic
                data = addCnForMutiMic(bytes);

            } else if (MicType.FMic == micType) {//4mic 16bits

                data = adapeter4Mic(bytes);
            } else if (MicType.FMic32Bit == micType) {//4MIC 32bits
                data = adapeter4Mic32bit(bytes);
            }


            if (null != mcaeCoreHelper) {
                mcaeCoreHelper.writeAudio(data);
                if (isSaveAudio && mRawFileUtil != null) {
                    mRawFileUtil.write(data, 0, data.length);
                }
            }
        }
    };

    OnCaeOperatorlistener mOnCaeOperatorlistener = new OnCaeOperatorlistener() {
        @Override
        public void onAudio(byte[] audioData, int dataLen) {

            if (isSaveAudio) {//保存降噪后音频
                mAsrFileUtil.write(audioData, 0, audioData.length);
            }


            if (null != caeListenner) {

                caeListenner.onAudio(audioData, dataLen);

            }

        }

        @Override
        public void onWakeup(int angle, int beam) {
            if (null != caeListenner) {
                caeListenner.onWakeup(angle, beam);
            }
        }
    };

    OnCaeOperatorlistener caeListenner;

    public void initCAEInstance(OnCaeOperatorlistener onCaeOperatorlistener) {
        Log.d(TAG, "initCAEInstance   ");
        caeListenner = onCaeOperatorlistener;
        mcaeCoreHelper = new CaeCoreHelper(mOnCaeOperatorlistener, is2Mic);
        Log.d(TAG, "createInstance AlsaRecorder");
        //开始创建录音声卡实例
        mAlsaRecorder = AlsaRecorder.createInstance(mPcmCard, mPcmDevice, mPcmChannel, mPcmSampleRate,
                mPcmPeriodSize, mPcmPeriodCount, mPcmFormat);
        mAlsaRecorder.setLogShow(false);

        Log.d(TAG, "initCaeInstance success ...");
    }

    public boolean startrecord() {
        if (mAlsaRecorder != null) {
            if (0 == mAlsaRecorder.startRecording(mPcmListener)) {
                Log.i(TAG, " start  recording sucess...");

                // if(isSaveAudio) {
                //降噪唤醒后的识别音频
                mAsrFileUtil = new PcmFileUtil(mAsrAudioDir);
                //原始音频
                mRawFileUtil = new PcmFileUtil(mRawAudioDir);


                //从设备中获取到的数据
                mPcmFileUtil = new PcmFileUtil(mPcmAudioDir);

                mAsrFileUtil.createPcmFile();
                mPcmFileUtil.createPcmFile();
                mRawFileUtil.createPcmFile();

                //   }
            } else {
                Log.i(TAG, "start recording fail...");
                return false;
            }


        } else {
            Log.d(TAG, "AlsaRecorder is null ..  .");
            return false;

        }
        return true;
    }


    public void stopRecord() {
        if (mAlsaRecorder != null)
            mAlsaRecorder.stopRecording();

        if (isSaveAudio) {
            mAsrFileUtil.closeWriteFile();
            mRawFileUtil.closeWriteFile();

            mPcmFileUtil.closeWriteFile();
        }
        Log.d(TAG, "stopRecd ok...");
    }

    public void restCaeEngine() {
        if (null != mcaeCoreHelper) {
            mcaeCoreHelper.ResetEngine();
        }
    }

    public void releaseCae() {
        if (isSaveAudio) {
            mAsrFileUtil.closeWriteFile();
            mRawFileUtil.closeWriteFile();
            mPcmFileUtil.closeWriteFile();
        }
        if (mAlsaRecorder != null) {
            mAlsaRecorder.destroy();
        }
        if (mcaeCoreHelper != null) {
            mcaeCoreHelper.DestoryEngine();
            mcaeCoreHelper = null;
        }
    }


    //6mic 通道号添加 8ch 32bits
    private byte[] addCnForMutiMic(byte[] data) {
        int datasize = data.length;
        byte[] newdata = new byte[datasize * 2];// 乘以2是数据从16bit变为32bit；
        int j = 0;
        int k = 0;
        int index = 0;
        int step = datasize / 2;

        while (j < step) {// 除以2是两个字节作为一组数据，进行添加通道号处理；
            for (int i = 1; i < 9; i++) {
                k = 4 * j;
                index = 2 * j;
                newdata[k] = 00;
                newdata[k + 1] = (byte) i;
                newdata[k + 2] = data[index];
                newdata[k + 3] = data[index + 1];
                j++;
            }

        }
        data = null;
        return newdata;
    }

    //4mic通道适配,输入8通道数据，适配成6通道数据
    private byte[] adapeter4Mic(byte[] data) {
        //  int size = ((data.length/8)*2)*6;
        int size = (data.length / 8) * 6;
        byte[] cpy = new byte[size];
        int j = 0;

        while (j < data.length / 16) {

            cpy[12 * j + 0] = data[16 * j + 0];
            cpy[12 * j + 1] = data[16 * j + 1];

            cpy[12 * j + 2] = data[16 * j + 2];
            cpy[12 * j + 3] = data[16 * j + 3];


            cpy[12 * j + 4] = data[16 * j + 4];
            cpy[12 * j + 5] = data[16 * j + 5];


            cpy[12 * j + 6] = data[16 * j + 6];
            cpy[12 * j + 7] = data[16 * j + 7];

            //通道7--》ref1
            cpy[12 * j + 8] = data[16 * j + 12];
            cpy[12 * j + 9] = data[16 * j + 13];

            //通道8 --》 ref2
            cpy[12 * j + 10] = data[16 * j + 14];
            cpy[12 * j + 11] = data[16 * j + 15];

            j++;
        }
        return cpy;
    }

    //4mic通道适配,输入8通道数据，适配成6通道数据
    private byte[] adapeter4Mic32bit(byte[] data) {
        //  int size = ((data.length/8)*2)*6;
        int size = (data.length / 8) * 6 * 2;

        byte[] cpy = new byte[size];
        int j = 0;

        while (j < data.length / 16) {

            cpy[24 * j + 0] = 0x00;
            cpy[24 * j + 1] = 0x01;
            cpy[24 * j + 2] = data[16 * j + 0];
            cpy[24 * j + 3] = data[16 * j + 1];


            cpy[24 * j + 4] = 0x00;
            cpy[24 * j + 5] = 0x02;
            cpy[24 * j + 6] = data[16 * j + 2];
            cpy[24 * j + 7] = data[16 * j + 3];


            cpy[24 * j + 8] = 0x00;
            cpy[24 * j + 9] = 0x03;
            cpy[24 * j + 10] = data[16 * j + 4];
            cpy[24 * j + 11] = data[16 * j + 5];


            cpy[24 * j + 12] = 0x00;
            cpy[24 * j + 13] = 0x04;
            cpy[24 * j + 14] = data[16 * j + 6];
            cpy[24 * j + 15] = data[16 * j + 7];

            //通道7--》ref1
            cpy[24 * j + 16] = 0x00;
            cpy[24 * j + 17] = 0x05;
            cpy[24 * j + 18] = data[16 * j + 12];
            cpy[24 * j + 19] = data[16 * j + 13];

            //通道8 --》 ref2
            cpy[24 * j + 20] = 0x00;
            cpy[24 * j + 21] = 0x06;
            cpy[24 * j + 22] = data[16 * j + 14];
            cpy[24 * j + 23] = data[16 * j + 15];

            j++;
        }
        return cpy;
    }

    //2mic通道适配
    private byte[] addCnFor2Mic(byte[] data) {
        byte[] cpy = new byte[data.length * 2];
        int j = 0;

        //通道： mic1 mic2 ref ref
        while (j < data.length / 8) {
            cpy[16 * j] = 00;
            cpy[16 * j + 1] = (byte) 1;
            cpy[16 * j + 2] = data[8 * j + 0];
            cpy[16 * j + 3] = data[8 * j + 1];

            cpy[16 * j + 4] = 00;
            cpy[16 * j + 5] = (byte) 2;
            cpy[16 * j + 6] = data[8 * j + 2];
            cpy[16 * j + 7] = data[8 * j + 3];

            cpy[16 * j + 8] = 00;
            cpy[16 * j + 9] = (byte) 3;
            cpy[16 * j + 10] = data[8 * j + 4];
            cpy[16 * j + 11] = data[8 * j + 5];

            cpy[16 * j + 12] = 00;
            cpy[16 * j + 13] = (byte) 4;
            cpy[16 * j + 14] = data[8 * j + 6];
            cpy[16 * j + 15] = data[8 * j + 7];

            j++;
        }
        return cpy;
    }


}
