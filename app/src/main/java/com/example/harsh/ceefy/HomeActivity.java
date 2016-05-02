package com.example.harsh.ceefy;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.linkedin.platform.LISessionManager;

/**
 * @author AmalChandran(Novasys)
 * **/
public class HomeActivity extends Activity {
    private static final String PROFILE_URL = "https://api.linkedin.com/v1/people/~";
    private static final String OAUTH_ACCESS_TOKEN_PARAM ="oauth2_access_token";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";

    private TextView welcomeText;
    private ProgressDialog pd;
    Button sms2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        welcomeText = (TextView) findViewById(R.id.activity_profile_welcome_text);
        sms2 = (Button)findViewById(R.id.sms2);
        //Request basic profile of the user
        SharedPreferences preferences = this.getSharedPreferences("user_info", 0);
        String accessToken = preferences.getString("accessToken", null);
        if(accessToken!=null){
            String profileUrl = getProfileUrl(accessToken);
            new GetProfileRequestAsyncTask().execute(profileUrl);
        }
        sms2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(HomeActivity.this,ReadSMS.class);
                startActivity(intent);
            }
        });
    }

    private static final String getProfileUrl(String accessToken){
        return PROFILE_URL
                +QUESTION_MARK
                +OAUTH_ACCESS_TOKEN_PARAM+EQUALS+accessToken;
    }

    private class GetProfileRequestAsyncTask extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected void onPreExecute(){
            pd = ProgressDialog.show(HomeActivity.this, "", "Loading..",true);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            if(urls.length>0){
                String url = urls[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("x-li-format", "json");
                try{
                    HttpResponse response = httpClient.execute(httpget);
                    if(response!=null){
                        //If status is OK 200
                        if(response.getStatusLine().getStatusCode()==200){
                            String result = EntityUtils.toString(response.getEntity());
                            //Convert the string result to a JSON Object
                            return new JSONObject(result);
                        }
                    }
                }catch(IOException e){
                    Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
                } catch (JSONException e) {
                    Log.e("Authorize","Error Http response "+e.getLocalizedMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject data){
            if(pd!=null && pd.isShowing()){
                pd.dismiss();
            }
            if(data!=null){

                try {
                    String welcomeTextString = String.format("Welcome %1$s %2$s, You are a %3$s",data.getString("firstName"),data.getString("lastName"),data.getString("headline"));
                    welcomeText.setText(welcomeTextString);
                } catch (JSONException e) {
                    Log.e("Authorize","Error Parsing json "+e.getLocalizedMessage());
                }
            }
        }


    };
    boolean doubleBackToExitPressedOnce=false;
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            finish();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        LISessionManager.getInstance(getApplicationContext()).clearSession();
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}