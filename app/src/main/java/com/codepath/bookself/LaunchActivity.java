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
import java.util.Objects;

public class LaunchActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    public static final String TAG = "LaunchActivity";
    public GoogleSignInClient mGoogleSignInClient;
    private String authCode;
    public static String accessToken;
    public static String refreshToken;
    public static Long expiresInSeconds;

    // Setting the Google Sign In and the button that starts the signin flow
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // Creating the GoogleSignInOptions to ask for the necessary scopes (Google Books)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(getString(R.string.booksScope)))
                .requestServerAuthCode(getString(R.string.clientId), true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.btnSignIn);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked");
                signIn();
            }
        });
    }

    // Redirecting the user to the Google Servers
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // If the user is signed in, we go directly to the Main Activity
    @Override
    protected void onStart() {
        super.onStart();
        ParseUser currentUser = ParseUser.getCurrentUser();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // Check both the Parse and Google current user
        if (account != null && currentUser != null) {
            Log.i(TAG, "No one signed in");
            goMainActivity();
        }
    }

    // Goes to the Main Activity
    private void goMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    // When the user has completed the sign in in the Google Server this function is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the return call is correct then I handle the SignIn
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Getting the authCode from the server response and start the login in Parse
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
            Log.e(TAG, "Sign in Result: failed code ", e);
        }
    }

    // Login to Parse
    private void loginParse(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                // If there was an error sign in the user to Parse, then the user is new.
                if (e != null) {
                    Log.i(TAG, "New user", e);

                    // I Sign up the new user
                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){

                                // Start the flow for a new user
                                // Get the access and refresh tokens
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

                // Checking if the current user has access and refresh token or not
                if (Objects.equals(ParseUser.getCurrentUser().getString("accessToken"), "") && Objects.equals(ParseUser.getCurrentUser().getString("refreshToken"), "")) {
                    try {
                        // Get the access and refresh tokens
                        getTokens();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                // If the user has access and refresh tokens he's sent to the Main Activity
                } else {
                    goMainActivity();
                    Toast.makeText(LaunchActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Getting access and refresh tokens from Google
    private void getTokens() throws IOException {
        Thread t = new Thread(new Runnable() {
            public void run() {
                GoogleTokenResponse tokenResponse;
                try {
                    tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                            new NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            getString(R.string.tokenServerEncodedUrl),
                            getString(R.string.clientId),
                            getString(R.string.clientSecret),
                            authCode,
                            "")
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

    // Saving access and refresh tokens in Parse database
    public void saveTokens(String accessToken, String refreshToken) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.put("accessToken", accessToken);
            currentUser.put("refreshToken", refreshToken);
            // Saves the object.
            currentUser.saveInBackground();
        }
    }
}