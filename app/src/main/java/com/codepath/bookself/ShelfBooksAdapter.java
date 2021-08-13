package com.codepath.bookself;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.material.card.MaterialCardView;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ShelfBooksAdapter extends RecyclerView.Adapter<ShelfBooksAdapter.ViewHolder>{

    private ArrayList<UsersBookProgress> progressesList;
    private Context context;
    private Shelves shelf;
    private androidx.appcompat.view.ActionMode mActionMode;
    private boolean contextualMode = false;
    private ArrayList<UsersBookProgress> selectedList;
    private ArrayList<MaterialCardView> selectedCardViews;
    private RequestQueue mRequestQueue;
    public static final String TAG = "MyBooksAdapter";
    private String tokenUrl = "https://oauth2.googleapis.com/token";

    public ShelfBooksAdapter(ArrayList<UsersBookProgress> booksList, Context context, Shelves shelf) {
        this.progressesList = booksList;
        this.context = context;
        this.shelf = shelf;
        this.selectedCardViews = new ArrayList<>();
        this.selectedList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ShelfBooksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ShelfBooksAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.shelf_book_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShelfBooksAdapter.ViewHolder holder, int position) {
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
        MaterialCardView materialCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            ivBookImage = itemView.findViewById(R.id.ivBookImage);
            materialCardView = itemView.findViewById(R.id.bookCard);
            ivBookImage.setVisibility(View.VISIBLE);
            materialCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!contextualMode){
                        mActionMode = ((AppCompatActivity) context).startSupportActionMode(new ShelfBooksAdapter.ViewHolder.ContextualCallback());
                    }
                    if (materialCardView.isChecked()) {
                        selectedList.remove(progressesList.get(getBindingAdapterPosition()));
                        selectedCardViews.remove(materialCardView);
                    } else {
                        selectedList.add(progressesList.get(getBindingAdapterPosition()));
                        selectedCardViews.add(materialCardView);
                    }
                    materialCardView.setChecked(!materialCardView.isChecked());
                    mActionMode.setTitle("Books selected: " + selectedList.size());
                    return true;
                }
            });
            materialCardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getBindingAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION && !contextualMode) {
                // get the movie at the position, this won't work if the class is static
                UsersBookProgress progress = progressesList.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, DetailsActivity.class);
                // serialize the movie using parceler, use its short name as a key
                intent.putExtra("FromMyLibrary", true);
                intent.putExtra(UsersBookProgress.class.getSimpleName(), Parcels.wrap(progress));
                // show the activity
                context.startActivity(intent);
            } else if (contextualMode) {
                if (materialCardView.isChecked()) {
                    Log.i("MyBooksAdapter", "Removing from list");
                    selectedList.remove(progressesList.get(getBindingAdapterPosition()));
                    selectedCardViews.remove(materialCardView);
                } else {
                    selectedList.add(progressesList.get(getBindingAdapterPosition()));
                    selectedCardViews.add(materialCardView);
                }
                materialCardView.setChecked(!materialCardView.isChecked());
                mActionMode.setTitle("Books selected: " + selectedList.size());
            }
        }

        public void bind(UsersBookProgress progress) {
            BooksParse book = (BooksParse) progress.getParseObject("book");
            assert book != null;
            tvBookTitle.setText(book.getTitle());
            String httpLink = book.getThumbnail();
            if (!httpLink.equals("")) {
                String httpsLink = httpLink.substring(0,4) + "s" + httpLink.substring(4);
                //Log.i("Something", "Link: " + httpsLink);
                Glide.with(context).load(httpsLink).transform(new RoundedCornersTransformation(30, 10)).into(ivBookImage);
                //Log.i("Something", "Animation: on");
            } else {
                Glide.with(context).load(R.drawable.book_cover_placeholder).transform(new RoundedCornersTransformation(30, 10)).dontAnimate().into(ivBookImage);
            }
        }

        class ContextualCallback implements androidx.appcompat.view.ActionMode.Callback {

            @Override
            public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.contextual_action_bar, menu);
                contextualMode = true;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.delete:
                        removeBookProgresses(mode);
                        return true;
                    case R.id.share:
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                destroySelection();
            }

            private void destroySelection() {
                contextualMode = false;
                for (int i = 0; i < selectedCardViews.size(); i++) {
                    selectedCardViews.get(i).setChecked(false);
                }
                selectedList.clear();
                selectedCardViews.clear();
            }

            private void removeBookProgresses(androidx.appcompat.view.ActionMode mode) {
                for (int i = 0; i < selectedList.size(); i++) {
                    UsersBookProgress bookProgress = selectedList.get(i);
                    uploadBookInShelf(shelf, bookProgress, true);
                    progressesList.remove(bookProgress);
                }
                destroySelection();
                mode.finish();
                notifyDataSetChanged();
            }
        }
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