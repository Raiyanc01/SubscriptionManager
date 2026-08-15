// This Part is done by Tafsir Hasan
public class AnnualSubscription extends Subscription implements Cancelable {

    // Initializes an annual subscription by passing all values
    // to the parent Subscription constructor
    public AnnualSubscription(String name, String category, String plan,
                              double cost, String renewalDate, String email, String password,
                              boolean active, boolean autoRenew,
                              String paymentMethod, String cardNumber,
                              String purchaseDate, String purchaseTime, String sellerWebsite) {
        super(name, category, plan, cost, renewalDate, email, password,
              active, autoRenew, paymentMethod, cardNumber,
              purchaseDate, purchaseTime, sellerWebsite);
    }

    // Calculates the monthly cost by dividing
    // the annual subscription cost by 12 months
    @Override
    public double getMonthlyCost() {
        return getCost() / 12;
    }

    // Displays a message when the subscription is cancelled
    @Override
    public void cancelSubscription() {
        System.out.println(getName() + " subscription cancelled.");
    }
}