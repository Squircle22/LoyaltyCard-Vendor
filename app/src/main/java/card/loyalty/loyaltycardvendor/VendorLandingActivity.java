package card.loyalty.loyaltycardvendor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
    private DatabaseReference mLoyaltyCardsRef;
    private ValueEventListener mValueEventListener;
    private Query mQuery;

    // Field to store the index of the offer being scanned
    private int mOfferIndex;

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
        mLoyaltyCardsRef = mRootRef.child("LoyaltyCards");

        // Initialise offers list
        mOffers = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.landing_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add the RecyclerClickListener
        recyclerView.addOnItemTouchListener(new RecyclerClickListener(this, recyclerView, this));

        mRecyclerAdapter = new LoyaltyOffersRecyclerAdapter(mOffers);
        recyclerView.setAdapter(mRecyclerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Activity activity = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
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

    // TODO: Refactor methods relating to updating customer card using ReactiveX (RxJava/RxAndroid)
    private void processScanResult(final String offerID, final String customerID) {
        String offerIDcustomerID = offerID + "_" + customerID;
        Log.d(TAG, "processScanResult: start");
        Query query = mLoyaltyCardsRef.orderByChild("offerID_customerID").equalTo(offerIDcustomerID);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot cardSnapshot: dataSnapshot.getChildren()) {
                        LoyaltyCard card = cardSnapshot.getValue(LoyaltyCard.class);
                        Log.d(TAG, "onDataChange: card purchase count: " + card.purchaseCount);
                        card.setCardID(cardSnapshot.getKey());
                        updateCard(card);
                    }
                } else {
                    createCard(offerID, customerID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO on cancelled method
            }
        };

        query.addListenerForSingleValueEvent(listener);
        Log.d(TAG, "processScanResult: end");
    }

    // TODO: Refactor as Rx
    protected void updateCard(LoyaltyCard card) {
        Log.d(TAG, "updateCard: start");
        card.addToPurchaseCount(1);
        String key = card.retrieveCardID();
        if (key == null) key = mLoyaltyCardsRef.push().getKey();
        mLoyaltyCardsRef.child(key).setValue(card, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast.makeText(VendorLandingActivity.this, "FAILED TO UPDATE. TRY AGAIN!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VendorLandingActivity.this, "Purchase Count Successfully Updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: success");
                }
            }
        });
        Log.d(TAG, "updateCard: end");
    }

    // TODO: Refactor as Rx
    private void createCard(String offerID, String customerID) {
        Log.d(TAG, "createCard: start");
        LoyaltyCard card = new LoyaltyCard(offerID, customerID);
        updateCard(card);
        Log.d(TAG, "createCard: end");
    }

    // Database listener retrieves offers from Firebase and sets data to recycler view adapter
    private void attachDatabaseReadListener() {
        mQuery = mLoyaltyOffersRef.orderByChild("vendorID").equalTo(mFirebaseAuth.getCurrentUser().getUid());

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: data change detected");
                        mOffers.clear();
                        for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                            LoyaltyOffer offer = offerSnapshot.getValue(LoyaltyOffer.class);
                            offer.setOfferID(offerSnapshot.getKey());
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

        mQuery.addValueEventListener(mValueEventListener);
    }
    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mQuery.removeEventListener(mValueEventListener);
            mValueEventListener = null;
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
        } else {
            // Handle the QR scanning result
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "onActivityResult: result.getContents(): "+ result.getContents());
                    String offerID = mOffers.get(mOfferIndex).getOfferID();
                    processScanResult(offerID, result.getContents());
                }
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
        mOfferIndex = position;
        launchScanner();
    }

    // When recycler item is longPressed
    @Override
    public void onLongClick(View view, int position) {
        Log.d(TAG, "onLongClick: starts");
        // makes a toast message for now...more functionality to come
        Toast.makeText(VendorLandingActivity.this, "Long tap at position " + position, Toast.LENGTH_LONG).show();
    }

    // Launch the QR Scanner
    private void launchScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }
}
