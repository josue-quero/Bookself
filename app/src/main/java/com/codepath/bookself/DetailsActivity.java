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
import com.codepath.bookself.models.UsersBookProgress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
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
    private ImageView bookIV;
    private BooksParse book;
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
            setAddButton(true);

        } else{
            book = (BooksParse) Parcels.unwrap(getIntent().getParcelableExtra(BooksParse.class.getSimpleName()));
            checkBookProgressInDatabase();
        }

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

                if (progress.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
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
                setAddButton(true);
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
                    i.putExtra(UsersBookProgress.class.getSimpleName(), Parcels.wrap(bookProgress));
                } else {
                    i.putExtra(BooksParse.class.getSimpleName(), Parcels.wrap(book));
                }
                i.putExtra("HasProgress", bookHasProgress);
                startActivityForResult(i, 2);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Refreshing page");
            checkBookProgressInDatabase();
        }
    }
}