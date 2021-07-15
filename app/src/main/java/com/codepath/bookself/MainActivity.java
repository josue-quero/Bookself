package com.codepath.bookself;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.bookself.databinding.ActivityMainBinding;
import com.codepath.bookself.models.Books;
import com.codepath.bookself.ui.discover.DiscoverFragment;
import com.codepath.bookself.ui.library.LibraryFragment;
import com.codepath.bookself.ui.profile.ProfileFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private String tokenUrl = "https://oauth2.googleapis.com/token";
    public static final String TAG = "MainActivity";
    private RequestQueue mRequestQueue;
    private ArrayList<Books> bookInfoArrayList;
    public String userId;
    GoogleSignInClient mGoogleSignInClient;
    private final String clientId = "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Getting google client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/books"))
                .requestServerAuthCode(clientId, true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) binding.btNavigationView;

        // Definition of fragments
        final Fragment fragmentDiscover = new DiscoverFragment();
        final Fragment fragmentLibrary = new LibraryFragment();
        final Fragment fragmentProfile = new ProfileFragment();

        // Handle navigation selection

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_library:
                        //item.setIcon(R.drawable.ic_instagram_home_filled_24);
                        fragment = fragmentLibrary;
                        break;
                    case R.id.navigation_discover:
                        //item.setIcon(R.drawable.ic_instagram_new_post_filled_24);
                        fragment = fragmentDiscover;
                        break;
                    case R.id.navigation_profile:
                        //item.setIcon(R.drawable.ic_instagram_user_filled_24);
                        fragment = fragmentProfile;
                        break;
                    default: return true;
                }
                fragmentManager.beginTransaction().replace(R.id.frPlaceholder, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.navigation_discover);

        getBooksInfo("Vonnegut");
        getInfoFromSignedInUser();
        getRecommended(ParseUser.getCurrentUser().getString("accessToken"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.extOption) {
            // Compose icon has been selected
            //Navigate to compose activity
            LaunchActivity temp = new LaunchActivity();
            logOut();
            goLaunchActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getInfoFromSignedInUser() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            userId = acct.getId();
        }
    }


    private void getRecommended(String accessToken) {
        bookInfoArrayList = new ArrayList<>();

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/8/volumes?key=" + BuildConfig.BOOKS_KEY;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //progressBar.setVisibility(View.GONE);
                // inside on response method we are extracting all our json data.
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    Log.i(TAG, "Bookshelf Response: " + itemsArray);
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                        String title = volumeObj.optString("title");
                        String subtitle = volumeObj.optString("subtitle");
                        JSONArray authorsArray = volumeObj.getJSONArray("authors");
                        String publisher = volumeObj.optString("publisher");
                        String publishedDate = volumeObj.optString("publishedDate");
                        String description = volumeObj.optString("description");
                        int pageCount = volumeObj.optInt("pageCount");
                        JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");
                        String thumbnail = imageLinks.optString("thumbnail");
                        String previewLink = volumeObj.optString("previewLink");
                        String infoLink = volumeObj.optString("infoLink");
                        JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                        String buyLink = saleInfoObj.optString("buyLink");
                        ArrayList<String> authorsArrayList = new ArrayList<>();
                        if (authorsArray.length() != 0) {
                            for (int j = 0; j < authorsArray.length(); j++) {
                                authorsArrayList.add(authorsArray.optString(i));
                            }
                        }
                        // after extracting all the data we are
                        // saving this data in our modal class.
                        Books bookInfo = new Books(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink);

                        // below line is use to pass our modal
                        // class in our array list.
                        bookInfoArrayList.add(bookInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
                    Toast.makeText(MainActivity.this, "No Data Found" + e, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "No data found: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    refreshAccessToken();
                } else {
                    // irrecoverable errors. show error to user.
                    Toast.makeText(MainActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error found is: " + error);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
    }

    private void refreshAccessToken() {
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JSONObject params = new JSONObject();
        try {
            params.put("client_id", "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com");
            params.put("client_secret", "FlTA8PyCAx43q4XjK3X-wZbC");
            params.put("refresh_token", ParseUser.getCurrentUser().getString("refreshToken"));
            params.put("grant_type", "refresh_token");
        } catch (JSONException ignored) {
            // never thrown in this case
        }

        JsonObjectRequest refreshTokenRequest = new JsonObjectRequest(Request.Method.POST, tokenUrl, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i(TAG, "Success refresh token");
                    String accessToken = response.getString("access_token");
                    saveAccessToken(accessToken);
                    getRecommended(accessToken);
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error using refreshed token " + e, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error using refreshed token " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // show error to user. refresh failed.
                Log.e("Error on token refresh", new String(error.networkResponse.data));
                LaunchActivity temp = new LaunchActivity();
                revokeAccess();
                goLaunchActivity();
            }
        });
        queue.add(refreshTokenRequest);
    }

    private void getBooksInfo(String query) {
        bookInfoArrayList = new ArrayList<>();

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(MainActivity.this);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query + "&key=" + BuildConfig.BOOKS_KEY;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //progressBar.setVisibility(View.GONE);
                // inside on response method we are extracting all our json data.
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    Log.i(TAG, "Response: " + itemsArray);
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                        String title = volumeObj.optString("title");
                        String subtitle = volumeObj.optString("subtitle");
                        JSONArray authorsArray = volumeObj.getJSONArray("authors");
                        String publisher = volumeObj.optString("publisher");
                        String publishedDate = volumeObj.optString("publishedDate");
                        String description = volumeObj.optString("description");
                        int pageCount = volumeObj.optInt("pageCount");
                        JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");
                        String thumbnail = imageLinks.optString("thumbnail");
                        String previewLink = volumeObj.optString("previewLink");
                        String infoLink = volumeObj.optString("infoLink");
                        JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                        String buyLink = saleInfoObj.optString("buyLink");
                        ArrayList<String> authorsArrayList = new ArrayList<>();
                        if (authorsArray.length() != 0) {
                            for (int j = 0; j < authorsArray.length(); j++) {
                                authorsArrayList.add(authorsArray.optString(i));
                            }
                        }
                        // after extracting all the data we are
                        // saving this data in our modal class.
                        Books bookInfo = new Books(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink);

                        // below line is use to pass our modal
                        // class in our array list.
                        bookInfoArrayList.add(bookInfo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
                    Toast.makeText(MainActivity.this, "No Data Found" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(MainActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error found is: " + error);
            }
        });
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
    }

    public void saveAccessToken(String refreshToken) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Other attributes than "email" will remain unchanged!
            currentUser.put("accessToken", refreshToken);
            // Saves the object.
            currentUser.saveInBackground();
        }
    }

    public void logOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ParseUser.logOut();
                    }
                });
    }

    public void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logOut();
                    }
                });
    }

    public void goLaunchActivity(){
        Intent i = new Intent(this, LaunchActivity.class);
        startActivity(i);
        finish();
    }
}