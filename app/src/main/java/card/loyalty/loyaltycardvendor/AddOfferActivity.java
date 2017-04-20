package card.loyalty.loyaltycardvendor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import card.loyalty.loyaltycardvendor.data_models.LoyaltyOffer;

public class AddOfferActivity extends AppCompatActivity {

    private static final String TAG = "AddOfferActivity";

    // Firebase Database References
    private DatabaseReference mRootRef;
    private DatabaseReference mLoyaltyOffersRef;

    // Firebase User ID
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_offer);

        Intent intent = getIntent();
        mUid = intent.getStringExtra(VendorLandingActivity.EXTRA_FIREBASE_UID);

        TextView uidView = (TextView) findViewById(R.id.offer_uid);
        uidView.setText(mUid);

        // Firebase initialisations
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mLoyaltyOffersRef = mRootRef.child("LoyaltyOffers");


        final EditText description = (EditText) findViewById(R.id.offerDescription);
        final EditText purchasesPerReward = (EditText) findViewById(R.id.offerPurchasesPerReward);
        final EditText reward = (EditText) findViewById(R.id.offerReward);
        Button submitButton = (Button) findViewById(R.id.btnOfferSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = mLoyaltyOffersRef.push().getKey();
                LoyaltyOffer newOffer = new LoyaltyOffer(key, mUid, description.getText().toString(), purchasesPerReward.getText().toString(), reward.getText().toString());
                mLoyaltyOffersRef.child(key).setValue(newOffer, new DatabaseReference.CompletionListener() {

                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(AddOfferActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddOfferActivity.this, "Loyalty Offer Added Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }
}
