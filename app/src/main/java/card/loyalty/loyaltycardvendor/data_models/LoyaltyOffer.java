package card.loyalty.loyaltycardvendor.data_models;

/**
 * Created by Sam on 20/04/2017.
 */

public class LoyaltyOffer {

    public String vendorID;
    public String description;
    public String purchasesPerReward;
    public String reward;

    private String offerID;

    public LoyaltyOffer() {};

    public LoyaltyOffer(String offerID, String vendorID, String description, String purchasesPerReward, String reward) {
        this.vendorID = vendorID;
        this.description = description;
        this.purchasesPerReward = purchasesPerReward;
        this.reward = reward;

        this.offerID = offerID;
    }

}