package com.codepath.bookself.ui.library;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.bookself.BitmapScaler;
import com.codepath.bookself.MyBooksAdapter;
import com.codepath.bookself.R;
import com.codepath.bookself.ShelfBooksAdapter;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MyBooksFragment extends Fragment {

    private static final String TAG = "MyBooksFragment";
    private RecyclerView recyclerView;
    private boolean justCreated;
    ShelfBooksAdapter shelfBooksAdapter;
    SpeedDialView spd;
    private File photoFile;
    private ImageView ivPostImage;
    private String photoFileName = "barCodePhoto.jpg";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    ArrayList<UsersBookProgress> allProgresses;

    public MyBooksFragment() {
        // Required empty public constructor
    }

    // Refreshing MyBooks fragment when resuming the fragment
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

    // The view is composed in a grid for the books
    // with the adapter and layout manager
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        justCreated = true;
        recyclerView = view.findViewById(R.id.rvMyBooks);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        allProgresses = new ArrayList<>();
        shelfBooksAdapter = new ShelfBooksAdapter(allProgresses, getContext());
        recyclerView.setAdapter(shelfBooksAdapter);
        // Getting all the books that belong to the user
        getUserBooks();

        spd = view.findViewById(R.id.speedDial);
        spd.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_action1, R.drawable.ic_barcode)
                .setLabel("Barcode")
                .setTheme(R.style.AppTheme_Purple)
                .create());
        spd.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_action2, R.drawable.ic_manually)
                .setLabel("Manually")
                .setTheme(R.style.AppTheme_Purple)
                .create());
        spd.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                Toast.makeText(getContext(), "item selected: " + actionItem.getId(), Toast.LENGTH_SHORT).show();
                switch (actionItem.getId()) {
                    case R.id.fab_action1:
                        launchCamera();
                    default:
                        return false;
                }
            }
        });
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.bookself", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                //InputImage image = InputImage.fromBitmap(takenImage, rotationDegree);
                // See BitmapScaler.java: https://gist.github.com/nesquena/3885707fd3773c09f1bb
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, 60);
                ivPostImage.setVisibility(View.VISIBLE);
                ivPostImage.setImageBitmap(resizedBitmap);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    // Getting the books from the Parse Database
    private void getUserBooks() {
        // Specify what type of data we want to query - UsersBookProgress.class
        ParseQuery<UsersBookProgress> query = ParseQuery.getQuery(UsersBookProgress.class);
        // Include data referred by user key
        query.include(UsersBookProgress.KEY_USER);
        query.include(UsersBookProgress.KEY_BOOK);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // Limit query to latest 20 items
        query.setLimit(20);
        // Order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // Start an asynchronous call for posts
        query.findInBackground(new FindCallback<UsersBookProgress>() {
            @Override
            public void done(List<UsersBookProgress> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // Save received posts to list and notify adapter of new data
                Log.i(TAG, "Comparing usersBookProgress" + posts);
                allProgresses.clear();
                allProgresses.addAll(posts);
                shelfBooksAdapter.updateAdapter(allProgresses);
            }
        });
    }
}