package com.codepath.bookself.ui.library;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.bookself.BuildConfig;
import com.codepath.bookself.DividerItemDecorator;
import com.codepath.bookself.LaunchActivity;
import com.codepath.bookself.R;
import com.codepath.bookself.ShelvesAdapter;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ShelvesFragment extends Fragment {

    private static final String TAG = "MyBooksFragment";
    private String tokenUrl = "https://oauth2.googleapis.com/token";
    private RequestQueue mRequestQueue;
    private RecyclerView recyclerView;
    private EditText etCompose;
    private LinearLayoutManager layoutManager;
    private boolean justCreated = false;
    GoogleSignInClient mGoogleSignInClient;
    ExtendedFloatingActionButton addShelfButton;
    ShelvesAdapter shelvesAdapter;
    ArrayList<Shelves> allShelves;


    public ShelvesFragment() {
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
        return inflater.inflate(R.layout.fragment_shelves, container, false);
    }

    // Refreshing the list of books when resuming the fragment
    @Override
    public void onResume() {
        super.onResume();
        if (!justCreated) {
            Log.i(TAG, "Shelves updated");
            refreshShelves(false);
        }
        justCreated = false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rvListShelves);

        justCreated = true;
        // Creating the GoogleSignInOptions to be able to sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(getString(R.string.booksScope)))
                .requestServerAuthCode(getString(R.string.clientId), true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Setting the layout manager and the adapter for the Shelves page
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        allShelves = new ArrayList<>();
        shelvesAdapter = new ShelvesAdapter(allShelves, getContext(), this);
        recyclerView.setAdapter(shelvesAdapter);

        // Checking whether to scroll to the bottom of the page or not
        if (getArguments() != null) {
            Toast.makeText(getContext(), "For some reason refreshing shelves", Toast.LENGTH_SHORT).show();
            refreshShelves(true);
        } else {
            getGoogleShelves(ParseUser.getCurrentUser().getString("accessToken"), false);
        }

        // Finding add shelf button and adding on click listener for it
        addShelfButton = view.findViewById(R.id.efabAddShelf2);
        addShelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = requireActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.new_shelf_dialog, null, false);
                etCompose = view.findViewById(R.id.etShelfTitle);
                builder.setView(view)
                        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Getting the title of the new shelf
                        String titleContent = etCompose.getText().toString();
                        if (!titleContent.isEmpty()) {
                            uploadShelf(titleContent);
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Sorry, your title cannot be empty", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    // Refreshing the shelves view with the newest ones
    public void refreshShelves(boolean goingBottom) {
        allShelves.clear();
        shelvesAdapter.updateAdapter(allShelves);
        getGoogleShelves(ParseUser.getCurrentUser().getString("accessToken"), goingBottom);
    }

    // Upload the most recently named shelf
    private void uploadShelf(String titleContent) {
        Shelves newShelf = new Shelves();
        newShelf.put("name", titleContent);
        newShelf.put("amountBooks", 0);
        newShelf.put("user", ParseUser.getCurrentUser());
        newShelf.put("idShelf", -1);
        newShelf.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                refreshShelves(true);
            }
        });
    }

    // Initialized when a shelf is deleted
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG, "GettingResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            refreshShelves(true);
        }
    }

    // Getting all the shelves that belong to the user
    private void getParseShelves(boolean goingToTheBottom) {
        // Specify what type of data we want to query - Shelf.class
        ParseQuery<Shelves> query = ParseQuery.getQuery(Shelves.class);
        // Include data referred by user key
        query.include("progresses.book");
        query.include("progresses.user");
        query.include(UsersBookProgress.KEY_BOOK);
        query.include(UsersBookProgress.KEY_USER);
        query.include(Shelves.KEY_PROGRESSES);
        query.include(Shelves.KEY_USER);
        // Limit query to latest 20 items
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(20);
        // Order posts by creation date (newest first)
        query.addAscendingOrder("createdAt");
        // Start an asynchronous call for posts
        query.findInBackground(new FindCallback<Shelves>() {
            @Override
            public void done(List<Shelves> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // Save received posts to list and notify adapter of new data
                posts.add(null);
                allShelves.addAll(posts);
                shelvesAdapter.updateAdapter(allShelves);
                if (goingToTheBottom) {
                    layoutManager.scrollToPosition(allShelves.size() - 1);
                }
            }
        });
    }

    // Getting the Google Shelf from Google
    private void getGoogleShelves(String accessToken, boolean goingToTheBottom) {
        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(requireContext());

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/7?key=" + BuildConfig.BOOKS_KEY;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // inside on response method we are extracting all our json data.
                Log.i(TAG, "Bookshelf Response: " + response);
                String title = response.optString("title");
                int volumeCounter = response.optInt("volumeCount");
                int id = response.optInt("id");
                Shelves shelve = new Shelves();
                shelve.setGoogleShelf(title, id, volumeCounter);
                allShelves.add(shelve);
                // below line is use to pass our modal
                // class in our array list.
                getParseShelves(goingToTheBottom);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    refreshAccessToken(goingToTheBottom);
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

    private void refreshAccessToken(boolean goingToTheBottom) {
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
                    getGoogleShelves(accessToken, goingToTheBottom);
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