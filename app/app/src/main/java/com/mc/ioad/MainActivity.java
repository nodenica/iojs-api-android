package com.mc.ioad;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "DfpInterstitial";

    private PublisherInterstitialAd interstitial = null;

    private WebView webView;
    /**
     * The view to show the ad.
     */
    private AdView adView;
    private AdRequest adRequest;
    private String deviceid;

    private int counter = 0;
    private int counterMax = 6;

    /* Your ad unit id. Replace with your actual ad unit id. */
    private static final String AD_UNIT_ID = "ca-app-pub-2015513932539714/9417463023";
    private static final String AD_UNIT_INT_ID = "ca-app-pub-2015513932539714/1894196229";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // admob
        adMobInit();

        adMobIntInit();

        // Hide back on ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // WebView Instance
        webView = (WebView) findViewById(R.id.webView);
        // Enable JAvaScript
        webView.getSettings().setJavaScriptEnabled(true);
        // Load home page
        webView.loadUrl("file:///android_asset/en/index.html");

        // Handler webView client
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                getSupportActionBar().setTitle(view.getTitle());
                counter++;

                if (counter >= counterMax) {
                    counter = 0;
                    displayInterstitial();
                }

                Log.i("EventCount", "" + counter);

                URL urls;
                try {
                    urls = new URL(webView.getUrl());
                    // get file name from url
                    String fileName = urls.getPath().substring(
                            urls.getPath().lastIndexOf("/") + 1);
                    if (fileName != "index.html") {
                        // show back on ActionBar
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    } else {
                        // hide back on ActionBar
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

    }

    private void adMobIntInit() {
        if (isConnected()) {
            // Create the interstitial.
            interstitial = new PublisherInterstitialAd(MainActivity.this);
            interstitial.setAdUnitId(AD_UNIT_INT_ID);

            // Set the AdListener.
            interstitial.setAdListener(new InterstitialAdListener());

            // Create ad request.
            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

            // Begin loading your interstitial.
            interstitial.loadAd(adRequest);
        }
    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (interstitial != null) {
            if (interstitial.isLoaded()) {
                interstitial.show();
            } else {
                Log.d("AdIntersial", "Interstitial ad was not ready to be shown.");
            }
        }

    }

    private void adMobInit() {
        // Create an ad.
        adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(AD_UNIT_ID);

        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.
        LinearLayout layout = (LinearLayout) findViewById(R.id.ad);
        layout.removeAllViews();

        if (isConnected()) {
            layout.setVisibility(View.VISIBLE);
            layout.addView(adView);

            final TelephonyManager tm = (TelephonyManager) getBaseContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);

            deviceid = tm.getDeviceId();

            // Create an ad request. Check logcat output for the hashed device
            // ID to
            // get test ads on a physical device.
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(deviceid).build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = false;
        if (activeNetwork != null)
            isConnected = true;
        return isConnected;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
                return true;
            case R.id.action_settings:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.mc.nad.pro"));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /**
     * Called before the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        adMobInit();

    }

    private class InterstitialAdListener extends AdListener {
        /**
         * Called when an ad is loaded.
         */
        @Override
        public void onAdLoaded() {
            Log.d(LOG_TAG, "onAdLoaded");
        }

        /**
         * Called when an ad failed to load.
         */
        @Override
        public void onAdFailedToLoad(int errorCode) {
            String message = String.format("onAdFailedToLoad (%s)", getErrorReason(errorCode));
            Log.d(LOG_TAG, message);
        }

        /**
         * Called when an Activity is created in front of the app (e.g. an interstitial is shown, or an
         * ad is clicked and launches a new Activity).
         */
        @Override
        public void onAdOpened() {
            Log.d(LOG_TAG, "onAdOpened");
        }

        /**
         * Called when an ad is closed and about to return to the application.
         */
        @Override
        public void onAdClosed() {
            Log.d(LOG_TAG, "onAdClosed");
        }

        /**
         * Called when an ad is clicked and going to start a new Activity that will leave the
         * application (e.g. breaking out to the Browser or Maps application).
         */
        @Override
        public void onAdLeftApplication() {
            Log.d(LOG_TAG, "onAdLeftApplication");
        }

        /**
         * Gets a string error reason from an error code.
         */
        private String getErrorReason(int errorCode) {
            switch (errorCode) {
                case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                    return "Internal error";
                case AdRequest.ERROR_CODE_INVALID_REQUEST:
                    return "Invalid request";
                case AdRequest.ERROR_CODE_NETWORK_ERROR:
                    return "Network Error";
                case AdRequest.ERROR_CODE_NO_FILL:
                    return "No fill";
                default:
                    return "Unknown error";
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

}
