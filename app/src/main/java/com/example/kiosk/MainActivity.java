package com.example.kiosk;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.PosPrinterDev;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static IMyBinder binder;
    public static PosPrinterDev.PortType portType;
    static boolean isPrinterConnected = false;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (IMyBinder) iBinder;
            Log.e("binder", "connected");
            UsbPrinterConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("binder", "disconnected");
        }
    };
    private String URL;
    private SharedPreferences sharedPreferences;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

        initialize();

        sharedPreferences = getBaseContext().getSharedPreferences("com.example.kiosk.PREFS", MODE_PRIVATE);

        String apiUrl = sharedPreferences.getString("apiUrl", getResources().getString(R.string.url_default));
        String ui = sharedPreferences.getString("ui", getResources().getString(R.string.ui_default));
        String apiVer = sharedPreferences.getString("apiVer", "");
        String apiKey = sharedPreferences.getString("apiKey", "");

        URL = apiUrl + "/modules/" + ui + ".php?v=" + apiVer + "&ak=" + apiKey;

//        Sentry.capture("URL: " + URL);

        if (getIsFirstStart()) {
            startActivity(new Intent(MainActivity.this, ConfigActivity.class));
        } else {
            Log.e("PREFS", sharedPreferences.getAll().toString());
            setUpWebView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialize();
        setUpWebView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
//            if (binder != null) {
//                binder.disconnectCurrentPort(new UiExecute() {
//                    @Override
//                    public void onsucess() {
//
//                    }
//
//                    @Override
//                    public void onfailed() {
//
//                    }
//                });
//            }
//            unbindService(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void setPortType(PosPrinterDev.PortType portType) {
        this.portType = portType;
    }

    public void UsbPrinterConnect() {
        List<String> usbList = PosPrinterDev.GetUsbPathNames(getApplicationContext());

        if (usbList != null) {
            String usbDevice = usbList.get(0);

            try {
                binder.connectUsbPort(getApplicationContext(), usbDevice, new UiExecute() {
                    @Override
                    public void onsucess() {
                        isPrinterConnected = true;
                        setPortType(PosPrinterDev.PortType.USB);
                    }

                    @Override
                    public void onfailed() {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    private boolean getIsFirstStart() {
        return sharedPreferences.getBoolean("isFirstStart", true);
    }

    private void setUpWebView() {
        mWebView = findViewById(R.id.activity_main_webview);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());

        WebSettings mWebSettings = mWebView.getSettings();

        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setDisplayZoomControls(false);
        mWebSettings.setBuiltInZoomControls(false);
//        mWebSettings.setAppCacheEnabled(false);
//        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // Inject Bridge
        mWebView.addJavascriptInterface(new JSBridge(this, mWebView), "JSBridgePlugin");
        mWebView.loadUrl(URL);
    }

    private void initialize() {
//        Sentry.init("https://f77b18f2b95b4bc58046a4755b3a5b21@sentry.io/1955488", new AndroidSentryClientFactory(this));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}