package com.codepath.bookself.ui.profile;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.bookself.MyBooksAdapter;
import com.codepath.bookself.ProfileAdapter;
import com.codepath.bookself.R;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.Shelves;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private RecyclerView recyclerView;
    ProfileAdapter profileAdapter;
    ArrayList<UsersBookProgress> allProgresses;
    ImageView ivProfilePicture;
    TextView tvPagesAmount, tvBooksAmount, tvGoalPercentage, tvUsersName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvBooksProgress);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        allProgresses = new ArrayList<>();
        profileAdapter = new ProfileAdapter(allProgresses, getContext());
        recyclerView.setAdapter(profileAdapter);

        // Hiding the toolbar
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

        // Getting Google user's information
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireActivity());
        String personName = account.getDisplayName();
        Uri personPhoto = account.getPhotoUrl();

        // Getting Parse user's information
        ParseUser currentUser = ParseUser.getCurrentUser();
        double pagesAmount = currentUser.getDouble("pagesReadAmount");
        double booksAmount = currentUser.getDouble("booksReadAmount");
        double monthlyGoal = currentUser.getDouble("monthlyGoal");
        double percentage;
        boolean booksGoal = currentUser.getBoolean("booksGoal");
        Log.i(TAG, "Monthly goal: " + monthlyGoal);
        if (booksGoal) {
            Log.i(TAG, "Books amount " + booksAmount);
            percentage = (booksAmount/monthlyGoal) * 100;
        } else {
            percentage = (pagesAmount/monthlyGoal) * 100;
        }
        Log.i(TAG, "Percentage " + percentage);
        long roundedPercentage = Math.round(percentage);

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUsersName = view.findViewById(R.id.tvProfileName);
        tvPagesAmount = view.findViewById(R.id.tvPagesAmount);
        tvBooksAmount = view.findViewById(R.id.tvBooksAmount);
        tvGoalPercentage = view.findViewById(R.id.tvGoalPercentage);

        // Setting in the view all the  information about the user
        Glide.with(getContext()).load(personPhoto).circleCrop().into(ivProfilePicture);
        tvUsersName.setText(personName);
        tvPagesAmount.setText(String.valueOf((int) pagesAmount));
        tvBooksAmount.setText(String.valueOf((int) booksAmount));
        tvGoalPercentage.setText(String.valueOf(roundedPercentage) + "%");
        getUserBooksCurrentlyReading();
    }

    // Getting books that are in progress
    private void getUserBooksCurrentlyReading() {
        // specify what type of data we want to query - UsersBookProgress.class
        ParseQuery<Shelves> query = ParseQuery.getQuery(Shelves.class);
        // include data referred by user key

        query.include(UsersBookProgress.KEY_USER);
        query.include(UsersBookProgress.KEY_BOOK);
        // limit query to latest 20 items
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("name", "Reading");
        query.whereNotEqualTo("currentPage", 0);
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Shelves>() {
            @Override
            public void done(List<Shelves> shelf, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // Getting the books from the reading shelf
                ParseRelation<UsersBookProgress> relation = shelf.get(0).getRelation("progresses");
                ParseQuery<UsersBookProgress> query = relation.getQuery();
                query.include(UsersBookProgress.KEY_BOOK);
                query.include(UsersBookProgress.KEY_USER);
                query.addDescendingOrder("lastRead");

                query.findInBackground(new FindCallback<UsersBookProgress>() {
                    @Override
                    public void done(List<UsersBookProgress> objects, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue with getting progresses", e);
                            return;
                        }
                        allProgresses.addAll(objects);
                        profileAdapter.updateAdapter(allProgresses);
                    }
                });
            }
        });
    }
}