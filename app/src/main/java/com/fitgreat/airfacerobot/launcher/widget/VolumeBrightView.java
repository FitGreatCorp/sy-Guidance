package com.fitgreat.airfacerobot.launcher.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.archmvp.base.util.LogUtils;


import java.math.BigDecimal;

public class VolumeBrightView extends LinearLayout {

    private VerticalSeekBar seekBarVolume;
    private VerticalSeekBar seekBarBright;
    private AudioManager audioManager = null;
    private LinearLayout itemVolume;
    private LinearLayout itemBright;
    private static final String TAG = "VolumeBrightView";
    private ContentResolver contentResolver;

    public VolumeBrightView(Context context) {
        super(context);
        initView(context);
    }

    public VolumeBrightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VolumeBrightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        //音频初始化
        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        //获取系统最大音量
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //获取系统当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //初始化view
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_volume_bright, this);
        seekBarVolume = (VerticalSeekBar) inflate.findViewById(R.id.sb_volume);
        itemVolume = (LinearLayout) inflate.findViewById(R.id.item_volume);
        //初始化设置音量
        seekBarVolume.setProgress(currentVolume);
        seekBarVolume.setMaxProgress(maxVolume);
        seekBarVolume.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
            @Override
            public void onStart(VerticalSeekBar slideView, int progress) {
            }

            @Override
            public void onProgress(VerticalSeekBar slideView, int progress) {
            }

            @Override
            public void onStop(VerticalSeekBar slideView, int progress) {
                LogUtils.d(TAG, "onStop , " + " 当前音量进度进度 " + progress);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);
            }
        });
        seekBarBright = (VerticalSeekBar) inflate.findViewById(R.id.sb_bright);
        itemBright = (LinearLayout) inflate.findViewById(R.id.item_bright);
        //设置屏幕亮度调解模式为手动模式
        contentResolver = context.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
            //获取当前屏幕亮度
            int currentBright = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
            LogUtils.d(TAG, "currentBright , " + " 当前屏幕亮度 " + currentBright);
            seekBarBright.setMaxProgress(255);
            seekBarBright.setProgress(currentBright);
            seekBarBright.setOnSlideChangeListener(new VerticalSeekBar.SlideChangeListener() {
                @Override
                public void onStart(VerticalSeekBar slideView, int progress) {

                }

                @Override
                public void onProgress(VerticalSeekBar slideView, int progress) {

                }

                @Override
                public void onStop(VerticalSeekBar slideView, int progress) {
                    LogUtils.d(TAG, "onStop , " + " 当前屏幕亮度进度 " + progress);
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, progress);
                }
            });
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void displayVolume(int volume) {
        itemVolume.setVisibility(VISIBLE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
        seekBarVolume.setProgress(volume);
    }

    public void hiddenVolume() {
        itemVolume.setVisibility(GONE);
    }

    public void displayBright(int bright) {
        itemBright.setVisibility(VISIBLE);
        seekBarBright.setProgress(bright);
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, bright);
    }

    public void hiddenBright() {
        itemBright.setVisibility(GONE);
    }

    public interface VolumeChangeListener {
        void volumeChange(int currentProgress);
    }

    public interface BrightChangeListener {
        void brightChange(int currentProgress);
    }

    /**
     * 设置音量变化监听
     *
     * @param volumeListener
     */
    public void setVolumeListener(VolumeChangeListener volumeListener) {

    }

    /**
     * 设置亮度变化监听
     *
     * @param brightListener
     */
    public void setBrightListener(BrightChangeListener brightListener) {

    }
}
