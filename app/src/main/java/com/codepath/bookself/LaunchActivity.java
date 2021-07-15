package com.codepath.bookself;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.bookself.databinding.ActivityLaunchBinding;
import com.codepath.bookself.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LaunchActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    public static final String TAG = "LaunchActivity";
    public GoogleSignInClient mGoogleSignInClient;
    private String clientId = "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com";
    private String authCode;
    public static String accessToken;
    public static String refreshToken;
    public static Long expiresInSeconds;

    private ActivityLaunchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //binding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_launch);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.i(TAG, "No one signed in");
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/books"))
                .requestServerAuthCode(clientId, true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.btnSignIn);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked");
                Toast.makeText(LaunchActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.i(TAG, "No one signed in");
            goMainActivity();
        }
        //silentSignIn()
    }
    /*
    private void silentSignIn() {
        mGoogleSignInClient.silentSignIn()
                .addOnSuccessListener(this, new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.i(TAG, "Silent sign in success");
                        goMainActivity();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }*/

    private void goMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try{
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.i(TAG, "Sign-in success!");

            assert account != null;
            authCode = account.getServerAuthCode();
            String personEmail = account.getEmail();
            String personId = account.getId();


            loginParse(personEmail, personId);
        } catch (ApiException e) {
            Log.w(TAG, "Sign in Result: failed code =" + e);
        }
    }

    private void getTokens() throws IOException {
        Thread t = new Thread(new Runnable() {
            public void run() {
                GoogleTokenResponse tokenResponse;
                try {
                    tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            "https://oauth2.googleapis.com/token",
                            clientId,
                            "FlTA8PyCAx43q4XjK3X-wZbC",
                            authCode,
                            "")  // Specify the same redirect URI that you use with your web
                            // app. If you don't have a web version of your app, you can
                            // specify an empty string.
                            .execute();
                    accessToken = tokenResponse.getAccessToken();
                    refreshToken = tokenResponse.getRefreshToken();
                    expiresInSeconds = tokenResponse.getExpiresInSeconds();
                    Log.i(TAG, "We did it: " + accessToken);
                    Log.i(TAG, "We did it: " + refreshToken);
                    Log.i(TAG, "We did it: " + expiresInSeconds);
                    saveTokens(accessToken, refreshToken);
                    goMainActivity();
                } catch (TokenResponseException e) {
                    Log.e(TAG, "Error retriving token: " + e);
                    if (e.getDetails() != null) {
                        System.err.println("Error: " + e.getDetails().getError());
                        if (e.getDetails().getErrorDescription() != null) {
                            System.err.println(e.getDetails().getErrorDescription());
                        }
                        if (e.getDetails().getErrorUri() != null) {
                            System.err.println(e.getDetails().getErrorUri());
                        }
                    } else {
                        System.err.println(e.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void loginParse(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.i(TAG, "New user", e);
                    ParseUser newUser = new ParseUser();

                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                //Get the access and refresh tokens
                                try {
                                    getTokens();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                                return;
                            } else{
                                Log.e(TAG, "Problem with sign up", e);
                            }
                        }
                    });
                    return;
                }
                goMainActivity();
                Toast.makeText(LaunchActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveTokens(String accessToken, String refreshToken) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Other attributes than "email" will remain unchanged!
            currentUser.put("accessToken", accessToken);
            currentUser.put("refreshToken", refreshToken);
            // Saves the object.
            currentUser.saveInBackground();
        }
    }
}