package com.example.vladimir.informer_pi;

import com.example.vladimir.informer_pi.MainActivity;
import com.example.vladimir.informer_pi.R;
import com.perm.kate.api.Auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Vladimir Kadochnikov on 02.02.15.
 */
public class LoginActivity extends Activity {
    private static final String TAG = "Kate.LoginActivity";
    private static final int REQUEST_LOGIN = 1;

    WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        webview = (WebView) findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        //otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
//        CookieSyncManager.createInstance(this);
//
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.removeAllCookie();

        String url = Auth.getUrl(MainActivity.API_ID, Auth.getSettings());
        webview.loadUrl(url);
    }


    class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            parseUrl(url);
        }
    }

    private void parseUrl(String url) {
        try {
            if (url == null)
                return;
            Log.i(TAG, "url=" + url);
            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    String[] auth = Auth.parseRedirectUrl(url);
                    Intent intent = new Intent();
                    intent.putExtra("token", auth[0]);
                    intent.putExtra("user_id", Long.parseLong(auth[1]));
                    setResult(Activity.RESULT_OK, intent);
                }
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
