package com.geek.webpage.web;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.apkfuns.jsbridge.JsBridge;
import com.geek.webpage.web.coolindicator.CoolIndicator;

/**
 * 代码描述<p>
 *
 * @author anhuiqing
 * @since 2019/6/6 13:28
 */
public class LWWebviewClient extends WebViewClient {
    private Context mContext;
    private WebViewListener mWebViewListener;
    private JsBridge mJsBridge;
    private CoolIndicator mCoolIndicator;

    public LWWebviewClient(Context context, WebViewListener webViewListener, JsBridge jsBridge, CoolIndicator coolIndicator) {
        this.mContext = context;
        this.mWebViewListener = webViewListener;
        this.mJsBridge = jsBridge;
        this.mCoolIndicator = coolIndicator;
    }

    private Uri stringToUrl(String string) {
        try {
            return Uri.parse(string);
        } catch (Exception e) {
            return null;
        }
    }

    public void setWebViewListener(WebViewListener webViewListener) {
        this.mWebViewListener = webViewListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("WebViewClient", " url   ---- >" + url);
        if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith("sms:") ||
                url.startsWith(WebView.SCHEME_MAILTO) || url.startsWith("weixin://wap/pay?")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String
            description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        if (mWebViewListener != null) {
            mWebViewListener.onError(view, errorCode, description, failingUrl);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                   SslError error) {
        super.onReceivedSslError(view, handler, error);
        handler.proceed(); // 接受所有证书
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d("WebViewClient", " url   ---- >start    " + url);
        super.onPageStarted(view, url, favicon);
        if (mCoolIndicator != null) {
            mCoolIndicator.start();
        }

        if (mWebViewListener != null) {
            mWebViewListener.onPageStart();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (request.isForMainFrame()) {
            if (mWebViewListener != null) {
                mWebViewListener.onError(view);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        if (errorResponse.getStatusCode() == 404 || errorResponse.getStatusCode() == 500) {
            if (mWebViewListener != null) {
                mWebViewListener.onError(view);
            }
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (mWebViewListener != null) {
            mWebViewListener.onFinish();
        }
        if (mJsBridge != null) {
            mJsBridge.injectJs(view);
        }
        if (mCoolIndicator != null) {
            mCoolIndicator.complete();
        }
    }
}
