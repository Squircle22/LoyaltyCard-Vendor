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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import card.loyalty.loyaltycardvendor.adapters.LoyaltyOffersRecyclerAdapter;
import card.loyalty.loyaltycardvendor.data_models.LoyaltyOffer;

public class VendorLandingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RecyclerClickListener.OnRecyclerClickListener {

    private static final String TAG = "VendorLandingActivity";
    private static final int RC_SIGN_IN = 123;
    // Firebase UID extra for launching add offer activity
    public static final String EXTRA_FIREBASE_UID = "FIREBASE_UID";

    // Adapter for Recycler View
    private LoyaltyOffersRecyclerAdapter mRecyclerAdapter;

    // Offers List
    private List<LoyaltyOffer> mOffers;

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Firebase Database
    private DatabaseReference mRootRef;
    private DatabaseReference mLoyaltyOffersRef;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Firebase Authentication Initialisation
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Firebase Database Initialisation
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mLoyaltyOffersRef = mRootRef.child("LoyaltyOffers");

        // Initialise offers list
        mOffers = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.landing_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add the RecyclerClickListener
        recyclerView.addOnItemTouchListener(new RecyclerClickListener(this, recyclerView, this));

        mRecyclerAdapter = new LoyaltyOffersRecyclerAdapter(mOffers);
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

    // Database listener retrieves offers from Firebase and sets data to recycler view adapter
    private void attachDatabaseReadListener() {
        Query query = mLoyaltyOffersRef.orderByChild("vendorID").equalTo(mFirebaseAuth.getCurrentUser().getUid());

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mOffers.clear();
                        for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                            LoyaltyOffer offer = offerSnapshot.getValue(LoyaltyOffer.class);
                            mOffers.add(offer);
                        }
                        mRecyclerAdapter.setOffers(mOffers);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
        }

        query.addValueEventListener(mValueEventListener);
    }
    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mLoyaltyOffersRef.removeEventListener(mChildEventListener);
            mChildEventListener = null;
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

    // Once signed in set any user specific data here. For example attach database event listener here
    private void onSignedInInitialise(FirebaseUser user){
        attachDatabaseReadListener();
    }

    // On signing out clean up any user specific data here. For example detatch database event listener
    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
    }

    // Launch the add offer activity
    private void launchAddOfferActivity() {
        String Uid = mFirebaseAuth.getCurrentUser().getUid();

        Intent intent = new Intent(this, AddOfferActivity.class);
        intent.putExtra(EXTRA_FIREBASE_UID, Uid);
        startActivity(intent);
    }

    // When recycler item is clicked/tapped
    @Override
    public void onClick(View view, int position) {
        Log.d(TAG, "onClick: starts");
        // makes a toast message for now...more functionality to come
        Toast.makeText(VendorLandingActivity.this, "Normal tap at position " + position, Toast.LENGTH_SHORT).show();
    }

    // When recycler item is longPressed
    @Override
    public void onLongClick(View view, int position) {
        Log.d(TAG, "onLongClick: starts");
        // makes a toast message for now...more functionality to come
        Toast.makeText(VendorLandingActivity.this, "Long tap at position " + position, Toast.LENGTH_LONG).show();
    }
}
