package com.example.harsh.ceefy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    LoginButton loginButton;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    Button fblogout;
    ProfileTracker profileTracker;
    Profile profile;
    Button button,sms,llogin;
    /*private static final String host = "api.linkedin.com";
    private static final String topCardUrl = "https://" + host + "/v1/people/~:" +
            "(email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";*/
    TextView textView,user_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        sms=(Button)findViewById(R.id.sms);
        llogin=(Button)findViewById(R.id.linked_log);
        llogin.setVisibility(View.VISIBLE);
        fblogout =(Button)findViewById(R.id.fblogout);
        fblogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                textView.setText("");
                loginButton.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);
                fblogout.setVisibility(View.GONE);
                sms.setVisibility(View.GONE);

            }
        });
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.harsh.ceefy",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

        llogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,LinkedWeb.class);
                startActivity(intent);
            }
        });
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(LoginActivity.this,ReadSMS.class);
                startActivity(intent);
            }
        });
        loginButton = (LoginButton) findViewById(R.id.login_button);
        button=(Button) findViewById(R.id.li_login);
        textView=(TextView)findViewById(R.id.text);
        user_name=(TextView)findViewById(R.id.user_name);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click","clicked");
                llogin.setVisibility(View.GONE);
                login_linkedin();

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llogin.setVisibility(View.GONE);

                loginButton.setReadPermissions(Arrays.asList(
                        "public_profile", "email", "user_birthday", "user_friends"));
                fblogout.setVisibility(View.VISIBLE);
                sms.setVisibility(View.VISIBLE);

                // If using in a fragment
                //    loginButton.setFragment(this);
                // Other app specific specialization

                // Callback registration

               /* loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.e("Well","Login successfull");
                        profile = Profile.getCurrentProfile();
                        textView.setText(displayMessage(profile));
                        textView.append(profile.getLastName().toString());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });*/
                accessTokenTracker = new AccessTokenTracker() {
                    @Override
                    protected void onCurrentAccessTokenChanged(
                            AccessToken oldAccessToken,
                            AccessToken currentAccessToken) {
                        // Set the access token using
                        // currentAccessToken when it's loaded or set.
                    }
                };
                // If the access token is available already assign it.
                accessToken = AccessToken.getCurrentAccessToken();
                CurrentUser();
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                // App code
                                Log.e("Well2","Login successfull");
                                profile = Profile.getCurrentProfile();
                                textView.setText(displayMessage(profile));
                                textView.append(" "+profile.getLastName().toString());
                                textView.append(" "+profile.getId().toString());
                               // profileTracker.startTracking();
                            }

                            @Override
                            public void onCancel() {
                                profileTracker.stopTracking();
                                // App code
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                profileTracker.stopTracking();
                                // App code
                            }
                        });

                profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(
                            Profile oldProfile,
                            Profile currentProfile) {
                        // App code
                        /*if(profileTracker.isTracking()==false) {
                            currentProfile = Profile.getCurrentProfile();
                            String c = currentProfile.getFirstName();
                            String b = currentProfile.getLastName();
                            String a = currentProfile.getId();
                            textView.setText(" " + c + " " + b + " " + a + " ");
                        }*/
                    }
                };
                loginButton.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d("resultc",String.valueOf(resultCode));
        Log.d("reqc",String.valueOf(requestCode));
        if(requestCode == 3672) {
            Intent intent = new Intent(LoginActivity.this, UpdateProfile.class);
            startActivity(intent);
        }
        else if(requestCode == 64206){
           /* Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);*/
        }

        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
    }

    private String displayMessage(Profile profile) {
        StringBuilder stringBuilder = new StringBuilder();
        if (profile != null) {
            stringBuilder.append("Logged In "+profile.getFirstName());
            /*Toast.makeText(getApplicationContext(), "Start Playing with the data "+profile.getFirstName(), Toast.LENGTH_SHORT).show();*/
        }else{
            stringBuilder.append("You are not logged in");
        }
        return stringBuilder.toString();
    }
    public void CurrentUser(){
        LoginManager.getInstance().logInWithReadPermissions(this,Arrays.asList("public_profile"));
    }
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS);
    }
    public void login_linkedin(){
        LISessionManager.getInstance(getApplicationContext()).init(this,
                buildScope(),new AuthListener() {

                    @Override
                    public void onAuthSuccess() {

/*                        Toast.makeText(getApplicationContext(), "success yooo" , Toast.LENGTH_LONG).show();*/

                        Log.d("pass","pass");

                    }

                    @Override
                    public void onAuthError(LIAuthError error) {

                        /*Toast.makeText(getApplicationContext(), "failed " + error.toString(),
                                Toast.LENGTH_LONG).show();*/
                        Log.d("fail","fail");
                    }
                }, true);
    }
    /*public void getUserData(){
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, topCardUrl, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse result) {
                try {

                    setUserProfile(result.getResponseDataAsJson());
     //               progress.dismiss();

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
    public  void  setUserProfile(JSONObject response){

        try {


            user_name.setText(response.get("formattedName").toString());
            Toast.makeText(getApplicationContext(),response.get("formattedName").toString(),Toast.LENGTH_LONG).show();

            *//*Picasso.with(this).load(response.getString("pictureUrl"))
                    .into(profile_pic);
*//*
        } catch (Exception e){
            e.printStackTrace();
        }
    }





    *//*
       Set User Profile Information in Navigation Bar.
     *//*
*/

    @Override
    public void onDestroy() {
        super.onDestroy();
      // accessTokenTracker.stopTracking();
//        profileTracker.stopTracking();
    }
    boolean doubleBackToExitPressedOnce = false;

    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        LoginManager.getInstance().logOut();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
