package com.example.webviewtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String androidCurrent = "Linux; U; Android " + Build.VERSION.RELEASE;
    private static final String chrome = "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";



    private static final String browser = chrome;
    private static final String device = androidCurrent;
    private static final String userAgent = "Mozilla/5.0 (" + device + ") " + browser;

    private static final String CAMERA_PERMISSION =  "android.permission.CAMERA";
    private static final String AUDIO_PERMISSION =  "android.permission.RECORD_AUDIO";
    private static final String STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String[] VIDEO_PERMISSION = {CAMERA_PERMISSION, AUDIO_PERMISSION};

    private static final String url = "https://web.whatsapp.com/\uD83C\uDF10/"+ Locale.getDefault().getLanguage();

    private static final int FILECHOOSER_RESULTCODE = 200;
    private static final int CAMERA_PERMISSION_RESULTCODE = 201;
    private static final int AUDIO_PERMISSION_RESULTCODE = 202;
    private static final int VIDEO_PERMISSION_RESULTCODE = 203;
    private static final int STORAGE_PERMISSION_RESULTCODE = 204;

    private static final String DEBUG_TAG = "WAWEBTOGO";

    private ViewGroup mainView;

    private long lastTouchClick = 0;
    private long lastBackClick = 0;
    private float lastXClick = 0;
    private float lastYClick = 0;

    boolean keyboardEnabled;
    Toast clickReminder = null;

    private SharedPreferences prefs;

    private final Activity activity = this;

    private ValueCallback<Uri[]> mUploadMessage;
    private PermissionRequest currentPermissionRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        WebView webView = findViewById(R.id.webView);
//        webView.setWebViewClient(new NoRedirectWebViewClient());
//
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//
//        // Set a WebViewClient to handle redirects
//
//        webView.loadUrl("https://web.whatsapp.com/");
//        // Set the user agent to mimic a desktop browser
        String desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
//        webSettings.setUserAgentString(desktopUserAgent);


        WebView   web = (WebView)findViewById(R.id.webView);

        WebSettings webSettings = web.getSettings();
        // load url in browser
        web.loadUrl(url);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setDisplayZoomControls(false);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        web.setScrollbarFadingEnabled(false);
        web.getSettings().setJavaScriptEnabled(true); //for wa web
        web.getSettings().setAllowContentAccess(true); // for camera
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setAllowFileAccessFromFileURLs(true);
        web.getSettings().setAllowUniversalAccessFromFileURLs(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false); //for audio messages
        web.getSettings().setDomStorageEnabled(true); //for html5 app
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setAppCacheEnabled(true); // app cache
        //web.getSettings().setAppCachePath(getCacheDir().getAbsolutePath()); //app cache
        web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); //app cache
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        web.setScrollbarFadingEnabled(true);
        web.getSettings().setUserAgentString(desktopUserAgent);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setDisplayZoomControls(false);
        web.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (request.getResources()[0].equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED
                            && ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, VIDEO_PERMISSION, VIDEO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    } else {
                        request.grant(request.getResources());
                    }
                } else if (request.getResources()[0].equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    if (ContextCompat.checkSelfPermission(activity, AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{AUDIO_PERMISSION}, AUDIO_PERMISSION_RESULTCODE);
                        currentPermissionRequest = request;
                    }
                } else {
                    try {
                        request.grant(request.getResources());
                    } catch (RuntimeException e) {
                        Log.d(DEBUG_TAG, "Granting permissions failed", e);
                    }
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessage = filePathCallback;
                Intent chooserIntent = fileChooserParams.createIntent();
                MainActivity.this.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                return true;
            }
        });


        //Webview client for Webview
        web.setWebViewClient(new WebViewClient(){

        });

    }
    private static class NoRedirectWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Return true to indicate that the WebView should handle the URL loading
            // If you want to block the redirect, return true; otherwise, return false
            return true;
        }
    }
}