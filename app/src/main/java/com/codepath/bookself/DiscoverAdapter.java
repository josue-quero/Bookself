package com.codepath.bookself;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.DiscoverAdaptersVh>{

    private ArrayList<BooksParse> booksList;
    private Context context;
    private int lastPosition = -1;

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

    public class DiscoverAdaptersVh extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvBookTitle;
        ImageView ivBookImage;
        RelativeLayout rvContainer;
        ImageView ivWishlist;
        AnimatedVectorDrawableCompat avdc;
        AnimatedVectorDrawable avd;
        public static final String TAG = "SearchAdapter";

        public DiscoverAdaptersVh(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            ivBookImage = itemView.findViewById(R.id.ivBookImage);
            rvContainer = itemView.findViewById(R.id.rvContainer);
            ivWishlist = itemView.findViewById(R.id.wishlist);
            itemView.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        int position = getBindingAdapterPosition();
                        // make sure the position is valid, i.e. actually exists in the view
                        if (position != RecyclerView.NO_POSITION) {
                            // get the movie at the position, this won't work if the class is static
                            BooksParse book = booksList.get(position);
                            addToWishlist(book);
                        }
                        return super.onDoubleTap(e);
                    }
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        onClick(itemView);
                        return super.onSingleTapConfirmed(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
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

        public void bind(BooksParse book, int position) {
            Animation fallInAnimation = AnimationUtils.loadAnimation(context, R.anim.fall_down_animation);
            rvContainer.clearAnimation();
            String httpLink = book.getThumbnail();
            if (!httpLink.equals("")) {
                String httpsLink = httpLink.substring(0,4) + "s" + httpLink.substring(4);
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
                            tvBookTitle.setText(book.getTitle());
                            rvContainer.setAnimation(fallInAnimation);
                            lastPosition = position;
                        }
                        return false;
                    }
                }).into(ivBookImage);
            } else {
                Glide.with(context).load(R.drawable.book_cover_placeholder).transform(new RoundedCornersTransformation(30, 10)).dontAnimate().into(ivBookImage);
                tvBookTitle.setText(book.getTitle());
                rvContainer.setAnimation(fallInAnimation);
            }
        }

        // Upload the most recently named shelf
        private void addToWishlist(BooksParse bookParse) {
            checkBookProgressInDatabase(bookParse);
        }

        private void checkBookProgressInDatabase(BooksParse book) {
            ParseQuery<BooksParse> bookOfInterest = ParseQuery.getQuery(BooksParse.class);
            bookOfInterest.whereEqualTo("googleId", book.getGoogleId());
            // specify what type of data we want to query - UsersBookProgress.class
            ParseQuery<UsersBookProgress> query = ParseQuery.getQuery(UsersBookProgress.class);
            // include data referred by user key
            query.include(UsersBookProgress.KEY_USER);
            query.include(UsersBookProgress.KEY_BOOK);
            // limit query to latest 20 items
            query.whereMatchesQuery("book", bookOfInterest);
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            //query.whereEqualTo("book.googleId", book.getGoogleId());
            query.setLimit(20);
            // order posts by creation date (newest first)
            query.addDescendingOrder("createdAt");
            // start an asynchronous call for posts
            query.findInBackground(new FindCallback<UsersBookProgress>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void done(List<UsersBookProgress> progress, ParseException e) {
                    // check for errors
                    if (e != null) {
                        Log.e(TAG, "Issue with getting posts", e);
                        return;
                    }
                    if (progress.isEmpty()) {
                        checkIfBookInDatabase(book, 0);
                        final Drawable drawable = ivWishlist.getDrawable();
                        ivWishlist.setAlpha(1f);
                        if (drawable instanceof AnimatedVectorDrawableCompat) {
                            avdc = (AnimatedVectorDrawableCompat) drawable;
                            avdc.start();
                        } else if(drawable instanceof  AnimatedVectorDrawable) {
                            avd = (AnimatedVectorDrawable) drawable;
                            avd.start();
                        }
                        return;
                    } else if (progress.get(0).getWishlist()) {
                        Toast.makeText(context, "Already wishlisted", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(context, "Already owned", Toast.LENGTH_SHORT).show();
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
                    Log.i(TAG, "This book has been uploaded previously" + bookFound);
                    BooksParse retrievedBook = bookFound.get(0);
                    UsersBookProgress newBookProgress = new UsersBookProgress();
                    if (pagesAmount > 0){
                        Date today = new Date();
                        newBookProgress.setLastRead(today);
                    }
                    newBookProgress.setProgress(pagesAmount, ParseUser.getCurrentUser(), retrievedBook, false, true);
                    saveProgress(newBookProgress);

                }
            });
        }

        private void saveProgress(UsersBookProgress newBookProgress) {
            newBookProgress.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.i(TAG, "Problem saving progress", e);
                        return;
                    }

                    Log.i(TAG, "Done saving progress");

                    getShelf(newBookProgress, "Wishlist", false);
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
                    UsersBookProgress newBookProgress = new UsersBookProgress();
                    if (pagesAmount > 0){
                        Date today = new Date();
                        newBookProgress.setLastRead(today);
                    }
                    newBookProgress.setProgress(pagesAmount, ParseUser.getCurrentUser(), book, false, true);
                    saveProgress(newBookProgress);
                }
            });
        }

        private void getShelf(UsersBookProgress newBookProgress, String nameShelf, boolean delete) {
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
            query.whereEqualTo("name", nameShelf);
            // start an asynchronous call for posts
            query.findInBackground(new FindCallback<Shelves>() {
                @Override
                public void done(List<Shelves> shelves, ParseException e) {
                    // check for errors
                    if (e != null) {
                        Log.e(TAG, "Issue with getting Shelves", e);
                        return;
                    }
                    uploadBookInShelf(shelves.get(0), newBookProgress, delete);
                }
            });
        }

        private void uploadBookInShelf(Shelves shelf, UsersBookProgress bookProgress, boolean delete) {
            ParseRelation<UsersBookProgress> relation = shelf.getRelation("progresses");
            if (delete) {
                shelf.increment("amountBooks", -1);
                relation.remove(bookProgress);
            } else {
                relation.add(bookProgress);
                shelf.increment("amountBooks");
            }
            shelf.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.i(TAG, "Problem saving shelf", e);
                        return;
                    }
                    Log.i(TAG, "Done updating shelf");
                }
            });
        }
    }
}
