package com.codepath.bookself.ui.library;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.bookself.DetailsActivity;
import com.codepath.bookself.R;
import com.codepath.bookself.models.BooksParse;
import com.google.android.material.card.MaterialCardView;

import org.parceler.Parcels;

import java.util.ArrayList;

public class ISBNBooksAdapter extends RecyclerView.Adapter<ISBNBooksAdapter.ISBNBooksAdaptersVh>{

    private ArrayList<BooksParse> booksList;
    private Context context;
    private int lastPosition = -1;

    public ISBNBooksAdapter(ArrayList<BooksParse> booksList, Context context) {
        this.booksList = booksList;
        this.context = context;
    }

    @NonNull
    @Override
    public ISBNBooksAdapter.ISBNBooksAdaptersVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ISBNBooksAdaptersVh(LayoutInflater.from(context).inflate(R.layout.isbn_book_item, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ISBNBooksAdapter.ISBNBooksAdaptersVh holder, int position) {
        BooksParse book = booksList.get(position);
        holder.bind(book, position);
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    public void updateAdapter(ArrayList<BooksParse> mBooks) {
        this.booksList = mBooks;
        notifyDataSetChanged();

    }

    public class ISBNBooksAdaptersVh extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvBookTitle;
        TextView tvAuthors;
        TextView tvDescription;
        TextView tvPublisher;
        TextView tvDatePublished;
        ImageView ivBookImage;
        LinearLayout llContainer;
        MaterialCardView card;

        public ISBNBooksAdaptersVh(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvTitle);
            ivBookImage = itemView.findViewById(R.id.ivBookCover);
            llContainer = itemView.findViewById(R.id.llBookISBN);
            tvAuthors = itemView.findViewById(R.id.tvAuthors);
            tvPublisher = itemView.findViewById(R.id.tvPublisher);
            tvDatePublished = itemView.findViewById(R.id.tvDatePublished);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            card = itemView.findViewById(R.id.card);

            card.setOnClickListener(this);
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

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bind(BooksParse book, int position) {
            //TODO: Put placeholder image for when a book has no image
            tvAuthors.setText(String.join(", " , book.getAuthors()));
            String subtitle = book.getSubtitle();
            String title = book.getTitle();
            if (!subtitle.isEmpty()) {
                subtitle = ": " + subtitle;
            }
            tvBookTitle.setText(title + subtitle);
            tvDatePublished.setText(book.getPublishedDate());
            tvPublisher.setText(book.getPublisher());
            tvDescription.setText(book.getDescription());
            Animation fallInAnimation = AnimationUtils.loadAnimation(context, R.anim.fall_down_animation);
            String httpLink = book.getThumbnail();
            if (!httpLink.equals("")) {
                String httpsLink = httpLink.substring(0,4) + "s" + httpLink.substring(4);
                card.clearAnimation();
                Glide.with(context).load(httpsLink).transform(new RoundedCornersTransformation(30, 10)).dontAnimate().listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.i("Something", "Last animated: " + lastPosition + "Position: " + position);
                        if (position > lastPosition) {
                            Log.i("Something", "Animated " + position);
                            card.setAnimation(fallInAnimation);
                            lastPosition = position;
                        }
                        return false;
                    }
                }).into(ivBookImage);
            } else {
                card.setAnimation(fallInAnimation);
            }
        }
    }
}
