package card.loyalty.loyaltycardvendor.data_models;

/**
 * Created by Sam on 25/04/2017.
 */

public class LoyaltyCard {

    // Public fields for Firebase interaction
    public String offerID;
    public String customerID;
    public String purchaseCount;
    public String rewardsIssued;
    public String rewardsClaimed;
    public String vendorID;

    // Hybrid key for search
    public String offerID_customerID;

    private String cardID;

    public LoyaltyCard() {};

    public LoyaltyCard(String offerID, String customerID) {
        this.offerID = offerID;
        this.customerID = customerID;
        this.purchaseCount = "0";
        this.rewardsIssued = "0";
        this.rewardsClaimed = "0";
        this.offerID_customerID = offerID + "_" + customerID;
    }

    public String retrieveCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    // Increases the purchase count by number provided as parameter
    public void addToPurchaseCount(int amountPurchased) {
        int pC = Integer.parseInt(this.purchaseCount);
        pC += amountPurchased;
        this.purchaseCount = Integer.toString(pC);
    }
}
