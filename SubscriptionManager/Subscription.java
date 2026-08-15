//This part is done by Hassan Shayer Palok
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public abstract class Subscription {

    // Basic subscription information
    private String name;
    private String category;
    private String plan;
    private double cost;
    private String renewalDate;

    // Login credentials used to verify the subscription owner
    private String email;
    private String password;

    // Tracks whether the subscription is active and if auto-renewal is enabled
    private boolean active;
    private boolean autoRenew;

    // Stores the payment method, card information, and seller website
    private String paymentMethod;
    private String cardNumber;
    private String sellerWebsite;

    // Stores when the subscription was originally purchased.
    // Used together with the renewal date to calculate a
    // real-time renewal progress percentage.
    private String purchaseDate;
    private String purchaseTime;

    // Shared date/time formats used across the application
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    // Constructor with sellerWebsite (14 parameters)
    public Subscription(String name, String category, String plan,
                        double cost, String renewalDate,
                        String email, String password,
                        boolean active, boolean autoRenew,
                        String paymentMethod, String cardNumber,
                        String purchaseDate, String purchaseTime,
                        String sellerWebsite) {

        this.name = name;
        this.category = category;
        this.plan = plan;
        this.cost = cost;
        this.renewalDate = renewalDate;
        this.email = email;
        this.password = password;

        this.active = active;
        this.autoRenew = autoRenew;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;

        this.purchaseDate = purchaseDate;
        this.purchaseTime = purchaseTime;
        this.sellerWebsite = sellerWebsite;
    }

    // Constructor without sellerWebsite (13 parameters)
    public Subscription(String name, String category, String plan,
                        double cost, String renewalDate,
                        String email, String password,
                        boolean active, boolean autoRenew,
                        String paymentMethod, String cardNumber,
                        String purchaseDate, String purchaseTime) {

        this(name, category, plan, cost, renewalDate, email, password,
             active, autoRenew, paymentMethod, cardNumber, purchaseDate, purchaseTime, null);
    }

    // Returns the subscription name
    public String getName() {
        return name;
    }

    // Returns the subscription category
    public String getCategory() {
        return category;
    }

    // Returns the subscription plan (Monthly/Annual)
    public String getPlan() {
        return plan;
    }

    // Returns the subscription cost
    public double getCost() {
        return cost;
    }

    // Returns the renewal date
    public String getRenewalDate() {
        return renewalDate;
    }

    // Returns the registered email
    public String getEmail() {
        return email;
    }

    // Returns the account password
    public String getPassword() {
        return password;
    }

    // Checks whether the subscription is currently active
    public boolean isActive() {
        return active;
    }

    // Updates the subscription status
    public void setActive(boolean active) {
        this.active = active;
    }

    // Checks whether auto-renewal is enabled
    public boolean isAutoRenew() {
        return autoRenew;
    }

    // Enables or disables auto-renewal
    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    // Returns the payment method
    public String getPaymentMethod() {
        return paymentMethod;
    }

    // Returns the raw card number
    public String getCardNumber() {
        return cardNumber;
    }

    // Returns the seller website
    public String getSellerWebsite() {
        return sellerWebsite;
    }

    // Returns the card number with everything but the last 4 digits hidden.
    // Preserves non-card payment logic such as Bkash.
    public String getMaskedCardNumber() {

        if (cardNumber == null || (paymentMethod != null && paymentMethod.equalsIgnoreCase("Bkash"))) {
            return cardNumber;
        }

        String digitsOnly = cardNumber.replace(" ", "");

        if (digitsOnly.length() < 4) {
            return "****";
        }

        String lastFour = digitsOnly.substring(digitsOnly.length() - 4);

        return "**** **** **** " + lastFour;
    }

    // Returns the date the subscription was originally purchased
    public String getPurchaseDate() {
        return purchaseDate;
    }

    // Returns the time the subscription was originally purchased
    public String getPurchaseTime() {
        return purchaseTime;
    }

    // Calculates how far along (0-100) the subscription currently is
    // between its purchase date/time and its next renewal date.
    public double getProgressPercentage() {

        try {

            LocalDate purchaseLocalDate = LocalDate.parse(purchaseDate, DATE_FORMATTER);
            LocalTime purchaseLocalTime = LocalTime.parse(purchaseTime, TIME_FORMATTER);
            LocalDateTime purchaseDateTime = LocalDateTime.of(purchaseLocalDate, purchaseLocalTime);

            LocalDate renewalLocalDate = LocalDate.parse(renewalDate, DATE_FORMATTER);
            LocalDateTime renewalDateTime = LocalDateTime.of(renewalLocalDate, LocalTime.MIDNIGHT);

            long totalMinutes = Duration.between(purchaseDateTime, renewalDateTime).toMinutes();
            long elapsedMinutes = Duration.between(purchaseDateTime, LocalDateTime.now()).toMinutes();

            if (totalMinutes <= 0) {
                return 100.0;
            }

            double percentage = ((double) elapsedMinutes / (double) totalMinutes) * 100.0;

            return Math.max(0, Math.min(percentage, 100.0));

        } catch (Exception e) {
            // Fall back to 0.0 if parsing fails
            return 0.0;
        }
    }

    // Forces every subclass to calculate and return its monthly cost
    public abstract double getMonthlyCost();
}