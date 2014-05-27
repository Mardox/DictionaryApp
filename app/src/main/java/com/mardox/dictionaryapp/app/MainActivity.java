package com.mardox.dictionaryapp.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;


public class MainActivity extends ActionBarActivity {


    public static final String TAG = "DictionaryApp";

    final Context context = this;

    EditText wordQueryET;
    TextView wordQueryTV;
    TextView typeQueryTV;
    TextView definitionTV;


    //Admob
    private InterstitialAd interstitial;
    private AdView adView;
    private RelativeLayout.LayoutParams rLParams;
    private RelativeLayout rLayout;

    Dialog dialog;

    public static final String PREFS_NAME  = "PrefsFile";
    boolean premium_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordQueryET = (EditText) findViewById(R.id.query_et);
        wordQueryTV = (TextView) findViewById(R.id.query_tv);
        typeQueryTV = (TextView) findViewById(R.id.type_tv);
        definitionTV = (TextView) findViewById(R.id.description_tv);

        wordQueryET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchWord();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        wordQueryET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(wordQueryET.getWindowToken(), 0);
                    searchWord();
                    handled = true;
                }
                return handled;
            }
        });


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
        boolean overlay_shown = settings.getBoolean("helpOverlay", false);
        if(!overlay_shown){
            //showOverLay();
        }


        //Initiate the admob banner
        adMobBannerInitiate();
        adMobInterstitialInitiate();


    }



    //Activity life cycles
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }

        super.onDestroy();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void searchWord(){

        //Search the database and update the UI.

        if(!wordQueryET.getText().toString().isEmpty()){

            DatabaseController dbc = new DatabaseController(getApplicationContext());
            Word search_result = dbc.search(wordQueryET.getText().toString());


            if(search_result.getDefinition() != null){
                wordQueryTV.setText(search_result.getWord());
                typeQueryTV.setVisibility(View.VISIBLE);
                typeQueryTV.setText(getString(R.string.type)+": "+search_result.getType());
                definitionTV.setText(search_result.getDefinition());
                definitionTV.setGravity(0);// set the gravity to center
            }else{
                wordQueryTV.setText(getString((R.string.no_result_title)));
                typeQueryTV.setVisibility(View.INVISIBLE);
                definitionTV.setText(getString(R.string.no_result_description));
                definitionTV.setGravity(17);// set the gravity to center
            }

        }else{
            //Clean the result when the search is empty
            wordQueryTV.setText("");
            typeQueryTV.setText("");
            definitionTV.setText("");
        }


    }


    /**
     * Initiate the adMob Banner
     */
    private void adMobBannerInitiate(){

        rLayout = (RelativeLayout) findViewById(R.id.main_layout);

        rLParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);

        rLayout = (RelativeLayout) findViewById(R.id.main_layout);

        //Remove the current banner
        AdView oldAdView = (AdView) findViewById(1);

        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.

        // Destroy the old AdView.
        if (oldAdView != null) {
            rLayout.removeView(oldAdView);
            oldAdView.destroy();
        }


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
        premium_status = settings.getBoolean("premiumStatus", false);

        if(!premium_status && !getString(R.string.admob_id_home).equals("")) {
            adView = new AdView(this);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(getString(R.string.admob_id_home));
            adView.setId(1);

            rLayout.addView(adView, rLParams);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device.
            AdRequest adRequest = new AdRequest.Builder().build();

            // Start loading the ad in the background.
            adView.loadAd(adRequest);
            final EasyTracker easyTracker = EasyTracker.getInstance(context);
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdOpened() {
                    //Send the ad open event to google analytics
                    easyTracker.send(MapBuilder
                                    .createEvent("ui_event",     // Event category (required)
                                            "button_press",  // Event action (required)
                                            "banner_ad",   // Event label
                                            null)            // Event value
                                    .build()
                    );
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    //Send the on ad fail to load event to google analytics
                    easyTracker.send(MapBuilder
                                    .createEvent("ui_event",     // Event category (required)
                                            "ui_load_fail",  // Event action (required)
                                            "banner_ad",   // Event label
                                            null)            // Event value
                                    .build()
                    );
                }

            });
        }

    }



    /**
     *  Initiate the interstitial adMob
     */
    private void adMobInterstitialInitiate(){

        if(!getString(R.string.admob_id_interstitials).equals("")){
            //AdMob Full Screen
            //Create the interstitial
            interstitial = new InterstitialAd(this);
            interstitial.setAdUnitId(getString(R.string.admob_id_interstitials));
            // Create ad request
            AdRequest interAdRequest = new AdRequest.Builder().build();
            // Begin loading your interstitial
            interstitial.loadAd(interAdRequest);

            final Activity act = this;
            interstitial.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    // Save app state before going to the ad overlay.
                    act.finish();
                }
            });
        }

    }



    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
        premium_status = settings.getBoolean("premiumStatus", false);
        if (interstitial.isLoaded() && !premium_status) {
            interstitial.show();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}


