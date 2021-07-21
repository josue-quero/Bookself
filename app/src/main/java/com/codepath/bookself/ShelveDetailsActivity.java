package com.codepath.bookself;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ShelveDetailsActivity extends AppCompatActivity {

    private Shelves shelf;
    public static final String TAG = "ShelveDetailsActivity";
    private RecyclerView recyclerView;
    MyBooksAdapter myBooksAdapter;
    ArrayList<UsersBookProgress> allProgresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelve_details);

        recyclerView = findViewById(R.id.rvShelfBooks);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        allProgresses = new ArrayList<>();
        // TODO: Create a new adapter for the library
        myBooksAdapter = new MyBooksAdapter(allProgresses, this);
        recyclerView.setAdapter(myBooksAdapter);
        // Getting Shelve object
        shelf = (Shelves) Parcels.unwrap(getIntent().getParcelableExtra(Shelves.class.getSimpleName()));
        int googleId = shelf.getGoogleId();
        if (googleId == -1) {
            Log.i(TAG, "Filing with googleId: " + String.valueOf(shelf.getGoogleId()));
            getParseShelf();
        } else {
            //getGoogleShelf(googleId);
        }
    }

    private void getParseShelf() {
        // save received posts to list and notify adapter of new data
        ParseRelation<UsersBookProgress> relation = shelf.getRelation("progresses");

        ParseQuery<UsersBookProgress> query = relation.getQuery();
        query.include(UsersBookProgress.KEY_BOOK);
        query.include(UsersBookProgress.KEY_USER);

        query.findInBackground(new FindCallback<UsersBookProgress>() {
            @Override
            public void done(List<UsersBookProgress> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting progresses", e);
                    return;
                }
                allProgresses.addAll(objects);
                myBooksAdapter.updateAdapter(allProgresses);
            }
        });
    }
}