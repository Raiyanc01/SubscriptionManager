//This Part is done by Tafsir Hasan
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FileManager {

    private static final String CREDENTIALS_FILE = "credentials.txt";

    public static void ensureFilesExist() throws IOException {
        File credFile = new File(CREDENTIALS_FILE);
        if (!credFile.exists()) {
            credFile.createNewFile();
            // Default accounts. Safe pass for "tafsir" is "Tafsir" and for
            // "admin" is "Admin1" as required. "palok" gets a placeholder
            // safe pass since none was specified.
            saveCredentials("tafsir", "tafsir123", "01700000000", "Tafsir");
            saveCredentials("palok", "palok123@", "01800000000", "Palok1");
            saveCredentials("admin", "admin123", "01900000000", "Admin1");
        }
    }

    public static void readFile() throws IOException {
        if (Main.currentUser != null && !Main.currentUser.trim().isEmpty()) {
            readFile(Main.currentUser);
            return;
        }

        File file = new File("subscriptions.txt");

        if (!file.exists()) {
            file.createNewFile();
            writeDefaultSubscriptions();
        }

        Scanner fileReader = new Scanner(file);

        while (fileReader.hasNextLine()) {
            String line = fileReader.nextLine();
            if (line.trim().isEmpty()) {
                continue;
            }
            Subscription sub = parseSubscriptionLine(line);
            if (sub != null) {
                Main.subscriptions.add(sub);
            }
        }

        fileReader.close();
    }

    public static void readFile(String username) throws IOException {
        String fileName = "subscriptions_" + username + ".txt";
        File file = new File(fileName);

        if (!file.exists()) {
            file.createNewFile();
            writeDefaultSubscriptions(username, fileName);
        }

        Scanner fileReader = new Scanner(file);

        while (fileReader.hasNextLine()) {
            String line = fileReader.nextLine();
            if (line.trim().isEmpty()) {
                continue;
            }
            Subscription sub = parseSubscriptionLine(line);
            if (sub != null) {
                Main.subscriptions.add(sub);
            }
        }

        fileReader.close();
    }

    // Reads a specific user's subscriptions from their file WITHOUT touching
    // Main.subscriptions (which always holds only the currently logged-in
    // user's data). Used by the Admin Panel to inspect any user's
    // subscriptions on demand. Returns an empty list if the user has never
    // had a subscriptions file created.
    public static List<Subscription> readSubscriptionsForUser(String username) throws IOException {
        List<Subscription> result = new ArrayList<>();
        File file = new File("subscriptions_" + username + ".txt");

        if (!file.exists()) {
            return result;
        }

        Scanner fileReader = new Scanner(file);
        while (fileReader.hasNextLine()) {
            String line = fileReader.nextLine();
            if (line.trim().isEmpty()) {
                continue;
            }
            Subscription sub = parseSubscriptionLine(line);
            if (sub != null) {
                result.add(sub);
            }
        }
        fileReader.close();

        return result;
    }

    // Shared line-parsing logic used by readFile(), readFile(username), and
    // readSubscriptionsForUser() so the format is only defined in one place.
    private static Subscription parseSubscriptionLine(String line) {
        String[] data = line.split(",", -1);

        String name = data[0];
        String category = data[1];
        String plan = data[2];
        double cost = Double.parseDouble(data[3]);
        String renewalDate = data[4];
        String email = data[5];
        // Password is stored encrypted in the file - decrypt it back to
        // plain text so the rest of the app keeps working as before.
        String password = EncryptionUtils.decrypt(data[6]);
        boolean active = Boolean.parseBoolean(data[7]);
        boolean autoRenew = Boolean.parseBoolean(data[8]);
        String paymentMethod = data[9];
        String cardNumber = data[10];
        String purchaseDate = data[11];
        String purchaseTime = data[12];
        String sellerWebsite = data.length > 13 ? data[13] : "";

        if (plan.equalsIgnoreCase("Monthly")) {
            return new MonthlySubscription(
                    name, category, plan, cost, renewalDate,
                    email, password, active, autoRenew,
                    paymentMethod, cardNumber, purchaseDate, purchaseTime, sellerWebsite);
        } else if (plan.equalsIgnoreCase("Annual")) {
            return new AnnualSubscription(
                    name, category, plan, cost, renewalDate,
                    email, password, active, autoRenew,
                    paymentMethod, cardNumber, purchaseDate, purchaseTime, sellerWebsite);
        }

        return null;
    }

    public static void saveFile() throws IOException {
        String fileName = (Main.currentUser != null && !Main.currentUser.trim().isEmpty())
                ? "subscriptions_" + Main.currentUser + ".txt"
                : "subscriptions.txt";

        FileWriter writer = new FileWriter(fileName);

        for (Subscription sub : Main.subscriptions) {
            writeSubscriptionLine(writer,
                    sub.getName(),
                    sub.getCategory(),
                    sub.getPlan(),
                    sub.getCost(),
                    sub.getRenewalDate(),
                    sub.getEmail(),
                    sub.getPassword(),
                    sub.isActive(),
                    sub.isAutoRenew(),
                    sub.getPaymentMethod(),
                    sub.getCardNumber(),
                    sub.getPurchaseDate(),
                    sub.getPurchaseTime(),
                    sub.getSellerWebsite());
        }

        writer.close();
    }

    // Writes a single subscription record to file, encrypting the raw
    // password before it ever touches disk.
    private static void writeSubscriptionLine(FileWriter writer, String name, String category, String plan,
            double cost, String renewalDate, String email, String rawPassword, boolean active, boolean autoRenew,
            String paymentMethod, String cardNumber, String purchaseDate, String purchaseTime,
            String sellerWebsite) throws IOException {

        writer.write(String.join(",",
                name,
                category,
                plan,
                String.valueOf(cost),
                renewalDate,
                email,
                EncryptionUtils.encrypt(rawPassword),
                String.valueOf(active),
                String.valueOf(autoRenew),
                paymentMethod,
                cardNumber,
                purchaseDate,
                purchaseTime,
                sellerWebsite == null ? "" : sellerWebsite) + "\n");
    }

    private static void writeDefaultSubscriptionsToFile(String targetFileName) throws IOException {
        FileWriter writer = new FileWriter(targetFileName);

        // Active subscriptions - dates are consistent with the current demo date (16/08/2026).
        writeSubscriptionLine(writer, "Windows 11", "Software", "Monthly", 20.0, "20/08/2026",
                "windowstafsir123@gmail.com", "windowstafsir123", true, true, "Visa",
                "4532 1122 8890 4586", "20/07/2026", "09:00", "https://www.microsoft.com/");
        writeSubscriptionLine(writer, "Adobe Creative Cloud", "Software", "Annual", 240.0, "15/12/2026",
                "adobetafsir234@gmail.com", "adobetafsir123", true, true, "MasterCard",
                "5412 7734 9081 7865", "15/12/2025", "11:30", "https://www.adobe.com/creativecloud.html");
        writeSubscriptionLine(writer, "Microsoft 365", "Software", "Annual", 120.0, "10/10/2026",
                "microsofttafsir345@gmail.com", "microsofttafsir123", true, true, "Visa",
                "4485 6621 3390 0122", "10/10/2025", "08:45", "https://www.microsoft.com/microsoft-365");
        writeSubscriptionLine(writer, "Netflix", "Streaming", "Monthly", 15.0, "25/08/2026",
                "netflixtafsir567@gmail.com", "nettafsir123", true, true, "Visa",
                "4716 2290 5581 4466", "25/07/2026", "19:00", "https://www.netflix.com/");
        writeSubscriptionLine(writer, "Spotify", "Streaming", "Monthly", 11.0, "15/09/2026",
                "spotifytafsir56@gmail.com", "spottafsir123", true, true, "MasterCard",
                "5588 3341 2207 7878", "15/08/2026", "17:20", "https://www.spotify.com/");
        writeSubscriptionLine(writer, "Disney+", "Streaming", "Annual", 120.0, "01/01/2027",
                "disneytafsir09@gmail.com", "disneytafsir123", true, true, "Visa",
                "4024 0071 6650 1243", "01/01/2026", "12:00", "https://www.disneyplus.com/");
        writeSubscriptionLine(writer, "Gold Gym", "Gym", "Monthly", 30.0, "30/08/2026",
                "goldgymtafsir67@gmail.com", "goldtafsir123", true, true, "MasterCard",
                "5299 4471 8823 0989", "30/07/2026", "07:00", "https://www.goldsgym.com/");
        writeSubscriptionLine(writer, "FitZone", "Gym", "Annual", 300.0, "01/11/2026",
                "fitzone11tafsir@gmail.com", "fittafsir123", true, true, "Visa",
                "4147 8832 1190 7712", "01/11/2025", "16:00", "https://www.fitzone.com/");

        // Expired subscriptions - renewal dates are before 16/08/2026.
        writeSubscriptionLine(writer, "X-box Game Pass", "Streaming", "Monthly", 15.0, "08/12/2025",
                "xboxgamepass@gmail.com", "pass123", false, false, "Visa",
                "4532 1122 8890 1234", "08/11/2025", "10:00", "https://www.xbox.com/xbox-game-pass");
        writeSubscriptionLine(writer, "Amazon Prime Watch", "Streaming", "Monthly", 8.99, "08/12/2025",
                "amazonwatch@gmail.com", "watch123", false, false, "Gift Card",
                "GC-9981-2234", "08/11/2025", "10:00", "https://www.amazon.com/prime");

        writer.close();
    }

    private static void writeDefaultSubscriptions() throws IOException {
        writeDefaultSubscriptionsToFile("subscriptions.txt");
    }

    private static void writeDefaultSubscriptions(String username, String fileName) throws IOException {
        if ("palok".equals(username)) {
            FileWriter writer = new FileWriter(fileName);
            writeSubscriptionLine(writer, "Canva", "Software", "Monthly", 12.0, "07/09/2026",
                    "canvapalok123@gmail.com", "canvapalok12", true, true, "Visa",
                    "1234567890123456", "07/08/2026", "10:00", "https://www.canva.com/");
            writeSubscriptionLine(writer, "Hulu", "Streaming", "Monthly", 7.99, "10/09/2026",
                    "hulupalok@gmail.com", "hulu123", true, true, "Gift Card",
                    "GC-7766-1122", "10/08/2026", "08:00", "https://www.hulu.com/");
            writer.close();
        } else {
            writeDefaultSubscriptionsToFile(fileName);
        }
    }

    public static String[] readCredentials() throws IOException {
        File file = new File(CREDENTIALS_FILE);

        if (!file.exists()) {
            ensureFilesExist();
        }

        Scanner reader = new Scanner(file);

        if (!reader.hasNextLine()) {
            reader.close();
            throw new IOException("Credentials file is empty.");
        }

        String line = reader.nextLine();
        reader.close();

        String[] data = line.split(",");

        if (data.length < 4) {
            throw new IOException("Credentials file is corrupted.");
        }

        return data;
    }

    // Returns username -> [username, encryptedPassword, phone, encryptedSafePass]
    public static Map<String, String[]> readAllCredentials() throws IOException {
        Map<String, String[]> users = new HashMap<>();
        File file = new File(CREDENTIALS_FILE);

        if (!file.exists()) {
            ensureFilesExist();
        }

        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] data = line.split(",", -1);
            if (data.length >= 4) {
                users.put(data[0], data);
            }
        }
        reader.close();
        return users;
    }

    // Encrypts the password and safe pass before writing to the credentials file.
    public static void saveCredentials(String username, String password, String phone, String safePass) throws IOException {
        FileWriter writer = new FileWriter(CREDENTIALS_FILE, true);
        writer.write(String.join(",",
                username,
                EncryptionUtils.encrypt(password),
                phone,
                EncryptionUtils.encrypt(safePass)) + "\n");
        writer.close();
    }

    public static void updatePassword(String username, String newPassword, String phone) throws IOException {
        Map<String, String[]> users = readAllCredentials();

        // Preserve the existing (already encrypted) safe pass for this user, if any.
        String[] existing = users.get(username);
        String encryptedSafePass = (existing != null && existing.length > 3)
                ? existing[3]
                : EncryptionUtils.encrypt("");

        users.put(username, new String[] { username, EncryptionUtils.encrypt(newPassword), phone, encryptedSafePass });

        FileWriter writer = new FileWriter(CREDENTIALS_FILE);
        for (String[] creds : users.values()) {
            writer.write(String.join(",", creds) + "\n");
        }
        writer.close();
    }

    // Permanently removes a user: deletes their credentials line AND their
    // subscriptions file. Used by the Admin Panel's "Delete User" action.
    public static void deleteUser(String username) throws IOException {
        Map<String, String[]> users = readAllCredentials();
        users.remove(username);

        FileWriter writer = new FileWriter(CREDENTIALS_FILE);
        for (String[] creds : users.values()) {
            writer.write(String.join(",", creds) + "\n");
        }
        writer.close();

        File subsFile = new File("subscriptions_" + username + ".txt");
        if (subsFile.exists()) {
            subsFile.delete();
        }
    }
}