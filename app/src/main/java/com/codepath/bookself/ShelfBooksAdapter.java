package com.codepath.bookself;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.codepath.bookself.models.UsersBookProgress;

import java.util.ArrayList;

public class ShelfBooksAdapter extends MyBooksAdapter{

    private ArrayList<UsersBookProgress> progressesList;
    private Context context;

    public ShelfBooksAdapter(ArrayList<UsersBookProgress> booksList, Context context) {
        super(booksList, context);
        this.progressesList = booksList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyBooksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.shelf_book_item, parent, false));
    }
}