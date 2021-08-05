package com.codepath.bookself.ui.library;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.codepath.bookself.BitmapScaler;
import com.codepath.bookself.BuildConfig;

import com.codepath.bookself.MyBooksAdapter;
import com.codepath.bookself.R;
import com.codepath.bookself.SearchActivity;
import com.codepath.bookself.ShelfBooksAdapter;
import com.codepath.bookself.models.BooksParse;
import com.codepath.bookself.models.UsersBookProgress;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class MyBooksFragment extends Fragment {

    private static final String TAG = "MyBooksFragment";
    private RecyclerView recyclerView;
    private boolean justCreated;
    private MyBooksAdapter shelfBooksAdapter;
    private SpeedDialView spd;
    private File photoFile;
    private ImageView ivPostImage;
    private String photoFileName = "barCodePhoto.jpg";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public final static int PICK_PHOTO_CODE = 1046;
    private ArrayList<BooksParse> resultBooks;
    private ArrayList<UsersBookProgress> allProgresses;
    private RequestQueue mRequestQueue;

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
        shelfBooksAdapter = new MyBooksAdapter(allProgresses, getContext());
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
        spd.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_action3, R.drawable.ic_media)
                .setLabel("Media")
                .setTheme(R.style.AppTheme_Purple)
                .create());
        spd.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                Toast.makeText(getContext(), "item selected: " + actionItem.getId(), Toast.LENGTH_SHORT).show();
                switch (actionItem.getId()) {
                    case R.id.fab_action1:
                        launchCamera();
                        return true;
                    case R.id.fab_action3:
                        launcherMediaSelector();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    // Trigger gallery selection to pick a photo
    private void launcherMediaSelector() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
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
                Uri takenImage = Uri.fromFile(photoFile);
                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_EAN_8,
                                Barcode.FORMAT_EAN_13)
                        .build();
                InputImage image;
                try {
                    image = InputImage.fromFilePath(requireContext(), takenImage);
                    BarcodeScanner scanner = BarcodeScanning.getClient(options);
                    Task<List<Barcode>> result = scanner.process(image)
                            .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                                @Override
                                public void onSuccess(List<Barcode> barcodes) {
                                    Log.i(TAG, "Amount barcodes: " + barcodes.size());
                                    for (int i = 0; i < barcodes.size(); i++) {
                                        int valueType = barcodes.get(i).getValueType();
                                        // See API reference for complete list of supported types
                                        if (valueType == Barcode.TYPE_ISBN) {
                                            String displayValue = barcodes.get(i).getDisplayValue();
                                            getBookWithISBN(displayValue, i, barcodes.size());
                                            Log.i(TAG, "Data: " + displayValue);
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error at detecting ISBN: ", e);
                                    Log.i(TAG, "Error at detection ISBN " + e);
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "Exception at detection ISBN " + e);
                }
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if ((data != null) && requestCode == PICK_PHOTO_CODE && resultCode == RESULT_OK) {
            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_EAN_8,
                            Barcode.FORMAT_EAN_13)
                    .build();
            Uri photoUri = data.getData();
            InputImage image;
            try {
                image = InputImage.fromFilePath(requireContext(), photoUri);
                BarcodeScanner scanner = BarcodeScanning.getClient(options);
                Task<List<Barcode>> result = scanner.process(image)
                        .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                            @Override
                            public void onSuccess(List<Barcode> barcodes) {
                                Log.i(TAG, "Amount barcodes: " + barcodes.size());
                                for (int i = 0; i < barcodes.size(); i++) {
                                    int valueType = barcodes.get(i).getValueType();
                                    // See API reference for complete list of supported types
                                    if (valueType == Barcode.TYPE_ISBN) {
                                            String displayValue = barcodes.get(i).getDisplayValue();
                                            getBookWithISBN(displayValue, i, barcodes.size());
                                            Log.i(TAG, "Data: " + displayValue);
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error at detecting ISBN: ", e);
                                Log.i(TAG, "Error at detection ISBN " + e);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "Exception at detection ISBN " + e);
            }
        }
    }

    private void getBookWithISBN(String ISBN, int barcodePlace, int barcodesAmount) {
        resultBooks = new ArrayList<>();

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(requireContext());

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.getCache().clear();

        // below is the url for getting data from API in json format.
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + ISBN + "&maxResults=40&key=" + BuildConfig.BOOKS_KEY;

        Log.i(TAG, "This is the line: " + url);

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(requireContext());


        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //progressBar.setVisibility(View.GONE);
                // inside on response method we are extracting all our json data.
                try {
                    JSONArray itemsArray = response.getJSONArray("items");
                    Log.i(TAG, "Response: " + response);
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONArray authorsArray = new JSONArray();
                        JSONArray categoriesArray = new JSONArray();
                        String thumbnail = "";
                        String buyLink = "";
                        JSONObject itemsObj = itemsArray.getJSONObject(i);
                        String googleId = itemsObj.optString("id");
                        JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                        String title = volumeObj.optString("title");
                        String subtitle = volumeObj.optString("subtitle");
                        try {
                            authorsArray = volumeObj.getJSONArray("authors");
                        } catch (JSONException e) {
                            Log.i(TAG, "No author", e);
                        }
                        try {
                            categoriesArray = volumeObj.getJSONArray("categories");
                        } catch (JSONException e) {
                            Log.i(TAG, "No categories");
                        }
                        String publisher = volumeObj.optString("publisher");
                        String publishedDate = volumeObj.optString("publishedDate");
                        String description = volumeObj.optString("description");
                        int pageCount = volumeObj.optInt("pageCount");
                        JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");
                        JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                        if (imageLinks != null) {
                            thumbnail = imageLinks.optString("thumbnail");
                        }
                        if (saleInfoObj != null) {
                            buyLink = saleInfoObj.optString("buyLink");
                        }
                        String previewLink = volumeObj.optString("previewLink");
                        String infoLink = volumeObj.optString("infoLink");
                        ArrayList<String> authorsArrayList = new ArrayList<>();
                        if (authorsArray.length() != 0) {
                            for (int j = 0; j < authorsArray.length(); j++) {
                                authorsArrayList.add(authorsArray.optString(j));
                            }
                        }
                        ArrayList<String> categoriesArrayList = new ArrayList<>();
                        if (categoriesArray.length() != 0) {
                            for (int x = 0; x < categoriesArray.length(); x++) {
                                categoriesArrayList.add(categoriesArray.optString(x));
                            }
                        }
                        // after extracting all the data we are
                        // saving this data in our modal class.
                        BooksParse bookInfo = new BooksParse();
                        bookInfo.setBook(title, subtitle, authorsArrayList, publisher, publishedDate, description, pageCount, thumbnail, previewLink, infoLink, buyLink, googleId, categoriesArrayList);

                        // below line is use to pass our modal
                        // class in our array list.
                        resultBooks.add(bookInfo);
                    }

                    if ((barcodePlace + 1) == barcodesAmount) {
                        Bundle b = new Bundle();
                        b.putParcelableArrayList("Books", resultBooks);
                        Intent i = new Intent(getContext(), ResultsISBN.class);
                        i.putExtras(b);
                        startActivity(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // displaying a toast message when we get any error from API
                    Toast.makeText(getContext(), "No Data Found" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // also displaying error message in toast.
                Toast.makeText(getContext(), "Error found is " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error found is: " + error);
            }
        });
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksObjrequest);
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
        query.whereNotEqualTo("wishlist", true);
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