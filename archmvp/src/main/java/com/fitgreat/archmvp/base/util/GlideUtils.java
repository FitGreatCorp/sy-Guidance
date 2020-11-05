package com.fitgreat.archmvp.base.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

/**
 * Glide加载图片工具<p>
 *
 * @author zixuefei
 * @since 2019/5/10 16:56
 */
public class GlideUtils {

    public static void loadImage(Context context, String url, RequestOptions options, ImageView target) {
        Glide.with(context).asDrawable().load(url).apply(options).transition(new DrawableTransitionOptions().crossFade()).into(target);
    }

    public static void loadImage(Context context, String url, RequestOptions options, int crossduration, ImageView target) {
        Glide.with(context).asDrawable().load(url).apply(options).transition(new DrawableTransitionOptions().crossFade(crossduration)).into(target);
    }

    public static void loadImage(Context context, String url, ImageView target) {
        Glide.with(context).asDrawable().load(url).transition(new DrawableTransitionOptions().crossFade()).into(target);
    }

    public static void loadImage(Context context, String url, int crossduration, ImageView target) {
        Glide.with(context).asDrawable().load(url).transition(new DrawableTransitionOptions().crossFade(crossduration)).into(target);
    }

//    public static void loadLocalVideoImage(Context context, File url, ImageView target) {
//        Glide.with(context).asDrawable().load(url).apply(createRequestOptions(R.drawable.shape_video_default)).
//                transition(new DrawableTransitionOptions().crossFade(300)).into(target);
//    }
//
//    public static void loadVideoImage(Context context, String url, ImageView target) {
//        Glide.with(context).asDrawable().load(url).apply(createRequestOptions(R.drawable.shape_video_default)).
//                transition(new DrawableTransitionOptions().crossFade(300)).into(target);
//    }

    public static void loadCircleImage(Context mContext, String url, int errorResId, ImageView imageView) {
        Glide.with(mContext)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()).error(errorResId))
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    public static void loadRoundedCornersImage(Context mContext, String url, int radius, int errorResId, ImageView imageView) {
        Glide.with(mContext)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(radius)).error(errorResId))
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

//    public static void loadRoundedCornersImage(Context mContext, String url, int radius, ImageView imageView) {
//        Glide.with(mContext)
//                .load(url)
//                .apply(RequestOptions.bitmapTransform(new RoundedCorners(radius)).error(R.drawable.shape_video_default))
//                .transition(new DrawableTransitionOptions().crossFade())
//                .into(imageView);
//    }

//    public static void loadRoundedCornersImageForCenterCrop(Context mContext, String url, int radius, ImageView imageView) {
//        RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(radius))
//                .error(R.drawable.shape_video_default);
//        Glide.with(mContext)
//                .load(url)
//                .apply(requestOptions)
//                .transition(new DrawableTransitionOptions().crossFade())
//                .into(imageView);
//    }

    public static RequestOptions createRequestOptions(int error, int overrideW, int overrideH) {
        return new RequestOptions()
                .placeholder(error)
                .error(error)
                .override(overrideW, overrideH);
    }

    public static RequestOptions createRequestOptions(int error) {
        return new RequestOptions()
                .placeholder(error)
                .error(error);
    }

    //禁止内存缓存，减少内存溢出，增加圆角处理
    public static RequestOptions createRequestRadiusOptions(int error, int radius) {
        return RequestOptions
                .bitmapTransform(new RoundedCorners(radius))
                .error(error);
    }
}
