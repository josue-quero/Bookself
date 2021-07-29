package com.codepath.bookself;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.FindCallback;
import com.parse.GetCallback;
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

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class DetailsActivity extends AppCompatActivity {

    // creating variables for strings,text view, image views and button.
    String title, subtitle, publisher, publishedDate, description, thumbnail, previewLink, infoLink, buyLink;
    int pageCount;
    private ArrayList<String> authors;
    public static final String TAG = "DetailsActivity";
    private String tokenUrl = "https://oauth2.googleapis.com/token";
    private final String clientId = "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com";

    TextView titleTV, subtitleTV, publisherTV, descTV, pageTV, publishDateTV, tvProgress;
    Button previewBtn, buyBtn, btnAddToLibrary;
    private ImageView bookIV, ivHeart;
    private BooksParse book;
    private boolean gettingOut = true, initialStateHeart, lastStateHeart;
    private UsersBookProgress bookProgress;
    private ProgressBar progressBar;
    private RequestQueue mRequestQueue;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/books"))
                .requestServerAuthCode(clientId, true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Get the progress items from the view
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        btnAddToLibrary = findViewById(R.id.btnAddToLibrary);
        // initializing our views..
        titleTV = findViewById(R.id.idTVTitle);
        subtitleTV = findViewById(R.id.idTVSubTitle);
        publisherTV = findViewById(R.id.idTVpublisher);
        descTV = findViewById(R.id.idTVDescription);
        pageTV = findViewById(R.id.idTVNoOfPages);
        publishDateTV = findViewById(R.id.idTVPublishDate);
        previewBtn = findViewById(R.id.idBtnPreview);
        buyBtn = findViewById(R.id.idBtnBuy);
        bookIV = findViewById(R.id.idIVbook);
        ivHeart = findViewById(R.id.ivHeart);
        // Checking if there is progress available
        Intent intent = getIntent();
        boolean fromMyLibrary = intent.getBooleanExtra("FromMyLibrary", false);
        // Getting book object
        if (fromMyLibrary) {
            bookProgress = (UsersBookProgress) Parcels.unwrap(getIntent().getParcelableExtra(UsersBookProgress.class.getSimpleName()));
            book = bookProgress.getBook();
            double currentProgress = ((double) bookProgress.getCurrentPage()/(double) book.getPageCount()) * 100;
            long newCurrentProgress = Math.round(currentProgress);
            progressBar.setProgress((int) newCurrentProgress);
            tvProgress.setText(String.valueOf(newCurrentProgress) + "%");
            initialStateHeart = bookProgress.getHearted();
            lastStateHeart = initialStateHeart;
            if (bookProgress.getHearted()) {
                ivHeart.setImageResource(R.drawable.like);
            }
            setMainView();
            setHeartButton();
            setAddButton(true);
        } else{
            book = (BooksParse) Parcels.unwrap(getIntent().getParcelableExtra(BooksParse.class.getSimpleName()));
            checkBookProgressInDatabase();
        }
    }

    private void setMainView() {
        // getting the data which we have passed from our adapter class.
        title = book.getTitle();
        subtitle = book.getSubtitle();
        publisher = book.getPublisher();
        publishedDate = book.getPublishedDate();
        description = book.getDescription();
        pageCount = book.getPageCount();
        thumbnail = book.getThumbnail();
        previewLink = book.getPreviewLink();
        infoLink = book.getInfoLink();
        buyLink = book.getBuyLink();

        // after getting the data we are setting
        // that data to our text views and image view.
        titleTV.setText(title);
        subtitleTV.setText(subtitle);
        publisherTV.setText(publisher);
        publishDateTV.setText("Published On : " + publishedDate);
        descTV.setText(description);
        pageTV.setText("No Of Pages : " + pageCount);
        String httpLink = book.getThumbnail();
        if (!httpLink.equals("")) {
            String httpsLink = httpLink.substring(0,4) + "s" + httpLink.substring(4);
            Log.i("Something", "Link: " + httpsLink);
            Glide.with(this).load(httpsLink).transform(new RoundedCornersTransformation(30, 10)).into(bookIV);
        }

        // adding on click listener for our preview button.
        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewLink.isEmpty()) {
                    // below toast message is displayed when preview link is not present.
                    Toast.makeText(DetailsActivity.this, "No preview Link present", Toast.LENGTH_SHORT).show();
                    return;
                }
                // if the link is present we are opening
                // that link via an intent.
                Uri uri = Uri.parse(previewLink);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        });

        // initializing on click listener for buy button.
        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buyLink.isEmpty()) {
                    // below toast message is displaying when buy link is empty.
                    Toast.makeText(DetailsActivity.this, "No buy page present for this book", Toast.LENGTH_SHORT).show();
                    return;
                }
                // if the link is present we are opening
                // the link via an intent.
                Uri uri = Uri.parse(buyLink);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        });
    }

    private void checkBookProgressInDatabase() {
        ParseQuery<BooksParse> bookOfInterest = ParseQuery.getQuery(BooksParse.class);
        bookOfInterest.whereEqualTo("googleId", book.getGoogleId());
        // specify what type of data we want to query - UsersBookProgress.class
        ParseQuery<UsersBookProgress> query = ParseQuery.getQuery(UsersBookProgress.class);
        // include data referred by user key
        query.include(UsersBookProgress.KEY_USER);
        query.include(UsersBookProgress.KEY_BOOK);
        // limit query to latest 20 items
        query.whereMatchesQuery("book", bookOfInterest);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        //query.whereEqualTo("book.googleId", book.getGoogleId());
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<UsersBookProgress>() {
            @Override
            public void done(List<UsersBookProgress> progress, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                setHeartButton();
                if (progress.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    initialStateHeart = false;
                    lastStateHeart = initialStateHeart;
                    setMainView();
                    setAddButton(false);
                    return;
                }
                // Put the progress of this book
                Log.i(TAG, "This book has progress" + progress);
                bookProgress = progress.get(0);
                double currentProgress = ((double) bookProgress.getCurrentPage()/(double) book.getPageCount()) * 100;
                long newCurrentProgress = Math.round(currentProgress);
                progressBar.setProgress((int) newCurrentProgress);
                tvProgress.setText(String.valueOf(newCurrentProgress) + "%");
                progressBar.setVisibility(View.VISIBLE);
                tvProgress.setVisibility(View.VISIBLE);
                initialStateHeart = bookProgress.getHearted();
                lastStateHeart = initialStateHeart;
                // Check if the book has been liked by the user
                if (bookProgress.getHearted()) {
                    ivHeart.setImageResource(R.drawable.like);
                }
                setMainView();
                setAddButton(true);
            }
        });
    }

    private void setHeartButton() {
        ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastStateHeart) {
                    ivHeart.setImageResource(R.drawable.heart);
                    lastStateHeart = false;
                } else {
                    ivHeart.setImageResource(R.drawable.like);
                    lastStateHeart = true;
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gettingOut && initialStateHeart != lastStateHeart) {
            if (bookProgress == null) {
                Log.i(TAG,"Fresh book");
                checkIfBookInDatabase(book, 0);
            } else {
                bookProgress.setHearted(lastStateHeart);
                updateBookProgress(bookProgress);
            }
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
                    Toast.makeText(DetailsActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DetailsActivity.this, "No Data Found" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(DetailsActivity.this, "Error found is " + error, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DetailsActivity.this, "Error using refreshed token " + e, Toast.LENGTH_SHORT).show();
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

    private void updateBookProgress(UsersBookProgress bookProgress) {
        bookProgress.saveInBackground();
        getFavoritesShelf(bookProgress);
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
                UsersBookProgress newBookProgress = new UsersBookProgress();
                if (pagesAmount > 0){
                    Date today = new Date();
                    newBookProgress.setLastRead(today);
                }
                newBookProgress.setProgress(pagesAmount, ParseUser.getCurrentUser(), retrievedBook, lastStateHeart);
                saveProgress(newBookProgress);

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
                UsersBookProgress newBookProgress = new UsersBookProgress();
                if (pagesAmount > 0){
                    Date today = new Date();
                    newBookProgress.setLastRead(today);
                }
                newBookProgress.setProgress(pagesAmount, ParseUser.getCurrentUser(), book, lastStateHeart);
                saveProgress(newBookProgress);
            }
        });
    }

    private void saveProgress(UsersBookProgress newBookProgress) {
        newBookProgress.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Problem saving progress", e);
                    return;
                }

                Log.i(TAG, "Done saving favorited progress");

                getFavoritesShelf(newBookProgress);
            }
        });
    }

    private void getFavoritesShelf(UsersBookProgress bookProgress) {
        manageGoogleUpload(bookProgress.getBook());
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
        query.whereEqualTo("name", "Favorites");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Shelves>() {
            @Override
            public void done(List<Shelves> shelves, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting Shelves", e);
                    return;
                }

                if (lastStateHeart) {
                    uploadBookToFavorites(shelves.get(0), bookProgress);
                } else {
                    deleteBookFromFavorites(shelves.get(0), bookProgress);
                }

            }
        });
    }

    private void manageGoogleUpload(BooksParse book) {
        if (book.getEbookId() == null) {
            if (book.getBuyLink().isEmpty()) {
                lookForEbookVersion(book);
            } else {
                book.setEbookId(book.getGoogleId());
                book.saveInBackground();
                updateGoogleFavorites(ParseUser.getCurrentUser().getString("accessToken"), book, !lastStateHeart);
            }
        } else {
            updateGoogleFavorites(ParseUser.getCurrentUser().getString("accessToken"), book, !lastStateHeart);
        }
    }

    private void deleteBookFromFavorites(Shelves shelf, UsersBookProgress bookProgress) {
        ParseRelation<UsersBookProgress> relation = shelf.getRelation("progresses");
        relation.remove(bookProgress);
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

    private void uploadBookToFavorites(Shelves shelf, UsersBookProgress bookProgress) {
        shelf.increment("amountBooks");
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
            }
        });
    }


    private void setAddButton(boolean bookHasProgress) {
        // Add this book to your library
        if (bookHasProgress) {
            btnAddToLibrary.setText("ADD TO SHELF");
        }

        Log.i(TAG, "Button set");

        btnAddToLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked to save");
                Intent i = new Intent(v.getContext(), AddToShelfActivity.class);
                if (bookHasProgress) {
                    bookProgress.setHearted(lastStateHeart);
                    if (initialStateHeart != lastStateHeart) {
                        i.putExtra("heartHasChanged", lastStateHeart);
                    }
                    i.putExtra(UsersBookProgress.class.getSimpleName(), Parcels.wrap(bookProgress));
                } else {
                    i.putExtra("isLiked", lastStateHeart);
                    i.putExtra(BooksParse.class.getSimpleName(), Parcels.wrap(book));
                }
                i.putExtra("HasProgress", bookHasProgress);
                gettingOut = false;
                startActivityForResult(i, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            gettingOut = true;
            Log.i(TAG, "Refreshing page");
            checkBookProgressInDatabase();
        }
    }
}