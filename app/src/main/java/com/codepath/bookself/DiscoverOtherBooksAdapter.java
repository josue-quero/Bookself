package com.codepath.bookself;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.codepath.bookself.models.BooksParse;

import java.util.ArrayList;

public class DiscoverOtherBooksAdapter extends DiscoverAdapter {

    private ArrayList<BooksParse> booksList;
    private Context context;

    public DiscoverOtherBooksAdapter(ArrayList<BooksParse> booksList, Context context) {
        super(booksList, context);
        this.booksList = booksList;
        this.context = context;
    }

    @NonNull
    @Override
    public DiscoverAdapter.DiscoverAdaptersVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiscoverAdaptersVh(LayoutInflater.from(context).inflate(R.layout.discover_books_item, parent, false));
    }
}
