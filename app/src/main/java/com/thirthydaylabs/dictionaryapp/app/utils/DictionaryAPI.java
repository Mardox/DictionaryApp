package com.thirthydaylabs.dictionaryapp.app.utils;

import com.thirthydaylabs.dictionaryapp.app.Word;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by HooMan on 1/06/2014.
 */
public class DictionaryAPI {

    private static Word result;

    public static Word getDefinition (String query){


        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://glosbe.com/gapi/translate?from=pol&dest=eng&format=json&phrase=witaj&pretty=true");
        // Create a new HttpClient and Post Header
        // Add your data
        try {
            HttpResponse response = httpclient.execute(httpget);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                String responseString = out.toString();
                JSONObject jsonResponse = new JSONObject(responseString);


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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }



}
