package com.codepath.bookself;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.bookself.models.Books;

import java.util.List;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.DiscoverAdaptersVh>{

    private List<Books> booksList;
    private Context context;

    public DiscoverAdapter(List<Books> booksList, Context context) {
        this.booksList = booksList;
        this.context = context;
    }

    @NonNull
    @Override
    public DiscoverAdapter.DiscoverAdaptersVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DiscoverAdaptersVh(LayoutInflater.from(context).inflate(R.layout.book_for_you_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoverAdapter.DiscoverAdaptersVh holder, int position) {
        Books book = booksList.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    public class DiscoverAdaptersVh extends RecyclerView.ViewHolder{

        TextView tvBookTitle;
        ImageView ivBookImage;

        public DiscoverAdaptersVh(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            ivBookImage = itemView.findViewById(R.id.ivBookImage);
        }

        public void bind(Books book) {
            tvBookTitle.setText(book.getTitle());
        }
    }
}
