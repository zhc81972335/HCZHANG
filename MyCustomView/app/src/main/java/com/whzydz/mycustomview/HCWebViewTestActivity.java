package com.whzydz.mycustomview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceError;
import android.widget.ProgressBar;

public class HCWebViewTestActivity extends Activity {
    private HCWebView rv;
    private ProgressBar pb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hc_webview);

        pb = (ProgressBar)findViewById(R.id.pb);
        pb.setVisibility(View.GONE);

        rv = (HCWebView)findViewById(R.id.wv);
        rv.setListener(new HCWebView.ProcessListener() {
            @Override
            public void onStart() {
                pb.setProgress(0);
                Log.i("hczhang", "start");
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(int newProgress) {
                pb.setProgress(newProgress);
            }

            @Override
            public void onFinished() {
                Log.i("hczhang", "finished");
                pb.setProgress(100);
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onError(CharSequence error) {
                Log.i("hczhang", "error");
                pb.setVisibility(View.GONE);
            }
        });
        View v = LayoutInflater.from(this).inflate(R.layout.view_error, null);
        rv.setErrorView(v);
        rv.loadUrl("http://www.baidu.com");
    }

    @Override
    public void onBackPressed() {
        if(rv.canGoBack()) {
            rv.goBack();
        } else {
            super.onBackPressed();
        }

    }
}
