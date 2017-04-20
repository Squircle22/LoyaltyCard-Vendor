package card.loyalty.loyaltycardvendor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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

import java.util.ArrayList;
import java.util.Arrays;

import card.loyalty.loyaltycardvendor.adapters.LoyaltyOffersRecyclerAdapter;
import card.loyalty.loyaltycardvendor.data_models.LoyaltyOffer;

public class VendorLandingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "VendorLandingActivity";
    private static final int RC_SIGN_IN = 123;
    // Firebase UID extra for launching add offer activity
    public static final String EXTRA_FIREBASE_UID = "FIREBASE_UID";

    // Adapter for Recycler View
    private LoyaltyOffersRecyclerAdapter mRecyclerAdapter;

    // Firebase Authentication Variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Firebase Authentication Initialisation
        mFirebaseAuth = FirebaseAuth.getInstance();

        // some crazy code to be removed soon
        ArrayList<LoyaltyOffer> offers = new ArrayList<>();
        offers.add(new LoyaltyOffer("1", "ven02", "Get 1 with every 1", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("2", "ven02", "Get 1 with every 2", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("3", "ven02", "Get 1 with every 5", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("4", "ven02", "Get 2 with every 11", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("5", "ven02", "Buy 4 get 1 free", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("6", "ven02", "Get 1 with every 1", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("7", "ven02", "Buy 6 get 1 free", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("8", "ven02", "Buy 5 get 1 free", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("9", "ven02", "Get 1 with every 1", "1", "1 free thing"));
        offers.add(new LoyaltyOffer("10", "ven02", "Buy 2 get 1 free", "1", "1 free thing"));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.landing_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerAdapter = new LoyaltyOffersRecyclerAdapter(offers);
        recyclerView.setAdapter(mRecyclerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Firebase UI Authentication
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: is signed_in:" + user.getUid());
                    onSignedInInitialise(user);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: is signed_out");
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    //.setTheme(R.style.AuthTheme) //set a theme for Firebase UI here
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addOffer) {
            launchAddOfferActivity();

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_signOut) {
            // Firebase UI Sign Out
            AuthUI.getInstance().signOut(this);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Once signed in initialise QR code and set any other user specific data here. For example attach database event listener here
    private void onSignedInInitialise(FirebaseUser user){

    }

    // On signing out clean up any user specific data here. For example detatch database event listener
    private void onSignedOutCleanup() {

    }

    // Launch the add offer activity
    private void launchAddOfferActivity() {
        String Uid = mFirebaseAuth.getCurrentUser().getUid();

        Intent intent = new Intent(this, AddOfferActivity.class);
        intent.putExtra(EXTRA_FIREBASE_UID, Uid);
        startActivity(intent);
    }
}
