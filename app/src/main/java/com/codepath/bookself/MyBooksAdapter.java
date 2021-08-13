package com.codepath.bookself;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MyBooksAdapter extends RecyclerView.Adapter<MyBooksAdapter.ViewHolder>{

    private ArrayList<UsersBookProgress> progressesList;
    private ArrayList<UsersBookProgress> selectedList;
    private ArrayList<MaterialCardView> selectedCardViews;
    private RequestQueue mRequestQueue;
    public static final String TAG = "MyBooksAdapter";
    private String tokenUrl = "https://oauth2.googleapis.com/token";

    private Context context;
    private boolean contextualMode = false;
    private GoogleSignInClient mGoogleSignInClient;
    private androidx.appcompat.view.ActionMode mActionMode;

    public MyBooksAdapter(ArrayList<UsersBookProgress> booksList, Context context) {
        this.progressesList = booksList;
        this.context = context;
        selectedList = new ArrayList<>();
        selectedCardViews = new ArrayList<>();
        // Getting google client so that we can sign out the user
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(context.getString(R.string.booksScope)))
                .requestServerAuthCode(context.getString(R.string.clientId), true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    @NonNull
    @Override
    public MyBooksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.shelf_book_item, parent, false));
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
                        mActionMode = ((AppCompatActivity) context).startSupportActionMode(new ContextualCallback());
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
                    if (bookProgress.getHearted()) {
                        updateGoogleFavorites(ParseUser.getCurrentUser().getString("accessToken"),bookProgress.getBook(), true);
                    }
                    bookProgress.deleteInBackground();
                    progressesList.remove(bookProgress);
                }
                getParseShelves();
                destroySelection();
                mode.finish();
                notifyDataSetChanged();
            }
        }
    }

    // Getting all the shelves that belong to the user
    private void getParseShelves() {
        // Specify what type of data we want to query - Shelf.class
        ParseQuery<Shelves> query = ParseQuery.getQuery(Shelves.class);
        // Include data referred by user key
        query.include("progresses.book");
        query.include("progresses.user");
        query.include(UsersBookProgress.KEY_BOOK);
        query.include(UsersBookProgress.KEY_USER);
        query.include(Shelves.KEY_PROGRESSES);
        query.include(Shelves.KEY_USER);
        // Limit query to latest 20 items
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(20);
        // Order posts by creation date (newest first)
        query.addAscendingOrder("createdAt");
        // Start an asynchronous call for posts
        query.findInBackground(new FindCallback<Shelves>() {
            @Override
            public void done(List<Shelves> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                for (Shelves shelf: posts) {
                    updateCountShelves(shelf);
                }
            }
        });
    }

    private void updateCountShelves(Shelves shelf) {
        // save received posts to list and notify adapter of new data
        ParseRelation<UsersBookProgress> relation = shelf.getRelation("progresses");
        ParseQuery<UsersBookProgress> query = relation.getQuery();

        query.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e != null) {
                    Log.e("ShelvesAdapter", "Error counting: ", e);
                    return;
                }
                shelf.setAmountBooks(count);
                shelf.saveInBackground();
            }
        });
    }

    private void updateGoogleFavorites(String accessToken, BooksParse currentBook, boolean delete) {

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(context);

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();
        String parameter;
        if (delete){
            parameter = "removeVolume";
        } else {
            parameter = "addVolume";
        }

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/mylibrary/bookshelves/7/" + parameter + "?volumeId=" + currentBook.getEbookId() + "&key=" + BuildConfig.BOOKS_KEY;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(context);


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Correctly updated Google favorite books" + "Deleted: "+ delete);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    refreshAccessToken(currentBook, delete);
                } else {
                    // irrecoverable errors. show error to user.
                    Toast.makeText(context, "Error found is " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error found is: " + error);
                }
                /*
                if (error.networkResponse.statusCode == 403) {
                    lookForEbookVersion(currentBook);
                }*/
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
    }

    private void refreshAccessToken(BooksParse book, boolean delete) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject params = new JSONObject();
        try {
            params.put("client_id", "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com");
            params.put("client_secret", "FlTA8PyCAx43q4XjK3X-wZbC");
            params.put("refresh_token", ParseUser.getCurrentUser().getString("refreshToken"));
            params.put("grant_type", "refresh_token");
        } catch (JSONException ignored) {
            // never thrown in this case
        }

        JsonObjectRequest refreshTokenRequest = new JsonObjectRequest(Request.Method.POST, tokenUrl, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i(TAG, "Success refresh token");
                    String accessToken = response.getString("access_token");
                    saveAccessToken(accessToken);
                    updateGoogleFavorites(accessToken, book, delete);
                } catch (JSONException e) {
                    Toast.makeText(context, "Error using refreshed token " + e, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error using refreshed token " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // show error to user. refresh failed.
                Log.e("Error on token refresh", new String(error.networkResponse.data));
                LaunchActivity temp = new LaunchActivity();
                revokeAccess();
                clearTokens();
                goLaunchActivity();
            }
        });
        queue.add(refreshTokenRequest);
    }

    private void clearTokens() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Other attributes than "email" will remain unchanged!
            currentUser.put("accessToken", "");
            currentUser.put("refreshToken", "");
            // Saves the object.
            currentUser.saveInBackground();
        }
    }

    public void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logOut();
                    }
                });
    }

    public void logOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ParseUser.logOut();
                    }
                });
    }

    public void goLaunchActivity(){
        Intent i = new Intent(context, LaunchActivity.class);
        ((MainActivity) context).startActivity(i);
        ((MainActivity) context).finish();
    }

    public void saveAccessToken(String refreshToken) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // Other attributes than "email" will remain unchanged!
            currentUser.put("accessToken", refreshToken);
            // Saves the object.
            currentUser.saveInBackground();
        }
    }
}