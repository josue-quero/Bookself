package com.codepath.bookself;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import com.bumptech.glide.request.target.Target;
import com.codepath.bookself.models.BooksParse;

import org.parceler.Parcels;

import java.util.ArrayList;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.DiscoverAdaptersVh>{

    private ArrayList<BooksParse> booksList;
    private Context context;

    public DiscoverAdapter(ArrayList<BooksParse> booksList, Context context) {
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
        BooksParse book = booksList.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    public void updateAdapter(ArrayList<BooksParse> mBooks) {
        this.booksList = mBooks;
        notifyDataSetChanged();

    }

    public class DiscoverAdaptersVh extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvBookTitle;
        ImageView ivBookImage;

        public DiscoverAdaptersVh(@NonNull View itemView) {
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
                BooksParse book = booksList.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, DetailsActivity.class);
                // serialize the movie using parceler, use its short name as a key
                intent.putExtra("Progress", false);
                intent.putExtra(BooksParse.class.getSimpleName(), Parcels.wrap(book));
                // show the activity
                context.startActivity(intent);
            }
        }

        public void bind(BooksParse book) {
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
