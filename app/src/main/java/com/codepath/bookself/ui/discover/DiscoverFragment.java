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
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.bookself.BuildConfig;
import com.codepath.bookself.DetailsActivity;
import com.codepath.bookself.DiscoverAdapter;
import com.codepath.bookself.DiscoverOtherBooksAdapter;
import com.codepath.bookself.LaunchActivity;
import com.codepath.bookself.R;
import com.codepath.bookself.SearchActivity;
import com.codepath.bookself.models.BooksParse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DiscoverFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";
    private final String clientId = "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com";
    private RequestQueue mRequestQueue;
    private RecyclerView recyclerViewRecommended, recyclerViewPenguin, recyclerViewHachette, recyclerViewJava;
    private MaterialCardView cFiction, cDrama, cPoetry, cHumor, cArt;
    GoogleSignInClient mGoogleSignInClient;
    ArrayList<BooksParse> recommendedBooks, penguinBooks, hachetteBooks, javaBooks;
    DiscoverAdapter discoverAdapter;
    DiscoverOtherBooksAdapter penguinAdapter, hachetteAdapter, javaAdapter;

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

    // Creating the view for the discover page
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        // Getting the current Google signed in user
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(getString(R.string.booksScope)))
                .requestServerAuthCode(clientId, true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Finding the recycler views and setting them with a linear layout manager
        recyclerViewRecommended = view.findViewById(R.id.rvDiscoverYou);
        recyclerViewPenguin = view.findViewById(R.id.rvDiscoverPenguin);
        recyclerViewHachette = view.findViewById(R.id.rvDiscoverHachette);
        recyclerViewJava = view.findViewById(R.id.rvDiscoverJava);
        LinearLayoutManager layoutManagerRecommended = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManagerPenguin = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManagerHachette = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManagerJava = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewRecommended.setLayoutManager(layoutManagerRecommended);
        recyclerViewPenguin.setLayoutManager(layoutManagerPenguin);
        recyclerViewHachette.setLayoutManager(layoutManagerHachette);
        recyclerViewJava.setLayoutManager(layoutManagerJava);

        recommendedBooks = new ArrayList<>();
        penguinBooks = new ArrayList<>();
        hachetteBooks = new ArrayList<>();
        javaBooks = new ArrayList<>();

        // Setting the adapters with empty arraylists
        penguinAdapter = new DiscoverOtherBooksAdapter(penguinBooks, getContext());
        hachetteAdapter = new DiscoverOtherBooksAdapter(hachetteBooks, getContext());
        javaAdapter = new DiscoverOtherBooksAdapter(javaBooks, getContext());
        discoverAdapter = new DiscoverAdapter(recommendedBooks, getContext());
        recyclerViewRecommended.setAdapter(discoverAdapter);
        recyclerViewPenguin.setAdapter(penguinAdapter);
        recyclerViewHachette.setAdapter(hachetteAdapter);
        recyclerViewJava.setAdapter(javaAdapter);

        SnapHelper snapHelperRecommended = new LinearSnapHelper();
        snapHelperRecommended.attachToRecyclerView(recyclerViewRecommended);
        /*SnapHelper snapHelperPenguin = new LinearSnapHelper();
        snapHelperPenguin.attachToRecyclerView(recyclerViewPenguin);
        SnapHelper snapHelperHachette = new LinearSnapHelper();
        snapHelperHachette.attachToRecyclerView(recyclerViewHachette);
        SnapHelper snapHelperJava = new LinearSnapHelper();
        snapHelperJava.attachToRecyclerView(recyclerViewJava);*/

        // Below line is used to initialize
        // the variable for the request queue.
        mRequestQueue = Volley.newRequestQueue(requireContext());

        // Getting all the books to populate the array discover page
        getRecommended(ParseUser.getCurrentUser().getString("accessToken"));
        getOtherBooks("inpublisher:Penguin", "&orderBy=newest", penguinBooks, penguinAdapter);
        getOtherBooks("inpublisher:Hachette%20Book%20Group", "&orderBy=relevance", hachetteBooks, hachetteAdapter);
        getOtherBooks("intitle:Android", "&orderBy=relevance", javaBooks, javaAdapter);

        // Getting the card views for the generes part of the discover page
        cFiction = view.findViewById(R.id.cFiction);
        cDrama = view.findViewById(R.id.cDrama);
        cPoetry = view.findViewById(R.id.cPoetry);
        cHumor = view.findViewById(R.id.cHumor);
        cArt = view.findViewById(R.id.cArt);

        // Setting on click listeners for the card views
        cFiction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SearchActivity.class);
                i.putExtra("Genre", "subject:Fiction");
                startActivity(i);
            }
        });

        cDrama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SearchActivity.class);
                i.putExtra("Genre", "subject:Drama");
                startActivity(i);
            }
        });

        cPoetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SearchActivity.class);
                i.putExtra("Genre", "subject:Poetry");
                startActivity(i);
            }
        });

        cHumor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SearchActivity.class);
                i.putExtra("Genre", "subject:Humor");
                startActivity(i);
            }
        });

        cArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SearchActivity.class);
                i.putExtra("Genre", "subject:Art");
                startActivity(i);
            }
        });
    }

    // Getting books from Google according to a query and parameters
    private void getOtherBooks(String query, String parameter, ArrayList<BooksParse> bookList, DiscoverOtherBooksAdapter adapter) {
        // Below line is use to clear cache this
        // will be used when our data is being updated.
        mRequestQueue.getCache().clear();

        // Url for getting data from API in json format
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query + "&maxResults=40" + parameter + "&key=" + BuildConfig.BOOKS_KEY;

        // Creating a new request queue
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Making json object request
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // inside on response method we are extracting all the json data of books
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    Log.i(TAG, "Response: " + response);
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONArray authorsArray = new JSONArray();
                        JSONArray categoriesArray = new JSONArray();
                        String thumbnail = "";
                        String buyLink = "";
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        String googleId = itemsObj.optString("id");
                        JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                        String title = volumeObj.optString("title");
                        String subtitle = volumeObj.optString("subtitle");
                        try {
                            authorsArray = volumeObj.getJSONArray("authors");
                        } catch (JSONException e) {
                            Log.i(TAG, "No author");
                        }
                        try {
                            categoriesArray = volumeObj.getJSONArray("categories");
                        } catch (JSONException e) {
                            Log.i(TAG, "No categories");
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
                                authorsArrayList.add(authorsArray.optString(j));
                            }
                        }
                        ArrayList<String> categoriesArrayList = new ArrayList<>();
                        if (categoriesArray.length() != 0) {
                            for (int x = 0; x < categoriesArray.length(); x++) {
                                categoriesArrayList.add(categoriesArray.optString(x));
                            }
                        }

                        // After extracting all the data I
                        // save it in the modal class of the Book
                        BooksParse bookInfo = new BooksParse();
                        bookInfo.setBook(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink, googleId, categoriesArrayList);

                        // Adding one book to the array list of the books
                        bookList.add(bookInfo);
                    }
                    // Notifying the adapter that the data changed
                    adapter.updateAdapter(bookList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
                    Log.e(TAG, "Error retrieving data from book", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(requireContext(), "Error found is " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error found is: " + error);
            }
        });
        // Adding the JSON object request in the request queue.
        queue.add(booksObjrequest);
    }

    // Getting the Recommended books from the Google algorithm
    private void getRecommended(String accessToken) {
        // Below line is use to clear cache this
        // will be used when our data is being updated.
        mRequestQueue.getCache().clear();

        // Url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/8/volumes?maxResults=40&key=" + BuildConfig.BOOKS_KEY;

        // Creating a new request queue
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // Making json object request
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // inside on response method we are extracting all the json data of books
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    Log.i(TAG, "Bookshelf Response: " + itemsArray);
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONArray authorsArray = new JSONArray();
                        JSONArray categoriesArray = new JSONArray();
                        String thumbnail = "";
                        String buyLink = "";
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        String googleId = itemsObj.optString("id");
                        JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                        String title = volumeObj.optString("title");
                        String subtitle = volumeObj.optString("subtitle");
                        try {
                            authorsArray = volumeObj.getJSONArray("authors");
                        } catch (JSONException e) {
                            Log.i(TAG, "No author", e);
                        }
                        try {
                            categoriesArray = volumeObj.getJSONArray("categories");
                        } catch (JSONException e) {
                            Log.i(TAG, "No categories");
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
                        ArrayList<String> categoriesArrayList = new ArrayList<>();
                        if (categoriesArray.length() != 0) {
                            for (int x = 0; x < categoriesArray.length(); x++) {
                                categoriesArrayList.add(categoriesArray.optString(x));
                            }
                        }

                        // After extracting all the data I
                        // save it in the modal class of the Book
                        BooksParse bookInfo = new BooksParse();
                        bookInfo.setBook(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink , googleId, categoriesArrayList);
                        // Adding one book to the array list of the books
                        recommendedBooks.add(bookInfo);
                    }
                    // Notifying the adapter that the data changed
                    discoverAdapter.updateAdapter(recommendedBooks);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Error
                    Log.e(TAG, "No data found: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // If the access token is expired, get the refresh token to request another one
                if (error.networkResponse.statusCode == 401) {
                    refreshAccessToken();
                } else {
                    // irrecoverable errors. show error to user.
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
        // Adding the JSON object request in the request queue.
        queue.add(booksObjrequest);
    }

    // Getting refresh access token to get a new access token
    private void refreshAccessToken() {
        mRequestQueue.getCache().clear();
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JSONObject params = new JSONObject();
        // Putting the params for the volley request
        try {
            params.put("client_id", getString(R.string.clientId));
            params.put("client_secret", getString(R.string.clientSecret));
            params.put("refresh_token", ParseUser.getCurrentUser().getString("refreshToken"));
            params.put("grant_type", "refresh_token");
        } catch (JSONException ignored) {
            // never thrown in this case
        }

        // Creating the JSON request for the access token
        JsonObjectRequest refreshTokenRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.tokenServerEncodedUrl), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i(TAG, "Success refresh token");
                    // If successful then we use the new access token and save it
                    String accessToken = response.getString("access_token");
                    saveAccessToken(accessToken);
                    getRecommended(accessToken);
                } catch (JSONException e) {
                    Log.e(TAG, "Error using refreshed token " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // If the refresh token failed, then we log out the user and send him to the launch activity
                Log.e("Error on token refresh", new String(error.networkResponse.data));
                revokeAccess();
                clearTokens();
                goLaunchActivity();
            }
        });
        queue.add(refreshTokenRequest);
    }

    // Function used to clear a user's tokens
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

    // Function to revoke access to a user
    public void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener((Activity) requireContext(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logOut();
                    }
                });
    }

    // Used to log out a user (Google and Parse)
    public void logOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener((Activity) requireContext(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ParseUser.logOut();
                    }
                });
    }

    // Going to launchActivity
    public void goLaunchActivity(){
        Intent i = new Intent(getContext(), LaunchActivity.class);
        startActivity(i);
        requireActivity().finish();
    }

    // Saving the recently retrieved access token
    public void saveAccessToken(String refreshToken) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.put("accessToken", refreshToken);
            // Saves the object.
            currentUser.saveInBackground();
        }
    }
}