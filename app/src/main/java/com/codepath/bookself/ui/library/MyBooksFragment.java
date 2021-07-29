package com.codepath.bookself.ui.library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.bookself.MyBooksAdapter;
import com.codepath.bookself.R;
import com.codepath.bookself.ShelfBooksAdapter;
import com.codepath.bookself.models.UsersBookProgress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyBooksFragment extends Fragment {

    private static final String TAG = "MyBooksFragment";
    private RecyclerView recyclerView;
    private boolean justCreated;
    ShelfBooksAdapter shelfBooksAdapter;
    ArrayList<UsersBookProgress> allProgresses;

    public MyBooksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!justCreated) {
            Log.i(TAG, "My books updating");
            getUserBooks();
        }
        justCreated = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_books, container, false);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        justCreated = true;
        recyclerView = view.findViewById(R.id.rvMyBooks);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL));
        allProgresses = new ArrayList<>();
        // TODO: Create a new adapter for the library
        shelfBooksAdapter = new ShelfBooksAdapter(allProgresses, getContext());
        recyclerView.setAdapter(shelfBooksAdapter);
        getUserBooks();
    }


    private void getUserBooks() {

        // specify what type of data we want to query - UsersBookProgress.class
        ParseQuery<UsersBookProgress> query = ParseQuery.getQuery(UsersBookProgress.class);
        // include data referred by user key
        query.include(UsersBookProgress.KEY_USER);
        query.include(UsersBookProgress.KEY_BOOK);
        // limit query to latest 20 items
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<UsersBookProgress>() {
            @Override
            public void done(List<UsersBookProgress> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // save received posts to list and notify adapter of new data
                Log.i(TAG, "Comparing usersBookProgress" + posts);
                allProgresses.clear();
                allProgresses.addAll(posts);
                shelfBooksAdapter.updateAdapter(allProgresses);
            }
        });
    }
}