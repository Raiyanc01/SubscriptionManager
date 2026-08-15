//This part is done by Hassan Shayer Palok
import java.util.ArrayList;
import javax.swing.JOptionPane;


public class Main {

    // Stores all subscriptions loaded from the text file
    public static ArrayList<Subscription> subscriptions = new ArrayList<>();

    // Tracks the currently logged-in user username
    public static String currentUser = "";

    // Displays all subscriptions (used for testing/debugging)
    static void displayAllSubscriptions() {
        for (Subscription sub : subscriptions) {
            System.out.println(sub.getName() + " - " + sub.getCategory());
        }
    }

    // Loads general subscriptions from the text file into the ArrayList
    private static void loadSubscriptions() {
        try {
            FileManager.readFile();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to load subscription data: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Loads user-specific subscriptions after authenticating
    public static void loadUserSubscriptions() {
        subscriptions.clear();
        try {
            FileManager.readFile(currentUser);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to load subscription data: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Program entry point
    public static void main(String[] args) {
        try {
            FileManager.ensureFilesExist();
        } catch (Exception e) {
            System.out.println("Init error: " + e.getMessage());
        }

        loadSubscriptions();

        // Opens the login window, which itself hands off to the
        // dashboard once the user is authenticated.
        LoginManager.showLoginWindow();
    }
}