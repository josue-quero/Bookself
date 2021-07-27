package com.codepath.bookself;

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

import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class AddToShelfActivity extends AppCompatActivity {

    private LinearLayoutManager layoutManager;
    private RecyclerView rvShelvesToAddTo;
    private ExtendedFloatingActionButton efabAddShelf2;
    private EditText etCompose;
    private BooksParse book;
    private String titleContent;
    private UsersBookProgress bookProgress;
    public static final String TAG = "AddToShelfActivity";
    ArrayList<Shelves> allShelves;
    AddToShelfAdapter shelvesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_shelf);
        rvShelvesToAddTo = findViewById(R.id.rvShelvesToAddTo);


        Intent intent = getIntent();
        boolean hasProgress = intent.getBooleanExtra("HasProgress", false);
        // Getting book object
        if (hasProgress) {
            bookProgress = (UsersBookProgress) Parcels.unwrap(getIntent().getParcelableExtra(UsersBookProgress.class.getSimpleName()));
            book = bookProgress.getBook();

        } else{
            book = (BooksParse) Parcels.unwrap(getIntent().getParcelableExtra(BooksParse.class.getSimpleName()));
        }
        layoutManager = new LinearLayoutManager(this);
        rvShelvesToAddTo.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider));
        rvShelvesToAddTo.addItemDecoration(dividerItemDecoration);
        //rvShelvesToAddTo.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        allShelves = new ArrayList<>();
        shelvesAdapter = new AddToShelfAdapter(allShelves, this, book, hasProgress, bookProgress);
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
                            if (hasProgress) {
                                uploadShelfWithBook(bookProgress, titleContent);
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
                setResult(RESULT_OK);
                finish();
            }
        });
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
                // TODO: Get users current page if that page is higher than 0 add to Reading list
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
        newBookProgress.setProgress(page, ParseUser.getCurrentUser(), book);
        newBookProgress.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Problem saving progress", e);
                    return;
                }

                Log.i(TAG, "Done saving progress");
                uploadShelfWithBook(newBookProgress, titleContent);
            }
        });

    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("[0-9]+");
    }
}