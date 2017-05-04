package card.loyalty.loyaltycardvendor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import card.loyalty.loyaltycardvendor.adapters.LoyaltyOffersRecyclerAdapter;
import card.loyalty.loyaltycardvendor.data_models.LoyaltyCard;
import card.loyalty.loyaltycardvendor.data_models.LoyaltyOffer;

public class VendorActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "VendorActivity";
    private static final int RC_SIGN_IN = 123;

    // Drawer Objects
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    // Firebase UID extra for launching add offer activity
    public static final String EXTRA_FIREBASE_UID = "FIREBASE_UID";

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String UID;
    private Bundle uidArgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor);

        // Firebase Authentication Initialisation
        mFirebaseAuth = FirebaseAuth.getInstance();
        UID = mFirebaseAuth.getCurrentUser().getUid();

        uidArgs = new Bundle();
        uidArgs.putString(EXTRA_FIREBASE_UID, UID);

        // FAB that launches the scanner
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        /** Drawer Initialisation **/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // toolbar id from app_bar_vendor
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // drawer_layout from activity_vendor
        drawerToggle = new ActionBarDrawerToggle(
                this,           // host Activity
                drawerLayout,   // DrawerLayout Object
                toolbar,        // Sets Icon that opens the drawer
                R.string.navigation_drawer_open,    // Open Drawer description
                R.string.navigation_drawer_close);  // Close Drawer description
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        // Sets Navigaiotn Item Listeners
            // Main Items
        NavigationView nView = (NavigationView) findViewById(R.id.nav_view);
        nView.setNavigationItemSelectedListener(this);
            // Footer Items
        NavigationView nViewFooter = (NavigationView) findViewById(R.id.nav_view_footer);
        nViewFooter.setNavigationItemSelectedListener(this);

        // Firebase UI Authentication
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: is signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: is signed_out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .setTheme(R.style.AppTheme_NoActionBar)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        if(savedInstanceState == null) {
            Fragment frag = new OffersRecFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.content_vendor, frag)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Handling the Firebase UI Auth result
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // On resuming activity
    @Override
    protected void onResume() {

        super.onResume();

        // Add the firebase auth state listener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    // On pausing activity
    @Override
    protected void onPause() {

        super.onPause();

        // Remove the firebase auth state listener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vendor_landing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Drawer Functionality **/
    // Add desired functionality in each switch case
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Fragment current = new OffersRecFragment();

        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_offers:
                // launches Offes Fragment
                current = new OffersRecFragment();
                break;
            case R.id.nav_add_offer:
                // Launches Add Offer Fragment
                current = new AddOfferFragment();
                current.setArguments(uidArgs);
                break;
            case R.id.nav_push_promos:
                Toast.makeText(getApplicationContext(), "Push Promos Pressed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_sign_out:
                // FirebaseUI Sign Out
                AuthUI.getInstance().signOut(this);
                break;
        }

        // Launches fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_vendor, current)
                .addToBackStack(null)
                .commit();
        // Closes drawer when item is pressed
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
