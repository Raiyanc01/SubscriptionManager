// This part is done by Hassan Shayer Palok
public class MonthlySubscription extends Subscription implements Cancelable {

    // Constructor including sellerWebsite field (14 parameters)
    public MonthlySubscription(String name, String category, String plan, double cost, String renewalDate, 
                               String email, String password, boolean active, boolean autoRenew, 
                               String paymentMethod, String cardNumber, String purchaseDate, 
                               String purchaseTime, String sellerWebsite) {
        super(name, category, plan, cost, renewalDate, email, password, active, autoRenew, 
              paymentMethod, cardNumber, purchaseDate, purchaseTime, sellerWebsite);
    }

    // Constructor without sellerWebsite field (13 parameters)
    public MonthlySubscription(String name, String category, String plan, double cost, String renewalDate, 
                               String email, String password, boolean active, boolean autoRenew, 
                               String paymentMethod, String cardNumber, String purchaseDate, 
                               String purchaseTime) {
        super(name, category, plan, cost, renewalDate, email, password, active, autoRenew, 
              paymentMethod, cardNumber, purchaseDate, purchaseTime);
    }

  
    @Override
    public double getMonthlyCost() {
        return getCost();
    }

    // Displays a message when the subscription is cancelled
    @Override
    public void cancelSubscription() {
        System.out.println(getName() + " subscription cancelled.");
    }
}