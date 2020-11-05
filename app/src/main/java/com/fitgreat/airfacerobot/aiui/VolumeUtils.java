package com.fitgreat.airfacerobot.aiui;

import android.content.Context;
import android.media.AudioManager;

import com.fitgreat.archmvp.base.util.LogUtils;

/**
 * 语音相关工具<p>
 *
 * @author zixuefei
 * @since 2020/3/25 0025 16:50
 */
public class VolumeUtils {
    private final static String TAG = VolumeUtils.class.getSimpleName();

    public static void upVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        LogUtils.d(TAG, "max =" + max + ",current = " + current);

        if (current < max) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current + 1, AudioManager.FLAG_SHOW_UI);
        }
    }

    public static void downVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        LogUtils.d(TAG, "max =" + max + ",current = " + current);

        if (current < max) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current + 1, AudioManager.FLAG_SHOW_UI);
        }
    }
}
