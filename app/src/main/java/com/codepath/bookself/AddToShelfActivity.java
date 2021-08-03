package com.codepath.bookself;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codepath.bookself.models.BooksParse;
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
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class AddToShelfActivity extends AppCompatActivity {

    private LinearLayoutManager layoutManager;
    private RecyclerView rvShelvesToAddTo;
    private ExtendedFloatingActionButton efabAddShelf2, efabAddToLibrary;
    private EditText etCompose;
    private BooksParse book;
    private boolean isLiked, heartHasChanged;
    private String titleContent;
    private UsersBookProgress bookProgress;
    public static final String TAG = "AddToShelfActivity";
    private String tokenUrl = "https://oauth2.googleapis.com/token";
    private final String clientId = "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com";
    boolean onlyAddToLibrary;
    private RequestQueue mRequestQueue;
    GoogleSignInClient mGoogleSignInClient;
    ArrayList<Shelves> allShelves;
    AddToShelfAdapter shelvesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_shelf);
        rvShelvesToAddTo = findViewById(R.id.rvShelvesToAddTo);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/books"))
                .requestServerAuthCode(clientId, true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent intent = getIntent();
        boolean hasProgress = intent.getBooleanExtra("HasProgress", false);
        efabAddToLibrary = findViewById(R.id.efabAddToLibrary);
        onlyAddToLibrary = false;
        // Getting book object
        if (hasProgress) {
            bookProgress = (UsersBookProgress) Parcels.unwrap(getIntent().getParcelableExtra(UsersBookProgress.class.getSimpleName()));
            heartHasChanged = intent.getBooleanExtra("heartHasChanged", false);
            book = bookProgress.getBook();
            efabAddToLibrary.setVisibility(View.GONE);
        } else{
            book = (BooksParse) Parcels.unwrap(getIntent().getParcelableExtra(BooksParse.class.getSimpleName()));
            isLiked = intent.getBooleanExtra("isLiked", false);
            if (isLiked) {
                efabAddToLibrary.setVisibility(View.GONE);
            } else {
                efabAddToLibrary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onlyAddToLibrary = true;
                        if (book.getPageCount() != 0) {
                            getPageInput(v);
                        }
                    }
                });
            }
        }
        layoutManager = new LinearLayoutManager(this);
        rvShelvesToAddTo.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider));
        rvShelvesToAddTo.addItemDecoration(dividerItemDecoration);
        allShelves = new ArrayList<>();
        shelvesAdapter = new AddToShelfAdapter(allShelves, this, book, hasProgress, bookProgress, isLiked, heartHasChanged);
        rvShelvesToAddTo.setAdapter(shelvesAdapter);

        // Finding and adding on click listener for the add shelf button.
        efabAddShelf2 = findViewById(R.id.efabAddShelf2);
        efabAddShelf2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(AddToShelfActivity.this);
                LayoutInflater inflater = getLayoutInflater();
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
                        titleContent = etCompose.getText().toString();
                        if (!titleContent.isEmpty()) {
                            if (hasProgress || book.getPageCount() == 0) {
                                if (heartHasChanged) {
                                    updateBookProgress(bookProgress);
                                } else {
                                    uploadShelfWithBook(bookProgress, titleContent);
                                }
                            } else {
                                getPageInput(v);
                            }
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(AddToShelfActivity.this, "Sorry, your title cannot be empty", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        getParseShelves();
    }

    private void updateBookProgress(UsersBookProgress bookProgress) {
        bookProgress.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                uploadShelfWithBook(bookProgress, titleContent);
            }
        });
    }

    private void getParseShelves() {
        // specify what type of data we want to query - Shelf.class
        ParseQuery<Shelves> query = ParseQuery.getQuery(Shelves.class);
        // include data referred by user key
        query.include("progresses.book");
        query.include("progresses.user");
        query.include(UsersBookProgress.KEY_BOOK);
        query.include(UsersBookProgress.KEY_USER);
        query.include(Shelves.KEY_PROGRESSES);
        query.include(Shelves.KEY_USER);
        // limit query to latest 20 items
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereNotEqualTo("idShelf", 0);
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addAscendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Shelves>() {
            @Override
            public void done(List<Shelves> shelves, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting Shelves", e);
                    return;
                }

                // save received posts to list and notify adapter of new data
                shelves.add(null);
                allShelves.addAll(shelves);
                shelvesAdapter.updateAdapter(allShelves);
            }
        });
    }

    private void uploadShelfWithBook(UsersBookProgress bookProgress, String shelfName) {
        Shelves shelf = new Shelves();
        shelf.setParseShelf(shelfName, -1, 1, ParseUser.getCurrentUser());
        ParseRelation<UsersBookProgress> relation = shelf.getRelation("progresses");
        relation.add(bookProgress);
        shelf.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Problem saving shelf", e);
                    return;
                }
                Log.i(TAG, "Done updating shelf");
                if (bookProgress.getHearted() && heartHasChanged) {
                    getShelf(bookProgress, "Favorite", false);
                    manageGoogleUpload(bookProgress.getBook(), false);
                } else if (!bookProgress.getHearted() && heartHasChanged){
                    getShelf(bookProgress, "Favorite", true);
                    manageGoogleUpload(bookProgress.getBook(), true);
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void manageGoogleUpload(BooksParse book, boolean delete) {
        if (book.getEbookId().isEmpty()) {
            if (book.getBuyLink().isEmpty()) {
                lookForEbookVersion(book);
            } else {
                book.setEbookId(book.getGoogleId());
                book.saveInBackground();
                updateGoogleFavorites(ParseUser.getCurrentUser().getString("accessToken"), book, delete);
            }
        } else {
            updateGoogleFavorites(ParseUser.getCurrentUser().getString("accessToken"), book, delete);
        }
    }

    private void updateGoogleFavorites(String accessToken, BooksParse currentBook, boolean delete) {
        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(this);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();
        String parameter;
        if (delete){
            parameter = "removeVolume";
        } else {
            parameter = "addVolume";
        }

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/7/" + parameter + "?volumeId=" + currentBook.getEbookId() + "&key=" + BuildConfig.BOOKS_KEY;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(this);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Correctly updated Google favorite books" + "Deleted: "+ delete);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    refreshAccessToken(currentBook, delete);
                } else {
                    // irrecoverable errors. show error to user.
                    Toast.makeText(AddToShelfActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error found is: " + error);
                }

                if (error.networkResponse.statusCode == 403) {
                    lookForEbookVersion(currentBook);
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

    private void lookForEbookVersion(BooksParse currentBook) {
        List<BooksParse> bookList = new ArrayList<>();

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(this);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        String title = currentBook.getTitle();
        String newTitle = title.replaceAll("\\s+", "%20");

        String author = currentBook.getAuthors().get(0);
        String newAuthor = author.replaceAll("\\s+", "%20");
        String authorParameter;
        if (author.isEmpty()) {
            authorParameter = "";
        } else {
            authorParameter = "+inauthor:" + newAuthor;
        }

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + currentBook.getTitle() + "+intitle:" + currentBook.getTitle() + authorParameter + "&filter=ebooks&maxResults=40" + "&key=" + BuildConfig.BOOKS_KEY;

        Log.i(TAG, "This is the line: " + url);

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(this);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //progressBar.setVisibility(View.GONE);
                // inside on response method we are extracting all our json data.
                try {
                    if (response.getInt("totalItems") == 0) {
                        Log.i(TAG, "Unable to find ebook version");
                    } else {
                        JSONArray itemsArray = response.getJSONArray("items");
                        for (int i = 0; i < itemsArray.length(); i++) {
                            Log.i(TAG, "Response: " + response);
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
                                    authorsArrayList.add(authorsArray.optString(j));
                                }
                            }
                            ArrayList<String> categoriesArrayList = new ArrayList<>();
                            if (categoriesArray.length() != 0) {
                                for (int x = 0; x < categoriesArray.length(); x++) {
                                    categoriesArrayList.add(categoriesArray.optString(x));
                                }
                            }
                            // after extracting all the data we are
                            // saving this data in our modal class.
                            BooksParse bookInfo = new BooksParse();
                            bookInfo.setBook(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink, googleId, categoriesArrayList);

                            // below line is use to pass our modal
                            // class in our array list.
                            if (!buyLink.isEmpty()) {
                                currentBook.setEbookId(bookInfo.getGoogleId());
                                currentBook.saveInBackground();
                                updateGoogleFavorites(ParseUser.getCurrentUser().getString("accessToken"), currentBook, false);
                                return;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
                    Toast.makeText(AddToShelfActivity.this, "No Data Found" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(AddToShelfActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error found is: " + error);
            }
        });
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
    }

    private void refreshAccessToken(BooksParse book, boolean delete) {
        RequestQueue queue = Volley.newRequestQueue(this);
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
                    updateGoogleFavorites(accessToken, book, delete);
                } catch (JSONException e) {
                    Toast.makeText(AddToShelfActivity.this, "Error using refreshed token " + e, Toast.LENGTH_SHORT).show();
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
                .addOnCompleteListener( this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logOut();
                    }
                });
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

    public void goLaunchActivity(){
        Intent i = new Intent(this, LaunchActivity.class);
        startActivity(i);
        finish();
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

    private void getPageInput(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.pages_progress_dialog, null, false);;
        etCompose = view.findViewById(R.id.etPagesAmount);
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
                String pagesContent = etCompose.getText().toString();
                if (!pagesContent.isEmpty()) {
                    if (isNumeric(pagesContent) && Integer.parseInt(pagesContent) <= book.getPageCount()) {
                        checkIfBookInDatabase(book, Integer.parseInt(pagesContent));
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(AddToShelfActivity.this, "Sorry, the amount of pages is invalid", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AddToShelfActivity.this, "Sorry, the amount of pages cannot be empty", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkIfBookInDatabase(BooksParse book, int pagesAmount) {
        ParseQuery<BooksParse> bookOfInterest = ParseQuery.getQuery(BooksParse.class);
        bookOfInterest.whereEqualTo("googleId", book.getGoogleId());
        // specify what type of data we want to query - UsersBookProgress.class
        // include data referred by user k
        bookOfInterest.setLimit(20);
        // order posts by creation date (newest first)
        bookOfInterest.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        bookOfInterest.findInBackground(new FindCallback<BooksParse>() {
            @Override
            public void done(List<BooksParse> bookFound, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                if (bookFound.isEmpty()) {
                    saveBookInDatabase(book, pagesAmount);
                    return;
                }
                // Creating progress for the book
                Log.i(TAG, "This book has been uploaded previously" + bookFound);
                BooksParse retrievedBook = bookFound.get(0);
                createAndSaveProgress(retrievedBook, pagesAmount);

            }
        });
    }

    private void saveBookInDatabase(BooksParse book, int pagesAmount) {
        book.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Problem saving book", e);
                    return;
                }

                Log.i(TAG, "Done saving book");
                createAndSaveProgress(book, pagesAmount);
            }
        });
    }

    private void createAndSaveProgress(BooksParse book, int page) {
        UsersBookProgress newBookProgress = new UsersBookProgress();
        String shelf = "";
        if (page != 0){
            if (page == book.getPageCount()) {
                newBookProgress.setRead(true);
                shelf = "Read";
                ParseUser.getCurrentUser().increment("bookReadAmount");
            } else {
                shelf = "Reading";
                ParseUser.getCurrentUser().increment("pagesReadAmount", page);
            }
            ParseUser.getCurrentUser().saveInBackground();
            Date today = new Date();
            newBookProgress.setLastRead(today);
        }
        newBookProgress.setProgress(page, ParseUser.getCurrentUser(), book, isLiked);
        Log.i(TAG, "Type of shelf to save to: " + shelf);
        String finalShelf = shelf;
        newBookProgress.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Problem saving progress", e);
                    return;
                }

                Log.i(TAG, "Done saving progress");
                if (!finalShelf.isEmpty()) {
                    getShelf(newBookProgress, finalShelf, false);
                }
                if (!onlyAddToLibrary) {
                    uploadShelfWithBook(newBookProgress, titleContent);
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    private void getShelf(UsersBookProgress newBookProgress, String nameShelf, boolean delete) {
        // specify what type of data we want to query - Shelf.class
        ParseQuery<Shelves> query = ParseQuery.getQuery(Shelves.class);
        // include data referred by user key
        query.include("progresses.book");
        query.include("progresses.user");
        query.include(UsersBookProgress.KEY_BOOK);
        query.include(UsersBookProgress.KEY_USER);
        query.include(Shelves.KEY_PROGRESSES);
        query.include(Shelves.KEY_USER);
        // limit query to latest 20 items
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("name", nameShelf);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Shelves>() {
            @Override
            public void done(List<Shelves> shelves, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting Shelves", e);
                    return;
                }
                uploadBookInShelf(shelves.get(0), newBookProgress, delete);
            }
        });
    }

    private void uploadBookInShelf(Shelves shelf, UsersBookProgress bookProgress, boolean delete) {
        Log.i(TAG, "Updating: " + shelf.getNameShelf());
        ParseRelation<UsersBookProgress> relation = shelf.getRelation("progresses");
        if (delete) {
            shelf.increment("amountBooks", -1);
            relation.remove(bookProgress);
        } else {
            relation.add(bookProgress);
            shelf.increment("amountBooks");
        }
        shelf.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Problem saving shelf", e);
                    return;
                }
                Log.i(TAG, "Done updating shelf");
            }
        });
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("[0-9]+");
    }
}