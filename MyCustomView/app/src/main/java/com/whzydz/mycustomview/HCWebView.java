package com.whzydz.mycustomview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HCWebView extends WebView{
    private boolean supportZoom;
    private Boolean autoLoadImg;

    private ProcessListener listener;
    private View errorView;

    public HCWebView(Context context) {
        this(context, null);
    }

    public HCWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HCWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义属性的值
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HCWebView);
        supportZoom = ta.getBoolean(R.styleable.HCWebView_support_zoom, false);
        autoLoadImg = ta.getBoolean(R.styleable.HCWebView_auto_load_img, true);

        init();
    }

    private void init() {
        config();
        setWebViewClient(new MyWebviewClient());
        setWebChromeClient(new MyWebChromeClient());
    }

    private void config() {
        WebSettings settings = this.getSettings();
        //缩放设置
        settings.setSupportZoom(supportZoom);
        //调整图片至适合webview的大小
        settings.setUseWideViewPort(true);
        // 缩放至屏幕的大小
        settings.setLoadWithOverviewMode(true);
        //设置默认编码
        settings.setDefaultTextEncodingName("utf-8");
        //设置自动加载图片
        settings.setLoadsImagesAutomatically(autoLoadImg);
        //开启javascript
        settings.setJavaScriptEnabled(true);
        //提高渲染的优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //支持内容重新布局
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //关闭webview中缓存
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    private final class MyWebviewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if(listener != null) {
                listener.onStart();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(listener != null) {
                listener.onFinished();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            //隐藏WebView，显示重新获取数据的提示图
            String data = "";
            view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
            showErrorView();
            if(listener != null) {
                listener.onError(description);
            }
        }
    }

    private void showErrorView() {
        ViewGroup parent = (ViewGroup)getParent();
        if(errorView != null) {
            parent.addView(errorView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        setVisibility(View.GONE);
    }

    private void hideErrorView() {
        ViewGroup parent = (ViewGroup)getParent();
        if(errorView != null) {
            parent.removeView(errorView);
        }
        setVisibility(View.VISIBLE);
    }

    private final class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if(listener != null) {
                listener.onProgressChanged(newProgress);
            }
        }
    }

    public void setListener(ProcessListener listener) {
        this.listener = listener;
    }

    public void setErrorView(View errorView) {
        this.errorView = errorView;
        this.errorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadUrl();
            }
        });
    }

    private void reloadUrl() {
        hideErrorView();
        reload();
    }

    public static interface ProcessListener {
        public void onStart();
        public void onProgressChanged(int newProgress);
        public void onFinished();
        public void onError(CharSequence error);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoading();
    }
}
