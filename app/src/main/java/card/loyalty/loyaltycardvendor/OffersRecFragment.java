package card.loyalty.loyaltycardvendor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import card.loyalty.loyaltycardvendor.adapters.LoyaltyOffersRecyclerAdapter;
import card.loyalty.loyaltycardvendor.data_models.LoyaltyOffer;

/**
 * Created by Caleb T on 3/05/2017.
 */

public class OffersRecFragment extends Fragment {

    private static final String TAG = "OffersRecFragment";

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;

    // Firebase Database References
    private DatabaseReference mRootRef;
    private DatabaseReference mLoyaltyOffersRef;
    private ValueEventListener mValueEventListener;
    private  Query mQuery;

    // Firebase User ID
    private String mUid;

    // RecyclerView Objects
    protected RecyclerView recyclerView;
    protected LoyaltyOffersRecyclerAdapter recyclerAdapter;
    protected RecyclerView.LayoutManager layoutManager;

    // List of Offers created
    protected List<LoyaltyOffer> mOffers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets UID
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUid = mFirebaseAuth.getCurrentUser().getUid();

        mOffers = new ArrayList<>();

        // Sets Database Reference
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mLoyaltyOffersRef = mRootRef.child("LoyaltyOffers");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rec_offers, container, false);
        view.setTag(TAG);

        // Links Recycler View
        recyclerView = (RecyclerView) view.findViewById(R.id.offers_recycler);
        // Sets Recycler View Layout
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Creates recyclerAdapter for content
        recyclerAdapter = new LoyaltyOffersRecyclerAdapter(mOffers);

        recyclerView.setAdapter(recyclerAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Attaches the Database
        attachDatabaseReadListener();

    }

    @Override
    public void onPause() {
        super.onPause();
        // Detaches the Database
        detachDatabaseReadListener();
    }

    // Database listener retrieves offers from Firebase and sets data to recycler view recyclerAdapter
    private void attachDatabaseReadListener() {
        mQuery = mLoyaltyOffersRef.orderByChild("vendorID").equalTo(mFirebaseAuth.getCurrentUser().getUid());
        Log.d(TAG, "Current mUid: " + mUid);

        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange started");
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: data change detected");
                        mOffers.clear();
                        for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                            LoyaltyOffer offer = offerSnapshot.getValue(LoyaltyOffer.class);
                            offer.setOfferID(offerSnapshot.getKey());
                            mOffers.add(offer);
                            Log.d(TAG, "Current offer description: " + offer.purchasesPerReward);
                        }
                        recyclerAdapter.setOffers(mOffers);
                    } else {
                        Log.d(TAG, "dataSnapshot doesn't exist");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
        } else {
            Log.d(TAG, "mValueEventListener is not null");
        }
        Log.d(TAG, "addValueEventListener added to mQuery");
        mQuery.addValueEventListener(mValueEventListener);
    }

    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mQuery.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

}
