package com.fastlib.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.UnsupportedEncodingException;

/**
 * Created by sgfb on 16/9/29.
 * 简易webview模块
 */
public abstract class AbsWebViewActivity extends AppCompatActivity {
    public static final String ARG_URL = "URL";
    public static final String ARG_TITLE = "title";
    public static final String ARG_DATA = "data"; //本地html数据

    protected WebView mWebView;
    protected ProgressBar mProgress;
    protected String mUrl;

    public abstract void webTitle(String title);

    protected void init(int webViewId, int progressId) throws UnsupportedEncodingException {
        mWebView = (WebView) findViewById(webViewId);
        mProgress = (ProgressBar) findViewById(progressId);
        mUrl = getIntent().getStringExtra(ARG_URL);
        String data = getIntent().getStringExtra(ARG_DATA);
        String title = getIntent().getStringExtra(ARG_TITLE);

        webTitle(title);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebClient());
        mWebView.setWebChromeClient(new ChromeClient());
        if (TextUtils.isEmpty(data)) mWebView.loadUrl(mUrl);
        else mWebView.loadData(data, "text/html;charset=UTF-8", null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected class ChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgress.setProgress(newProgress);
            if (newProgress >= 100)
                mProgress.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            webTitle(title);
            mProgress.setVisibility(View.VISIBLE);
            mProgress.setProgress(20);
        }
    }

    protected class WebClient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            if(url.startsWith("tel:")){
                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            System.out.println("page start:"+url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url){
            System.out.println("page end:"+url);
            super.onPageFinished(view, url);
        }
    }
}
