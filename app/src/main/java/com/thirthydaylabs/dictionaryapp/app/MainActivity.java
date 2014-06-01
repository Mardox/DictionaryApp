package com.thirthydaylabs.dictionaryapp.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.thirthydaylabs.engtagdic.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class MainActivity extends ActionBarActivity {


    public static final String TAG = "DictionaryApp";

    final Context context = this;

    Button switchLanguageBT;
    EditText wordQueryET;
    TextView noInternetTV;
    TextView wordQueryTV;
    TextView typeQueryTV;
    TextView otherDefinitionsTV;
    TextView otherDefinitionsDescriptionTV;
    TextView definitionTV;
    RelativeLayout mainLayout;
    LinearLayout resultL;
    boolean languageSwitchFlag = true;


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

        switchLanguageBT = (Button) findViewById(R.id.switch_language_bt);
        wordQueryET = (EditText) findViewById(R.id.query_et);
        noInternetTV = (TextView) findViewById(R.id.no_internet_tv);
        wordQueryTV = (TextView) findViewById(R.id.query_tv);
        typeQueryTV = (TextView) findViewById(R.id.type_tv);
        otherDefinitionsTV = (TextView) findViewById(R.id.other_definitions_tv);
        otherDefinitionsDescriptionTV = (TextView) findViewById(R.id.other_definitions_description_tv);
        definitionTV = (TextView) findViewById(R.id.description_tv);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        resultL = (LinearLayout) findViewById(R.id.result_layout);


        ActionBar actionBar = getActionBar();
        actionBar.hide();


        //live search handler
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

        //on click listener for the search button on the keyboard
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


        //Hide keyboard on off the edit text tap
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "relative layout tag");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(wordQueryET.getWindowToken(), 0);
            }
        });

        //Setting the switch button text
        switchLanguageBT.setText(getString(R.string.language_a)+"  >  "+getString(R.string.language_b));

        //Language switch listener
        switchLanguageBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(languageSwitchFlag){
                    languageSwitchFlag = false;
                    switchLanguageBT.setText(getString(R.string.language_b)+"  >  "+getString(R.string.language_a));
                }else{
                    languageSwitchFlag = true;
                    switchLanguageBT.setText(getString(R.string.language_a)+"  >  "+getString(R.string.language_b));
                }
                wordQueryET.setText("");
                otherDefinitionsDescriptionTV.setVisibility(View.GONE);
            }
        });


        //Help overlay controller
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS);
        boolean overlay_shown = settings.getBoolean("helpOverlay", false);
        if(!overlay_shown){
            //showOverLay();
        }


        //Initiate the Admob banner
        adMobBannerInitiate();
        //adMobInterstitialInitiate();


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


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    private void searchWord(){

        //Search the database and update the UI.
        String query = wordQueryET.getText().toString().trim();

        //Result reset
        wordQueryTV.setText("");
        definitionTV.setText("");
        typeQueryTV.setText("");
        otherDefinitionsTV.setText("");
        otherDefinitionsDescriptionTV.setVisibility(View.GONE);

        if(!query.isEmpty()){


            //Check if the internet connection is available
            if(!isOnline()){
                noInternetTV.setVisibility(View.VISIBLE);
                OfflineSearch();
                return;
            }
            noInternetTV.setVisibility(View.GONE);

            AsyncTask apiSearchAsyncTask = new APICall();
            if(apiSearchAsyncTask.getStatus()== AsyncTask.Status.RUNNING){
                apiSearchAsyncTask.cancel(true);
                boolean status = apiSearchAsyncTask.isCancelled();
            }

            DatabaseController dbc = new DatabaseController(getApplicationContext());
            Word search_result = dbc.search(query, languageSwitchFlag);
//            if(search_result.getDefinition() != null){
//                wordQueryTV.setText(search_result.getWord());
//                if(search_result.getType()!=null){
//                    typeQueryTV.setVisibility(View.VISIBLE);
//                    typeQueryTV.setText(getString(R.string.type) + ": " + search_result.getType());
//                }
//                definitionTV.setVisibility(View.VISIBLE);
//                definitionTV.setText(search_result.getDefinition());
//                definitionTV.setGravity(0);// set the gravity to center
//            }else{
                //Search the network

                String[] asyncTaskParams = {"",""};
                asyncTaskParams[0] = query;
                if(languageSwitchFlag)
                    asyncTaskParams[1] = "ab";
                else
                    asyncTaskParams[1] = "ba";


                apiSearchAsyncTask.execute(asyncTaskParams);

                //No results found
                //wordQueryTV.setText(getString((R.string.no_result_title)));
                wordQueryTV.setText("Searching Network");
                definitionTV.setVisibility(View.GONE);
                typeQueryTV.setVisibility(View.GONE);
                otherDefinitionsTV.setVisibility(View.GONE);
                otherDefinitionsDescriptionTV.setVisibility(View.GONE);

//            }

        }

    }



    class APICall extends AsyncTask<String, String, String> {

        private boolean running = true;

        @Override
        protected String doInBackground(String... query) {

            String responseString = null;

            String lang_a = getString(R.string.language_code_a);
            String lang_b = getString(R.string.language_code_b);

            if(query[1].equals("ab")){
                lang_a = getString(R.string.language_code_a);
                lang_b = getString(R.string.language_code_b);
            }else if(query[1].equals("ba")){
                lang_a = getString(R.string.language_code_b);
                lang_b = getString(R.string.language_code_a);
            }


            HttpClient httpclient = new DefaultHttpClient();
            String url = "http://glosbe.com/gapi/translate?from="+lang_a+"&dest="+lang_b+"&format=json&phrase="+query[0]+"&pretty=true" ;
            HttpGet httpget = new HttpGet(url);
            // Create a new HttpClient and Post Header
            // Add your data
            try {
                HttpResponse response = httpclient.execute(httpget);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();

                }else{
                    //Close the connection
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }


        @Override
        protected void onCancelled() {
            running = false;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..

            String targetLanguageCode;
            String otherLanguageCode;

            if(languageSwitchFlag){
                targetLanguageCode = getString(R.string.language_code_b);
                otherLanguageCode = getString(R.string.language_code_a);
            }else{
                targetLanguageCode = getString(R.string.language_code_a);
                otherLanguageCode = getString(R.string.language_code_b);
            }


            try {
                JSONObject jsonResponse = new JSONObject(result);

                String responsePhrase = jsonResponse.get("phrase").toString();

                String targetDefinition = "";
                String otherDefinitions = "";
                String targetLanguagePhrase = "";
                String otherLanguagePhrase = "";

                JSONArray objects = jsonResponse.getJSONArray("tuc");
                for (int i = 0 ; i < objects.length();i++){
                    JSONObject currentAuthorObject = objects.getJSONObject(i);
                    if (currentAuthorObject.has("meanings")){
                        JSONArray currentAuthorMeaningsObject = currentAuthorObject.getJSONArray("meanings");
                        for (int j = 0; j <currentAuthorMeaningsObject.length(); j++){
                            JSONObject currentMeaningObject = currentAuthorMeaningsObject.getJSONObject(j);
                            //Check if the string is number free
                            if(!currentMeaningObject.get("text").toString().matches(".*\\d.*")) {
                                if (currentMeaningObject.get("language").toString().equals(targetLanguageCode)) {
                                    targetDefinition += currentMeaningObject.get("text").toString() + ", ";
                                } else if (currentMeaningObject.get("language").toString().equals(otherLanguageCode)) {
                                    otherDefinitions += currentMeaningObject.get("text").toString() + ", ";
                                } else if (currentMeaningObject.get("language").toString().equals("eng")) {
                                    otherDefinitions += currentMeaningObject.get("text").toString() + ", ";
                                }
                            }
                        }
                    }

                    if(currentAuthorObject.has("phrase")){
                        JSONObject currentAuthorPhraseObject = currentAuthorObject.getJSONObject("phrase");
                        //Check if the string is number free
                        if(!currentAuthorPhraseObject.get("text").toString().matches(".*\\d.*")) {
                            if (currentAuthorPhraseObject.get("language").toString().equals(targetLanguageCode)) {
                                targetLanguagePhrase += currentAuthorPhraseObject.get("text").toString() + ", ";
                            } else if (currentAuthorPhraseObject.get("language").toString().equals(otherLanguageCode)) {
                                otherLanguagePhrase += currentAuthorPhraseObject.get("text").toString() + ", ";
                            } else if (currentAuthorPhraseObject.get("language").toString().equals("eng")) {
                                otherLanguagePhrase += currentAuthorPhraseObject.get("text").toString() + ", ";
                            }
                        }
                    }
                }

                wordQueryTV.setText(responsePhrase);

                if(targetDefinition != "" || targetLanguagePhrase != ""){
                    definitionTV.setVisibility(View.VISIBLE);
                    definitionTV.setText(targetDefinition + targetLanguagePhrase);
                    definitionTV.setGravity(0);// set the gravity to center
                }else{
                    definitionTV.setVisibility(View.GONE);
                }

                if(otherDefinitions != "" || otherLanguagePhrase != ""){
                    otherDefinitionsDescriptionTV.setVisibility(View.VISIBLE);
                    otherDefinitionsTV.setVisibility(View.VISIBLE);
                    otherDefinitionsTV.setText(otherDefinitions + otherLanguagePhrase);
                }else{
                    otherDefinitionsTV.setVisibility(View.GONE);
                    otherDefinitionsDescriptionTV.setVisibility(View.GONE);
                }


                if(targetLanguagePhrase.equals("") && targetDefinition.equals("") && otherLanguagePhrase.equals("") && otherDefinitions.equals("")){
                    //No network results found
                    //Do a depper database search
                    OfflineSearch();

                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    private void OfflineSearch(){

        DatabaseController dbc = new DatabaseController(getApplicationContext());
        Word search_result = dbc.search(wordQueryET.getText().toString().trim(), languageSwitchFlag);

        if(search_result.getDefinition() != null){
            wordQueryTV.setText(search_result.getWord());
            if(search_result.getType()!=null){
                typeQueryTV.setVisibility(View.VISIBLE);
                typeQueryTV.setText(getString(R.string.type) + ": " + search_result.getType());
            }
            definitionTV.setVisibility(View.VISIBLE);
            definitionTV.setText(search_result.getDefinition());
            definitionTV.setGravity(0);// set the gravity to center
        }else {

            wordQueryTV.setText(getString((R.string.no_result_title)));
            otherDefinitionsTV.setVisibility(View.INVISIBLE);
            otherDefinitionsDescriptionTV.setVisibility(View.INVISIBLE);
            typeQueryTV.setVisibility(View.INVISIBLE);
            definitionTV.setText(getString(R.string.no_result_description));
            definitionTV.setGravity(17);// set the gravity to center
        }
    }



    /**
     * Check if Internet connectuion is available
     */

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }




    /**
     * Network connection error dialog
     */
    private void networkErrorDialog() {

        final Context context = this;
        //Create the upgrade dialog
        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.error_dialog_title))
                .setMessage(R.string.no_internet_message)
                .setPositiveButton(R.string.retry_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // reset the request

                    }
                })
                .setIcon(R.drawable.ic_action_dark_error)
                .show();
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
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


}


