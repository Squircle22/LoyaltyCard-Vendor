package card.loyalty.loyaltycardvendor;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import card.loyalty.loyaltycardvendor.data_models.LoyaltyOffer;

/**
 * Created by Caleb T on 3/05/2017.
 */

public class AddOfferFragment extends Fragment {

    private static final String TAG = "AddOfferFragment";

    // Firebase Database References
    private DatabaseReference mRootRef;
    private DatabaseReference mLoyaltyOffersRef;

    // Firebase User ID
    private String mUid;

    // View object
    private View view;

    // Fields
    private EditText description;
    private EditText purchasesPerReward;
    private EditText reward;
    private Button submitButton;
    private Button cancelButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        // Initialises the View object
        view = inflater.inflate(R.layout.fragment_add_offer, container, false);

        // gets UID from Vendor Activity
        Bundle uidArgs = getArguments();
        mUid = uidArgs.getString(VendorActivity.EXTRA_FIREBASE_UID);

        // Gets Text for TextView and sets it
            // Finds the TextView
        TextView uidView = (TextView) view.findViewById(R.id.offer_uid);
            // Places text into TextView
        uidView.setText(mUid);

        // Firebase Initialisation
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mLoyaltyOffersRef = mRootRef.child("LoyaltyOffers");

        // Field Initialisation
        description = (EditText) view.findViewById(R.id.offer_description);
        purchasesPerReward = (EditText) view.findViewById(R.id.offer_purchases_per_reward);
        reward = (EditText) view.findViewById(R.id.offer_reward);
        submitButton = (Button) view.findViewById(R.id.btn_submit_offer);
        cancelButton = (Button) view.findViewById(R.id.btn_cancel_offer);

        // Submit's info and closes fragment
        submitButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(v == submitButton) {
                    String key = mLoyaltyOffersRef.push().getKey();

                    // Creates new Loyalty Offer Object
                    LoyaltyOffer newOffer = new LoyaltyOffer(
                            mUid,
                            description.getText().toString(),
                            purchasesPerReward.getText().toString(),
                            reward.getText().toString()
                    );

                    mLoyaltyOffersRef.child(key).setValue(newOffer, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(getContext(), "Adding Loyalty Offer Failed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Loyalty Offer Successfully Added!", Toast.LENGTH_SHORT).show();
                                // Closes Fragment
                                if(getFragmentManager().getBackStackEntryCount() > 0 ) {
                                    hideKeyboard();
                                    getFragmentManager().popBackStack();
                                }
                            }
                        }
                    });
                }
            }
        });

        // Closes fragment when pressed
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v == cancelButton) {
                    Toast.makeText(getContext(), "Offer Creation Canceled", Toast.LENGTH_SHORT).show();
                    // Closes Fragment
                    if(getFragmentManager().getBackStackEntryCount() > 0 ) {
                        hideKeyboard();
                        getFragmentManager().popBackStack();
                    }
                }
            }
        });
        return view;
    }

    // Hides Keyboard after fragment closes
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
