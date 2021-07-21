package com.codepath.bookself;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import org.parceler.Parcels;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class DetailsActivity extends AppCompatActivity {

    // creating variables for strings,text view, image views and button.
    String title, subtitle, publisher, publishedDate, description, thumbnail, previewLink, infoLink, buyLink;
    int pageCount;
    private ArrayList<String> authors;

    TextView titleTV, subtitleTV, publisherTV, descTV, pageTV, publishDateTV, tvProgress;
    Button previewBtn, buyBtn;
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
        // Checking if there is progress available
        Intent intent = getIntent();
        boolean withProgress = intent.getBooleanExtra("Progress", false);
        // Getting book object
        if (withProgress) {
            bookProgress = (UsersBookProgress) Parcels.unwrap(getIntent().getParcelableExtra(UsersBookProgress.class.getSimpleName()));
            book = bookProgress.getBook();
            int currentProgress = book.getPageCount()/bookProgress.getCurrentPage();
            progressBar.setProgress(currentProgress);
            tvProgress.setText("%" + String.valueOf(currentProgress));

        } else{
            book = (BooksParse) Parcels.unwrap(getIntent().getParcelableExtra(BooksParse.class.getSimpleName()));
            progressBar.setVisibility(View.GONE);
            tvProgress.setVisibility(View.GONE);
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
}