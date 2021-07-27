package com.codepath.bookself;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.codepath.bookself.ui.library.ShelvesFragment;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static android.app.Activity.RESULT_OK;

public class AddToShelfAdapter extends RecyclerView.Adapter<AddToShelfAdapter.ViewHolder>{

    private ArrayList<Shelves> shelvesList;
    //private AddToShelfActivity mActivity;
    private Context context;
    private BooksParse globalBook;
    private EditText etCompose;
    private UsersBookProgress globalBookProgress;
    private Shelves shelfToInputTo;
    private boolean userHasProgress;
    public static final String TAG = "AddToShelfAdapter";

    public AddToShelfAdapter(ArrayList<Shelves> shelvesList, Context context, BooksParse globalBook, boolean userHasProgress, UsersBookProgress globalBookProgress) {
        this.shelvesList = shelvesList;
        this.context = context;
        this.globalBook = globalBook;
        this.userHasProgress = userHasProgress;
        this.globalBookProgress = globalBookProgress;
    }

    @NonNull
    @Override
    public AddToShelfAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AddToShelfAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.shelf_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AddToShelfAdapter.ViewHolder holder, int position) {
        Shelves shelf = shelvesList.get(position);
        holder.bind(shelf);
    }

    @Override
    public int getItemCount() {
        return shelvesList.size();
    }

    public void updateAdapter(ArrayList<Shelves> mShelves) {
        this.shelvesList = mShelves;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvNameShelf;
        TextView tvAmountBooks;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameShelf = itemView.findViewById(R.id.tvNameShelf);
            tvAmountBooks = itemView.findViewById(R.id.tvAmountBooks);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                shelfToInputTo = shelvesList.get(position);
                if (shelfToInputTo != null) {
                    Log.i(TAG, "Shelf trying to input to: " + shelfToInputTo.getNameShelf());
                    if (userHasProgress) {
                        checkIfBookIsInShelf(globalBookProgress);
                    } else {
                        getPageInput(v);
                    }
                }
            }
        }

        public void bind(Shelves shelf) {
            if (shelf != null) {
                tvNameShelf.setText(shelf.getNameShelf());
                tvAmountBooks.setText(String.valueOf(shelf.getAmountBooks()));
                ivIcon.setVisibility(View.VISIBLE);
            } else {
                ivIcon.setVisibility(View.GONE);
                tvNameShelf.setText("");
                tvAmountBooks.setText("");
            }
        }
    }

    private void getPageInput(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.pages_progress_dialog, null, false);;
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
                    if (isNumeric(pagesContent) && Integer.parseInt(pagesContent) <= globalBook.getPageCount()) {
                        checkIfBookInDatabase(globalBook, Integer.parseInt(pagesContent));
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(context, "Sorry, the amount of pages is invalid", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "Sorry, the amount of pages cannot be empty", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("[0-9]+");
    }

    private void checkIfBookIsInShelf(UsersBookProgress bookProgress) {
        // save received posts to list and notify adapter of new data
        ParseRelation<UsersBookProgress> relation = shelfToInputTo.getRelation("progresses");

        ParseQuery<UsersBookProgress> query = relation.getQuery();
        query.whereEqualTo("objectId", bookProgress.getObjectId());

        query.findInBackground(new FindCallback<UsersBookProgress>() {
            @Override
            public void done(List<UsersBookProgress> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting progresses", e);
                    return;
                }

                if (objects.isEmpty()) {
                    updateShelf(bookProgress);
                } else{
                    Toast.makeText(context, "This book is already in this shelf", Toast.LENGTH_SHORT).show();
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
                updateShelf(newBookProgress);
            }
        });

    }

    private void updateShelf(UsersBookProgress bookProgress) {
        ParseRelation<UsersBookProgress> relation = shelfToInputTo.getRelation("progresses");
        relation.add(bookProgress);
        shelfToInputTo.increment("amountBooks");
        shelfToInputTo.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Problem saving shelf", e);
                    return;
                }
                Log.i(TAG, "Done updating shelf");
                ((AddToShelfActivity) context).setResult(RESULT_OK);
                ((AddToShelfActivity) context).finish();
            }
        });
    }
}
