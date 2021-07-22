package com.codepath.bookself.ui.discover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.bookself.BuildConfig;
import com.codepath.bookself.DiscoverAdapter;
import com.codepath.bookself.LaunchActivity;
import com.codepath.bookself.R;
import com.codepath.bookself.models.BooksParse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DiscoverFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";
    private String tokenUrl = "https://oauth2.googleapis.com/token";
    private final String clientId = "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com";
    private RequestQueue mRequestQueue;
    private RecyclerView recyclerView;
    GoogleSignInClient mGoogleSignInClient;
    ArrayList<BooksParse> discoverBooks;
    DiscoverAdapter discoverAdapter;

    public DiscoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/books"))
                .requestServerAuthCode(clientId, true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        recyclerView = view.findViewById(R.id.rvDiscoverYou);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL));
        discoverBooks = new ArrayList<>();
        discoverAdapter = new DiscoverAdapter(discoverBooks, getContext());
        recyclerView.setAdapter(discoverAdapter);
        getRecommended(ParseUser.getCurrentUser().getString("accessToken"));
    }

    private void getRecommended(String accessToken) {
        discoverBooks = new ArrayList<>();

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(requireContext());

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/8/volumes?maxResults=40&key=" + BuildConfig.BOOKS_KEY;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(requireContext());


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
                        JSONArray authorsArray = new JSONArray();
                        String thumbnail = "";
                        String buyLink = "";
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                        String title = volumeObj.optString("title");
                        String subtitle = volumeObj.optString("subtitle");
                        try {
                            authorsArray = volumeObj.getJSONArray("authors");
                        } catch (JSONException e) {
                            Log.i(TAG, "No author", e);
                        }
                        String publisher = volumeObj.optString("publisher");
                        String publishedDate = volumeObj.optString("publishedDate");
                        String description = volumeObj.optString("description");
                        int pageCount = volumeObj.optInt("pageCount");
                        JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");
                        JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                        if (imageLinks != null) {
                            thumbnail = imageLinks.optString("thumbnail");
                        }
                        if (saleInfoObj != null) {
                            buyLink = saleInfoObj.optString("buyLink");
                        }
                        String previewLink = volumeObj.optString("previewLink");
                        String infoLink = volumeObj.optString("infoLink");
                        ArrayList<String> authorsArrayList = new ArrayList<>();
                        if (authorsArray.length() != 0) {
                            for (int j = 0; j < authorsArray.length(); j++) {
                                authorsArrayList.add(authorsArray.optString(i));
                            }
                        }
                        // after extracting all the data we are
                        // saving this data in our modal class.
                        BooksParse bookInfo = new BooksParse();
                        bookInfo.setBook(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink);
                        discoverBooks.add(bookInfo);
                    }
                    // below line is use to pass our modal
                    // class in our array list.
                    discoverAdapter.updateAdapter(discoverBooks);
                    Log.i(TAG, "URL: " + url);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
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
                    Toast.makeText(getContext(), "Error found is " + error, Toast.LENGTH_SHORT).show();
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
        RequestQueue queue = Volley.newRequestQueue(requireContext());
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
                    Toast.makeText(getContext(), "Error using refreshed token " + e, Toast.LENGTH_SHORT).show();
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
                clearTokens();
                goLaunchActivity();
            }
        });
        queue.add(refreshTokenRequest);
    }

    private void clearTokens() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Other attributes than "email" will remain unchanged!
            currentUser.put("accessToken", "");
            currentUser.put("refreshToken", "");
            // Saves the object.
            currentUser.saveInBackground();
        }
    }

    public void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener((Activity) requireContext(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logOut();
                    }
                });
    }

    public void logOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener((Activity) requireContext(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ParseUser.logOut();
                    }
                });
    }

    public void goLaunchActivity(){
        Intent i = new Intent(getContext(), LaunchActivity.class);
        startActivity(i);
        requireActivity().finish();
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
}