package com.example.harsh.ceefy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
//import com.firebase.security.token.TokenGenerator;
//import com.firebase.security.token.TokenOptions;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
//import com.firebase.security.token.*;
import java.util.HashMap;
import java.util.Map;
//import org.apache.commons.codec.android.binary.Base64;

public class UpdateProfile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView user_name,user_email,email1;
    ImageView profile_pic;
    NavigationView navigation_view;
    Button logout;
    String profile="";
    Firebase this_user;
    private static final String host = "api.linkedin.com";
    private static final String fb_secret="6ABOH4ojdO1h9FDPbLeKmNZyBtNwRRb3Vp75nDf4";
    private static final String topCardUrl = "https://" + host + "/v1/people/~:" +
            "(email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Button sms1 = (Button)findViewById(R.id.sms1);



        TokenOptions tokenOptions = new TokenOptions();
        tokenOptions.setAdmin(true);
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("uid", "1");
        TokenGenerator tokenGenerator = new TokenGenerator(fb_secret);
        String token = tokenGenerator.createToken(payload,tokenOptions);
        Log.d("Token",token);
        Firebase ref = new Firebase("https://cefy.firebaseio.com/");
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        this_user = ref.child(android_id);
        ref.authWithCustomToken(token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticationError(FirebaseError error) {
                System.err.println("Login Failed! " + error.getMessage());
            }

            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d("Login" ,"Succeeded!");
                Map<String, Object> user_data = new HashMap<String, Object>();
                user_data.put("email",user_email );
                user_data.put("profile",profile);
                user_data.put("Name",user_name);
                this_user.updateChildren(user_data);
            }
        });

        getUserData();
        sms1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(UpdateProfile.this,ReadSMS.class);
                startActivity(intent);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setNavigationHeader();
        navigation_view = (NavigationView) findViewById(R.id.nav_view);
        navigation_view.setNavigationItemSelectedListener(this);
        //navigationView.setNavigationItemSelectedListener(this);
    }
    public void getUserData(){
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(UpdateProfile.this, topCardUrl, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse result) {
                try {

                    setUserProfile(result.getResponseDataAsJson());
                    profile=result.getResponseDataAsJson().toString();
                    //progress.dismiss();

                } catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onApiError(LIApiError error) {
                // ((TextView) findViewById(R.id.error)).setText(error.toString());

            }
        });
    }
    public void setNavigationHeader(){

        navigation_view = (NavigationView) findViewById(R.id.nav_view);

        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_update_profile, null);
        navigation_view.addHeaderView(header);

        user_name = (TextView) header.findViewById(R.id.username);
        profile_pic = (ImageView) header.findViewById(R.id.profile_pic);
        user_email = (TextView) header.findViewById(R.id.email);
    }

    /*
       Set User Profile Information in Navigation Bar.
     */

    public  void  setUserProfile(JSONObject response){

        try {

            user_email.setText(response.get("emailAddress").toString());
            user_name.setText(response.get("formattedName").toString());
            String a="user profile"+response.get("emailAddress").toString();
            Log.d("umail",a);
            Picasso.with(this).load(response.getString("pictureUrl"))
                    .into(profile_pic);

        } catch (Exception e){
            String a =e.getMessage();
            Log.d("eemail",a);
            e.printStackTrace();
        }
    }
    boolean doubleBackToExitPressedOnce = false;

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.update_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            LISessionManager.getInstance(getApplicationContext()).clearSession();
            Intent intent = new Intent(UpdateProfile.this, LoginActivity.class);
            startActivity(intent);

 //           return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
