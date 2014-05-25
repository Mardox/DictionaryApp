package com.mardox.dictionaryapp.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {


    EditText wordQueryET;
    TextView wordQueryTV;
    TextView typeQueryTV;
    TextView definationTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordQueryET = (EditText) findViewById(R.id.query_et);
        wordQueryTV = (TextView) findViewById(R.id.query_tv);
        typeQueryTV = (TextView) findViewById(R.id.type_tv);
        definationTV = (TextView) findViewById(R.id.description_tv);

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



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    private void searchWord(){

        //Search the database and update the UI.


//        Dictionary dic = new Dictionary();
//        Word search_result = dic.search(wordQueryET.getText().toString());

        DatabaseController dbc = new DatabaseController(getApplicationContext());
        Word search_result = dbc.search(wordQueryET.getText().toString());


        if(search_result.getDefinition() != null){
            wordQueryTV.setText(search_result.getWord());
            typeQueryTV.setVisibility(View.VISIBLE);
            typeQueryTV.setText("Type: "+search_result.getType());
            definationTV.setText(search_result.getDefinition());
            definationTV.setGravity(0);// set the gravity to center
        }else{
            wordQueryTV.setText(getString((R.string.no_result_title)));
            typeQueryTV.setVisibility(View.INVISIBLE);
            definationTV.setText(getString(R.string.no_result_description));
            definationTV.setGravity(17);// set the gravity to center
        }


    }


}

