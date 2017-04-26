package card.loyalty.loyaltycardvendor.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import card.loyalty.loyaltycardvendor.R;
import card.loyalty.loyaltycardvendor.data_models.LoyaltyOffer;

/**
 * Created by Sam on 20/04/2017.
 */

public class LoyaltyOffersRecyclerAdapter extends RecyclerView.Adapter<LoyaltyOffersRecyclerAdapter.LoyaltyOfferViewHolder> {
    private static final String TAG = "LoyaltyOffersRecyclerAdapter";

    private List<LoyaltyOffer> mOffers;

    public LoyaltyOffersRecyclerAdapter(List<LoyaltyOffer> offers) {
        mOffers = offers;
    }

    public void setOffers(List<LoyaltyOffer> offers) {
        mOffers = offers;
        notifyDataSetChanged();
    }

    @Override
    public LoyaltyOfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loyalty_offer_list_item, parent, false);

        LoyaltyOfferViewHolder holder = new LoyaltyOfferViewHolder(view);

        return holder;
    }

    @Override
    public int getItemCount() {
        return ((mOffers != null) && (mOffers.size() != 0) ? mOffers.size() : 0);
    }

    public LoyaltyOffer getOffer(int position) {
        return ((mOffers!=null)&&(mOffers.size()!=0)? mOffers.get(position) : null);
    }

    @Override
    public void onBindViewHolder(LoyaltyOfferViewHolder holder, int position) {
        LoyaltyOffer offer = mOffers.get(position);
        holder.content.setText(offer.description);
    }


    static class LoyaltyOfferViewHolder extends RecyclerView.ViewHolder {
        TextView content = null;

        public LoyaltyOfferViewHolder(View itemView) {
            super(itemView);
            this.content = (TextView) itemView.findViewById(R.id.offer_description);
        }
    }
}
