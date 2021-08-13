package com.codepath.bookself;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toolbar;

import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.codepath.bookself.ui.library.ShelvesFragment;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.victor.loading.book.BookLoading;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ShelveDetailsActivity extends AppCompatActivity {

    private Shelves shelf;
    public static final String TAG = "ShelveDetailsActivity";
    private RecyclerView recyclerView;
    private BookLoading bookLoading;
    private boolean firstTime;
    ShelfBooksAdapter shelfBooksAdapter;
    ArrayList<UsersBookProgress> allProgresses;

    @Override
    protected void onResume() {
        if (!firstTime){
            allProgresses.clear();
            shelfBooksAdapter.updateAdapter(allProgresses);
            getParseShelf();
        }
        firstTime = false;
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelve_details);


        firstTime = true;
        shelf = (Shelves) Parcels.unwrap(getIntent().getParcelableExtra(Shelves.class.getSimpleName()));
        androidx.appcompat.widget.Toolbar mActionBarToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(shelf.getNameShelf());

        // Finding and starting the loading icon
        bookLoading = findViewById(R.id.bookloading);
        bookLoading.start();
        recyclerView = findViewById(R.id.rvShelfBooks);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        allProgresses = new ArrayList<>();
        shelfBooksAdapter = new ShelfBooksAdapter(allProgresses, this, shelf);
        recyclerView.setAdapter(shelfBooksAdapter);
        // Getting Shelve object
        int googleId = shelf.getGoogleId();
        if (googleId != 1) {
            Log.i(TAG, "Filing with googleId: " + String.valueOf(shelf.getGoogleId()));
            getParseShelf();
        } else {
            //getGoogleShelf(googleId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shelf_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                return true;
            case R.id.delete:
                deleteShelf();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteShelf() {
        shelf.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error deleting: ", e);
                    return;
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void getParseShelf() {
        // save received posts to list and notify adapter of new data
        ParseRelation<UsersBookProgress> relation = shelf.getRelation("progresses");

        ParseQuery<UsersBookProgress> query = relation.getQuery();
        query.include(UsersBookProgress.KEY_BOOK);
        query.include(UsersBookProgress.KEY_USER);
        query.addDescendingOrder("updatedAt");

        query.findInBackground(new FindCallback<UsersBookProgress>() {
            @Override
            public void done(List<UsersBookProgress> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting progresses", e);
                    return;
                }
                allProgresses.addAll(objects);
                shelfBooksAdapter.updateAdapter(allProgresses);
                bookLoading.setVisibility(View.GONE);
                bookLoading.stop();
            }
        });
    }
}