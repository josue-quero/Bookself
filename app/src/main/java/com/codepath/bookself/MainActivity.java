package com.codepath.bookself;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
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
    public String userId;
    GoogleSignInClient mGoogleSignInClient;
    private final String clientId = "562541520541-2j9aqk39pp8nts5efc2c9dfc3b218kl3.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Getting google client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/books"))
                .requestServerAuthCode(clientId, true)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar);

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
                        //item.setIcon(R.drawable.ic_instagram_home_filled_24);
                        fragment = fragmentLibrary;
                        break;
                    case R.id.navigation_discover:
                        //item.setIcon(R.drawable.ic_instagram_new_post_filled_24);
                        fragment = fragmentDiscover;
                        break;
                    case R.id.navigation_profile:
                        //item.setIcon(R.drawable.ic_instagram_user_filled_24);
                        fragment = fragmentProfile;
                        break;
                    default: return true;
                }
                fragmentManager.beginTransaction().replace(R.id.frPlaceholder, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.navigation_discover);

        //getBooksInfo("Vonnegut");
        getInfoFromSignedInUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.extOption:
            // Compose icon has been selected
            //Navigate to compose activity
                LaunchActivity temp = new LaunchActivity();
                logOut();
                goLaunchActivity();
                return true;
            case R.id.itSearch:
                super.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goLaunchActivity(){
        Intent i = new Intent(this, LaunchActivity.class);
        startActivity(i);
        finish();
    }

    private void getInfoFromSignedInUser() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            userId = acct.getId();
        }
    }

    public void logOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ParseUser.logOut();
                    }
                });
    }
}