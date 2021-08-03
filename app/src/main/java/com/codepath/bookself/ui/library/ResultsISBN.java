package com.codepath.bookself.ui.library;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.codepath.bookself.DiscoverAdapter;
import com.codepath.bookself.DiscoverOtherBooksAdapter;
import com.codepath.bookself.R;
import com.codepath.bookself.models.BooksParse;

import java.util.ArrayList;

public class ResultsISBN extends AppCompatActivity {

    private ISBNBooksAdapter discoverAdapter;
    private RecyclerView rvIsbnBooks;
    private ArrayList<BooksParse> isbnBooksList;
    public static final String TAG = "ResultsISBNActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_isbn);

        Bundle b = this.getIntent().getExtras();
        isbnBooksList = b.getParcelableArrayList("Books");
        Log.i(TAG, "Book list: " + isbnBooksList);
        rvIsbnBooks = findViewById(R.id.rvIsbnBooks);
        LinearLayoutManager layoutManagerIsbnBooks = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvIsbnBooks.setLayoutManager(layoutManagerIsbnBooks);

        // Setting the adapters with empty arraylists
        discoverAdapter = new ISBNBooksAdapter(isbnBooksList, this);
        rvIsbnBooks.setAdapter(discoverAdapter);
    }
}