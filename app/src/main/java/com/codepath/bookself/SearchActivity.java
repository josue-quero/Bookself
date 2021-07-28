package com.codepath.bookself;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.bookself.models.BooksParse;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private ArrayList<BooksParse> bookList;
    private SearchAdapter adapter;
    private RequestQueue mRequestQueue;
    public static final String TAG = "SearchActivity";
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.rvSearchedBooks);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        bookList = new ArrayList<>();
        // Configure the recycler view: layout manager and adapter
        adapter = new SearchAdapter(bookList, this);
        recyclerView.setAdapter(adapter);
        // Get the intent, verify the action and get the query
        String newQuery;
        String parameter;
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            newQuery = query.replaceAll("\\s+", "%20");
            parameter = "&orderBy=relevance";
            Log.i(TAG, "Query: " + newQuery);
        } else {
            newQuery = intent.getStringExtra("Genre");
            parameter = "&orderBy=newest";
        }
        doBookSearch(newQuery, parameter);
    }

    private void doBookSearch(String query, String parameter) {
        bookList = new ArrayList<>();

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(SearchActivity.this);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + query + "&maxResults=40" + parameter + "&key=" + BuildConfig.BOOKS_KEY;

        Log.i(TAG, "This is the line: " + url);

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(SearchActivity.this);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //progressBar.setVisibility(View.GONE);
                // inside on response method we are extracting all our json data.
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    Log.i(TAG, "Response: " + response);
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONArray authorsArray = new JSONArray();
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
                        // after extracting all the data we are
                        // saving this data in our modal class.
                        BooksParse bookInfo = new BooksParse();
                        bookInfo.setBook(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink, googleId);

                        // below line is use to pass our modal
                        // class in our array list.
                        bookList.add(bookInfo);
                    }
                    adapter.updateAdapter(bookList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
                    Toast.makeText(SearchActivity.this, "No Data Found" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(SearchActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error found is: " + error);
            }
        });
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.extOption:
                // Compose icon has been selected
                //Navigate to compose activity
                return true;
            case R.id.itSearch:
                super.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}