package com.codepath.bookself;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
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

import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class DetailsActivity extends AppCompatActivity {

    // creating variables for strings,text view, image views and button.
    String title, subtitle, publisher, publishedDate, description, thumbnail, previewLink, infoLink, buyLink;
    int pageCount;
    private ArrayList<String> authors;
    public static final String TAG = "DetailsActivity";

    TextView titleTV, subtitleTV, publisherTV, descTV, pageTV, publishDateTV, tvProgress;
    Button previewBtn, buyBtn, btnAddToLibrary;
    private ImageView bookIV, ivHeart;
    private BooksParse book;
    private boolean gettingOut = true, initialStateHeart, lastStateHeart;
    private UsersBookProgress bookProgress;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

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

    /*
    private void getUpdatedBookProgress(UsersBookProgress oldBookProgress) {
        ParseQuery<UsersBookProgress> query = ParseQuery.getQuery(UsersBookProgress.class);
        query.include(UsersBookProgress.KEY_BOOK);
        query.getInBackground(oldBookProgress.getObjectId(), new GetCallback<UsersBookProgress>() {
            @Override
            public void done(UsersBookProgress object, ParseException e) {
                bookProgress = object;
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
            }
        });
    }*/

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

    private void updateBookProgress(UsersBookProgress bookProgress) {
        bookProgress.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error updating progress");
                }

                Log.i(TAG, "Book progress updated");
                getFavoritesShelf(bookProgress);
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