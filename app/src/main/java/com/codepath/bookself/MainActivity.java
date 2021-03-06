package com.codepath.bookself;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.bookself.databinding.ActivityMainBinding;
import com.codepath.bookself.ui.discover.DiscoverFragment;
import com.codepath.bookself.ui.library.LibraryFragment;
import com.codepath.bookself.ui.library.MyBooksFragment;
import com.codepath.bookself.ui.profile.ProfileFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    public static final String TAG = "MainActivity";
    GoogleSignInClient mGoogleSignInClient;

    // Passing this result to a fragment
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Getting google client so that we can sign out the user
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(getString(R.string.booksScope)))
                .requestServerAuthCode(getString(R.string.clientId), true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting up the bottomNavigationView
        BottomNavigationView bottomNavigationView = (BottomNavigationView) binding.btNavigationView;
        // Definition of fragments
        final Fragment fragmentDiscover = new DiscoverFragment();
        final Fragment fragmentLibrary = new LibraryFragment();
        final Fragment fragmentProfile = new ProfileFragment();

        // Handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_library:
                        fragment = fragmentLibrary;
                        break;
                    case R.id.navigation_discover:
                        fragment = fragmentDiscover;
                        break;
                    case R.id.navigation_profile:
                        fragment = fragmentProfile;
                        break;
                    default: return true;
                }
                fragmentManager.beginTransaction().replace(R.id.frPlaceholder, fragment).commit();
                return true;
            }
        });
        // By default we go to the discover page
        bottomNavigationView.setSelectedItemId(R.id.navigation_discover);
    }

    // Creating the Menu options in the toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    // Managing what happens when the user clicks on a determined menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Logging out the user
            case R.id.extOption:
            // Compose icon has been selected
            //Navigate to compose activity
                LaunchActivity temp = new LaunchActivity();
                logOut();
                goLaunchActivity();
                return true;
            // Start the Search flow (first expanding the search bar)
            case R.id.itSearch:
                super.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Passing this result to a fragment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Going to the launch activity once the user logs out
    public void goLaunchActivity(){
        Intent i = new Intent(this, LaunchActivity.class);
        startActivity(i);
        finish();
    }

    // Loging out the current user (Google and Parse)
    public void logOut() {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
        suggestions.clearHistory();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ParseUser.logOut();
                    }
                });
    }
}