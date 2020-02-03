package ph.cleverqms.kiosk;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
            Log.d("PrinterService", "Connected");
            UsbPrinterConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("PrinterService", "Disconnected");
        }
    };
    private String URL;
    private SharedPreferences sharedPreferences;
    private Utils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

        mUtils = new Utils(this);
        mUtils.initialize();

        sharedPreferences = getBaseContext().getSharedPreferences("com.example.kiosk.PREFS", MODE_PRIVATE);

        String apiUrl = sharedPreferences.getString("apiUrl", getResources().getString(R.string.url_default));
        String ui = sharedPreferences.getString("ui", getResources().getString(R.string.ui_default));
        String apiVer = sharedPreferences.getString("apiVer", "");
        String apiKey = sharedPreferences.getString("apiKey", "");

        URL = apiUrl + "/modules/" + ui + ".php?v=" + apiVer + "&ak=" + apiKey;

        if (getIsFirstStart()) {
            startActivity(new Intent(MainActivity.this, ConfigActivity.class));
        } else {
            setUpWebView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUtils.initialize();
        setUpWebView();

        Intent intent = new Intent(this, PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            binder.disconnectCurrentPort(new UiExecute() {
                @Override
                public void onsucess() {
                    Log.d("PrinterService", "Successfully Disconnected");
                }

                @Override
                public void onfailed() {
                    Log.d("PrinterService", "Failed to Disconnect");
                }
            });
            unbindService(conn);
        } catch (RuntimeException e) {
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
        }
    }

    private boolean getIsFirstStart() {
        return sharedPreferences.getBoolean("isFirstStart", true);
    }

    private void setUpWebView() {
        WebView mWebView = findViewById(R.id.activity_main_webview);

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());

        WebSettings mWebSettings = mWebView.getSettings();

        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setDisplayZoomControls(false);
        mWebSettings.setBuiltInZoomControls(false);

        // Inject Bridge
        mWebView.addJavascriptInterface(new JSBridge(this, mWebView), "JSBridgePlugin");
        mWebView.loadUrl(URL);
    }
}