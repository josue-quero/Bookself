package com.codepath.bookself;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.UsersBookProgress;

import org.parceler.Parcels;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MyBooksAdapter extends RecyclerView.Adapter<MyBooksAdapter.ViewHolder>{

    private ArrayList<UsersBookProgress> progressesList;
    private Context context;

    public MyBooksAdapter(ArrayList<UsersBookProgress> booksList, Context context) {
        this.progressesList = booksList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyBooksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.book_for_you_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyBooksAdapter.ViewHolder holder, int position) {
        UsersBookProgress progress = progressesList.get(position);
        holder.bind(progress);
    }

    @Override
    public int getItemCount() {
        return progressesList.size();
    }

    public void updateAdapter(ArrayList<UsersBookProgress> mProgress) {
        this.progressesList = mProgress;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvBookTitle;
        ImageView ivBookImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            ivBookImage = itemView.findViewById(R.id.ivBookImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                UsersBookProgress progress = progressesList.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, DetailsActivity.class);
                // serialize the movie using parceler, use its short name as a key
                intent.putExtra("Progress", true);
                intent.putExtra(UsersBookProgress.class.getSimpleName(), Parcels.wrap(progress));
                // show the activity
                context.startActivity(intent);
            }
        }

        public void bind(UsersBookProgress progress) {
            BooksParse book = (BooksParse) progress.getParseObject("book");
            assert book != null;
            tvBookTitle.setText(book.getTitle());
            String httpLink = book.getThumbnail();
            if (!httpLink.equals("")) {
                String httpsLink = httpLink.substring(0,4) + "s" + httpLink.substring(4);
                Log.i("Something", "Link: " + httpsLink);
                Glide.with(context).load(httpsLink).transform(new RoundedCornersTransformation(30, 10)).into(ivBookImage);
            }
        }
    }
}