//This part is done by Raiyan Choudhury
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Custom Exception for Gift Card coupon errors
class InvalidGiftCardException extends Exception {
    public InvalidGiftCardException(String message) {
        super(message);
    }
}

public class SubscriptionManager {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static JFrame dashboardFrame;
    private static Timer refreshTimer;
    private static JTextField searchField;
    private static JPanel searchResultPanel;
    private static JTabbedPane mainTabs;

    private static final List<Subscription> rowSubscriptions = new ArrayList<>();
    private static final List<JProgressBar> rowProgressBars = new ArrayList<>();

    // App Design Theme Colors (Modern Blue Palette)
    private static final Color APP_BG = new Color(222, 235, 247);
    private static final Color TAB_BG = new Color(200, 220, 242);
    private static final Color PANEL_BG = new Color(245, 247, 250);
    private static final Color ACCENT_BLUE = new Color(26, 115, 232);
    private static final Color PRIMARY_TEXT = new Color(32, 33, 36);

    // ===================== DASHBOARD =====================

    public static void showDashboard() {
        dashboardFrame = new JFrame("Subscription Manager (" + Main.currentUser + ")");
        dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboardFrame.setSize(800, 650);
        dashboardFrame.setLocationRelativeTo(null);
        dashboardFrame.getContentPane().setBackground(APP_BG);
        dashboardFrame.setLayout(new BorderLayout());

        // TOP HEADER BAR (Refresh & Logout Buttons)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(PANEL_BG);
        topHeader.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel userLabel = new JLabel("Logged in as: " + Main.currentUser);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLabel.setForeground(PRIMARY_TEXT);

        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topButtonPanel.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            // Fully rebuilds every tab from the current data (including any
            // subscriptions added since the dashboard was last built), not
            // just the progress-bar percentages.
            refreshDashboardUI();
            JOptionPane.showMessageDialog(dashboardFrame, "Dashboard Refreshed!", "System Notification", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(dashboardFrame, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                if (refreshTimer != null) refreshTimer.stop();
                dashboardFrame.dispose();
                // Redirect to Login Window (Replace with your actual Login call)
                JOptionPane.showMessageDialog(null, "Logged out successfully.");
            }
        });

        topButtonPanel.add(refreshBtn);
        topButtonPanel.add(logoutBtn);

        topHeader.add(userLabel, BorderLayout.WEST);
        topHeader.add(topButtonPanel, BorderLayout.EAST);

        dashboardFrame.add(topHeader, BorderLayout.NORTH);

        buildDashboardTabs();
        dashboardFrame.add(mainTabs, BorderLayout.CENTER);

        refreshTimer = new Timer(1000, e -> refreshProgressBarsOnly());
        refreshTimer.start();

        dashboardFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (refreshTimer != null) {
                    refreshTimer.stop();
                }
            }
        });

        dashboardFrame.setVisible(true);
    }

    // Builds (or completely rebuilds) the mainTabs JTabbedPane from the
    // current data in Main.subscriptions / credentials file. Called once
    // when the dashboard first opens, and again any time the data needs to
    // be reflected in the UI (Refresh button, after adding a subscription).
    private static void buildDashboardTabs() {
        // Remember which top-level tab was open so a rebuild doesn't yank
        // the user back to "Budget Overview".
        String previousTabTitle = null;
        if (mainTabs != null && mainTabs.getSelectedIndex() != -1) {
            previousTabTitle = mainTabs.getTitleAt(mainTabs.getSelectedIndex());
        }

        // All category/expired/search rows tracked for the live progress-bar
        // timer are about to be rebuilt from scratch - drop the old ones so
        // the list doesn't grow indefinitely across refreshes.
        rowSubscriptions.clear();
        rowProgressBars.clear();

        mainTabs = new JTabbedPane();
        mainTabs.setBackground(TAB_BG);
        mainTabs.setForeground(PRIMARY_TEXT);
        mainTabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Tab 1: Monthly & Annual Budget
        JPanel budgetTab = new JPanel(new BorderLayout(15, 15));
        budgetTab.setBackground(PANEL_BG);
        budgetTab.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        budgetTab.add(SubscriptionWindow.createBudgetPanel(), BorderLayout.NORTH);
        mainTabs.addTab("Budget Overview", budgetTab);

        // Tab 2: Subscribed Subscriptions (3 Sub-tabs)
        JTabbedPane categoryTabs = new JTabbedPane();
        categoryTabs.setBackground(TAB_BG);
        categoryTabs.setFont(new Font("SansSerif", Font.BOLD, 12));
        categoryTabs.addTab("Software", createCategoryPanel("Software", true));
        categoryTabs.addTab("Streaming", createCategoryPanel("Streaming", true));
        categoryTabs.addTab("Gym", createCategoryPanel("Gym", true));

        categoryTabs.addChangeListener(e -> updateTabColors(categoryTabs));
        updateTabColors(categoryTabs);

        mainTabs.addTab("Subscribed", categoryTabs);

        // Tab 3: Expired Subscriptions
        mainTabs.addTab("Expired", createExpiredSubscriptionsPanel());

        // Tab 4: Search Subscriptions
        mainTabs.addTab("Search", createSearchPanel());

        // Tab 5: Add Subscription
        mainTabs.addTab("Add Subscription", createAddSubscriptionPanel());

        // Tab 6: Admin Panel (Accessible by Admin user)
        if ("admin".equalsIgnoreCase(Main.currentUser)) {
            mainTabs.addTab("Admin Panel", createAdminPanel());
        }

        // Tab 7: About Us
        mainTabs.addTab("About Us", createAboutUsPanel());

        // Active tab color change listener
        mainTabs.addChangeListener(e -> updateTabColors(mainTabs));
        updateTabColors(mainTabs);

        if (previousTabTitle != null) {
            for (int i = 0; i < mainTabs.getTabCount(); i++) {
                if (mainTabs.getTitleAt(i).equals(previousTabTitle)) {
                    mainTabs.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    // Swaps the current mainTabs component out for a freshly rebuilt one
    // and repaints the frame. This is what actually makes "Refresh" (and a
    // successful "Save Subscription") show up-to-date data.
    private static void refreshDashboardUI() {
        if (mainTabs != null) {
            dashboardFrame.remove(mainTabs);
        }
        buildDashboardTabs();
        dashboardFrame.add(mainTabs, BorderLayout.CENTER);
        dashboardFrame.revalidate();
        dashboardFrame.repaint();
    }

    private static void updateTabColors(JTabbedPane tabPane) {
        for (int i = 0; i < tabPane.getTabCount(); i++) {
            if (i == tabPane.getSelectedIndex()) {
                tabPane.setBackgroundAt(i, ACCENT_BLUE); // Dark blue background for current tab
                tabPane.setForegroundAt(i, Color.WHITE);  // White text for visibility
            } else {
                tabPane.setBackgroundAt(i, TAB_BG);
                tabPane.setForegroundAt(i, PRIMARY_TEXT);
            }
        }
    }

    // ===================== ADMIN PANEL =====================

    // Top table: every registered user (username + phone + how many
    // subscriptions they have). Bottom table: the subscriptions belonging
    // to whichever user is selected above. "Delete Selected User" removes
    // the user's login AND their subscriptions file entirely.
    private static JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Admin Control Center - User & Subscription Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(ACCENT_BLUE);
        panel.add(title, BorderLayout.NORTH);

        // ---- Users table ----
        String[] userColumns = {"Username", "Phone", "Subscriptions"};
        DefaultTableModel userModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        Map<String, String[]> allUsers;
        try {
            allUsers = FileManager.readAllCredentials();
        } catch (IOException ex) {
            allUsers = new java.util.HashMap<>();
            JOptionPane.showMessageDialog(dashboardFrame, "Unable to load users: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }

        for (String[] creds : allUsers.values()) {
            String username = creds[0];
            String phone = creds.length > 2 ? creds[2] : "";
            int subCount = 0;
            try {
                subCount = FileManager.readSubscriptionsForUser(username).size();
            } catch (IOException ex) {
                subCount = 0;
            }
            userModel.addRow(new Object[]{username, phone, subCount});
        }

        JTable userTable = new JTable(userModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScroll = new JScrollPane(userTable);
        userScroll.setBorder(BorderFactory.createTitledBorder("Registered Users"));

        // ---- Subscriptions table for whichever user is selected above ----
        String[] subColumns = {"Subscription Name", "Category", "Plan", "Cost ($)", "Renewal Date"};
        DefaultTableModel subModel = new DefaultTableModel(subColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable subTable = new JTable(subModel);
        JScrollPane subScroll = new JScrollPane(subTable);
        subScroll.setBorder(BorderFactory.createTitledBorder("Selected User's Subscriptions"));

        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            subModel.setRowCount(0);
            int row = userTable.getSelectedRow();
            if (row == -1) return;
            String username = (String) userModel.getValueAt(row, 0);
            try {
                for (Subscription sub : FileManager.readSubscriptionsForUser(username)) {
                    subModel.addRow(new Object[]{sub.getName(), sub.getCategory(), sub.getPlan(), sub.getCost(), sub.getRenewalDate()});
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dashboardFrame, "Unable to load subscriptions: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userScroll, subScroll);
        splitPane.setResizeWeight(0.5);
        panel.add(splitPane, BorderLayout.CENTER);

        JButton deleteUserBtn = new JButton("Delete Selected User (Totally)");
        deleteUserBtn.setBackground(Color.RED);
        deleteUserBtn.setForeground(Color.WHITE);
        deleteUserBtn.setFont(new Font("SansSerif", Font.BOLD, 13));

        deleteUserBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dashboardFrame, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String username = (String) userModel.getValueAt(row, 0);

            if (username.equalsIgnoreCase(Main.currentUser)) {
                JOptionPane.showMessageDialog(dashboardFrame, "You cannot delete the account you are currently logged in with.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dashboardFrame,
                    "Permanently delete user \"" + username + "\"?\nThis removes their login AND all of their subscriptions.",
                    "Confirm Delete User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    FileManager.deleteUser(username);
                    userModel.removeRow(row);
                    subModel.setRowCount(0);
                    JOptionPane.showMessageDialog(dashboardFrame, "User \"" + username + "\" has been deleted.", "User Deleted", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dashboardFrame, "Error deleting user: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(deleteUserBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ===================== TAB BUILDERS =====================

    private static JPanel createCategoryPanel(String category, boolean isActiveOnly) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(PANEL_BG);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(PANEL_BG);

        List<Subscription> filtered = Main.subscriptions.stream()
                .filter(s -> s.getCategory().equalsIgnoreCase(category) && (isActiveOnly ? isSubscriptionActive(s) : !isSubscriptionActive(s)))
                .collect(Collectors.toList());

        for (Subscription sub : filtered) {
            list.add(createSubscriptionRow(sub));
        }

        if (filtered.isEmpty()) {
            JLabel emptyLabel = new JLabel("No subscriptions found in this category.");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            list.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private static JPanel createExpiredSubscriptionsPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(PANEL_BG);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(PANEL_BG);

        List<Subscription> expired = Main.subscriptions.stream()
                .filter(s -> !isSubscriptionActive(s))
                .collect(Collectors.toList());

        for (Subscription sub : expired) {
            list.add(createSubscriptionRow(sub));
        }

        if (expired.isEmpty()) {
            JLabel emptyLabel = new JLabel("No expired subscriptions.");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            list.add(emptyLabel);
        }

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private static JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setOpaque(false);
        JLabel searchLbl = new JLabel("Search Subscriptions: ");
        searchLbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        searchField = new JTextField(25);
        searchResultPanel = new JPanel();
        searchResultPanel.setLayout(new BoxLayout(searchResultPanel, BoxLayout.Y_AXIS));
        searchResultPanel.setBackground(PANEL_BG);

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateSearchResults(searchField.getText().trim());
            }
        });

        topBar.add(searchLbl);
        topBar.add(searchField);
        panel.add(topBar, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(searchResultPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        updateSearchResults(""); // Initially empty search view
        return panel;
    }

    private static void updateSearchResults(String query) {
        searchResultPanel.removeAll();

        if (query.trim().isEmpty()) {
            searchResultPanel.revalidate();
            searchResultPanel.repaint();
            return;
        }

        List<Subscription> matched = Main.subscriptions.stream()
                .filter(s -> s.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        for (Subscription sub : matched) {
            searchResultPanel.add(createSubscriptionRow(sub));
        }

        if (matched.isEmpty()) {
            JLabel noRes = new JLabel("No subscriptions match your search query.");
            noRes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            searchResultPanel.add(noRes);
        }

        searchResultPanel.revalidate();
        searchResultPanel.repaint();
    }

    private static JPanel createAddSubscriptionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_BG);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBackground(PANEL_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JTextField nameField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Software", "Streaming", "Gym"});
        JComboBox<String> planBox = new JComboBox<>(new String[]{"Monthly", "Annual"});
        JTextField costField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox autoRenewBox = new JCheckBox("Enabled", true);
        autoRenewBox.setOpaque(false);

        JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"Visa", "MasterCard", "Bkash", "Gift Card"});
        JTextField paymentNumberField = new JTextField();
        JLabel paymentNumberLabel = new JLabel("Card Number (16 digits):");
        JTextField transactionIdField = new JTextField();
        JLabel transactionIdLabel = new JLabel("Transaction ID:");
        JTextField websiteField = new JTextField();

        final int[] monthsRequested = { 1 };

        planBox.addActionListener(e -> {
            if ("Monthly".equalsIgnoreCase((String) planBox.getSelectedItem())) {
                String input = JOptionPane.showInputDialog(dashboardFrame, "Enter number of months:", "Monthly Duration", JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int m = Integer.parseInt(input.trim());
                        if (m <= 0) throw new NumberFormatException();
                        monthsRequested[0] = m;
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dashboardFrame, "Invalid month value. Defaulting to 1 month.", "Error", JOptionPane.ERROR_MESSAGE);
                        monthsRequested[0] = 1;
                    }
                }
            }
        });

        paymentMethodBox.addActionListener(e -> {
            String method = (String) paymentMethodBox.getSelectedItem();
            boolean card = "Visa".equalsIgnoreCase(method) || "MasterCard".equalsIgnoreCase(method);
            boolean bkash = "Bkash".equalsIgnoreCase(method);

            paymentNumberLabel.setText(card ? "Card Number (16 digits):" : "Bkash Phone Number:");
            paymentNumberField.setText("");
            transactionIdField.setText("");
            transactionIdLabel.setVisible(bkash);
            transactionIdField.setVisible(bkash);
            paymentNumberField.setVisible(!"Gift Card".equalsIgnoreCase(method));
            paymentNumberLabel.setVisible(!"Gift Card".equalsIgnoreCase(method));
            form.revalidate();
            form.repaint();
        });

        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(categoryBox);
        form.add(new JLabel("Plan Type:"));
        form.add(planBox);
        form.add(new JLabel("Cost ($):"));
        form.add(costField);
        form.add(new JLabel("Account Email:"));
        form.add(emailField);
        form.add(new JLabel("Account Password:"));
        form.add(passwordField);
        form.add(new JLabel("Auto Renewal:"));
        form.add(autoRenewBox);
        form.add(new JLabel("Payment Method:"));
        form.add(paymentMethodBox);
        form.add(paymentNumberLabel);
        form.add(paymentNumberField);
        form.add(transactionIdLabel);
        form.add(transactionIdField);
        form.add(new JLabel("Subscription Website:"));
        form.add(websiteField);

        transactionIdLabel.setVisible(false);
        transactionIdField.setVisible(false);

        JButton saveBtn = new JButton("Save Subscription");
        saveBtn.setBackground(ACCENT_BLUE);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setFocusPainted(false);

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String category = (String) categoryBox.getSelectedItem();
                String plan = (String) planBox.getSelectedItem();
                double cost = Double.parseDouble(costField.getText().trim());
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());
                boolean autoRenew = autoRenewBox.isSelected();
                String paymentMethod = (String) paymentMethodBox.getSelectedItem();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(dashboardFrame, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String cardOrCoupon;

                if ("Visa".equalsIgnoreCase(paymentMethod) || "MasterCard".equalsIgnoreCase(paymentMethod)) {
                    String cardNumber = paymentNumberField.getText().replaceAll("\\s+", "");
                    if (!cardNumber.matches("\\d{16}")) {
                        throw new IllegalArgumentException("Card number must contain exactly 16 digits.");
                    }
                    cardOrCoupon = cardNumber;
                } else if ("Bkash".equalsIgnoreCase(paymentMethod)) {
                    String phone = paymentNumberField.getText().trim();
                    String transactionId = transactionIdField.getText().trim();
                    if (!phone.matches("01\\d{9}")) {
                        throw new IllegalArgumentException("Enter a valid 11-digit Bkash phone number.");
                    }
                    if (transactionId.isEmpty()) {
                        throw new IllegalArgumentException("Bkash transaction ID is required.");
                    }
                    cardOrCoupon = "Phone: " + phone + " | Transaction ID: " + transactionId;
                } else {
                    String coupon = JOptionPane.showInputDialog(dashboardFrame, "Enter Gift Card Coupon Code (8+ characters):", "Gift Card Validation", JOptionPane.QUESTION_MESSAGE);
                    if (coupon == null || coupon.trim().length() < 6) {
                        throw new InvalidGiftCardException("Invalid Coupon! Coupon code must be at least 6 characters.");
                    }
                    cardOrCoupon = "GC-" + coupon.trim();
                }

                String sellerWebsite = websiteField.getText().trim();
                if (sellerWebsite.isEmpty()) {
                    throw new IllegalArgumentException("Subscription website is required.");
                }
                if (!sellerWebsite.matches("(?i)^[a-z][a-z0-9+.-]*://.*$")) {
                    sellerWebsite = "https://" + sellerWebsite;
                }

                String purchaseDate = LocalDate.now().format(DATE_FMT);
                String purchaseTime = LocalTime.now().format(TIME_FMT);

                LocalDate renewalLocalDate = plan.equalsIgnoreCase("Monthly")
                        ? LocalDate.now().plusMonths(monthsRequested[0])
                        : LocalDate.now().plusYears(1);

                String renewalDate = renewalLocalDate.format(DATE_FMT);

                Subscription sub;
                if ("Monthly".equalsIgnoreCase(plan)) {
                    sub = new MonthlySubscription(name, category, plan, cost, renewalDate,
                            email, password, true, autoRenew, paymentMethod, cardOrCoupon, purchaseDate, purchaseTime, sellerWebsite);
                } else {
                    sub = new AnnualSubscription(name, category, plan, cost, renewalDate,
                            email, password, true, autoRenew, paymentMethod, cardOrCoupon, purchaseDate, purchaseTime, sellerWebsite);
                }

                Main.subscriptions.add(sub);
                FileManager.saveFile();
                JOptionPane.showMessageDialog(dashboardFrame, "Subscription added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                nameField.setText("");
                costField.setText("");
                emailField.setText("");
                passwordField.setText("");
                paymentNumberField.setText("");
                transactionIdField.setText("");
                websiteField.setText("");

                // Rebuild the dashboard so the new subscription shows up in
                // Subscribed/Expired/Search/Admin without needing a manual
                // click on Refresh.
                refreshDashboardUI();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dashboardFrame, "Please enter a valid numeric cost.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dashboardFrame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidGiftCardException ex) {
                JOptionPane.showMessageDialog(dashboardFrame, ex.getMessage(), "Gift Card Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dashboardFrame, "Failed to save: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(form, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 0, 0);
        panel.add(saveBtn, gbc);

        return panel;
    }

    // ===================== ABOUT US TAB (With clickable GitHub links) =====================

    private static JPanel createAboutUsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel header = new JLabel("Project Team Members");
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(ACCENT_BLUE);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createVerticalStrut(15));

        addMemberCard(panel, "Raiyan Choudhury", "2531141042", "raiyan.choudhuary.253@northsouth.edu", "https://github.com/Raiyanc01");
        addMemberCard(panel, "Tafsir Hasan", "2523897642", "tafsir.hasan.252@northsouth.edu", "https://github.com/TafsirHasan10");
        addMemberCard(panel, "Hassan Shayer Palok", "2524003042", "hassan.palok.252@northsouth.edu", "https://github.com/hassanpalok252-CSE");

        panel.add(Box.createVerticalStrut(20));

        // Closing message in bold blue text
        JLabel thankYouMsg = new JLabel("Thank you for using our subscription manager");
        thankYouMsg.setFont(new Font("SansSerif", Font.BOLD, 16));
        thankYouMsg.setForeground(ACCENT_BLUE);
        thankYouMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(thankYouMsg);

        return panel;
    }

    private static void addMemberCard(JPanel parent, String name, String id, String email, String githubUrl) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(235, 243, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 240), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setMaximumSize(new Dimension(550, 105));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel("Name: " + name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel idLbl = new JLabel("ID: " + id);
        idLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel emailLbl = new JLabel("Email: " + email);
        emailLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel githubLbl = new JLabel("<html>GitHub: <a href=''>" + githubUrl + "</a></html>");
        githubLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        githubLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        githubLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(githubUrl));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "Unable to open link: " + ex.getMessage());
                }
            }
        });

        card.add(nameLbl);
        card.add(idLbl);
        card.add(emailLbl);
        card.add(githubLbl);

        parent.add(card);
        parent.add(Box.createVerticalStrut(10));
    }

    // ===================== HELPER UI COMPONENTS =====================

    private static JPanel createSubscriptionRow(Subscription sub) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 225, 240)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(sub.getName() + " (" + sub.getPlan() + ") - $" + String.format("%.2f", sub.getMonthlyCost()) + "/mo");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel autoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        autoPanel.setOpaque(false);

        JLabel autoTitle = new JLabel("Auto-renewal: ");
        autoTitle.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel autoStatus = new JLabel(sub.isAutoRenew() ? "Enabled" : "Disabled");
        autoStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        autoStatus.setForeground(sub.isAutoRenew() ? new Color(46, 125, 50) : Color.RED);

        autoPanel.add(autoTitle);
        autoPanel.add(autoStatus);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(nameLabel, BorderLayout.WEST);
        topRow.add(autoPanel, BorderLayout.EAST);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        updateProgressBar(progressBar, sub);

        rowSubscriptions.add(sub);
        rowProgressBars.add(progressBar);

        JButton detailsBtn = new JButton("Details");
        detailsBtn.addActionListener(e -> showDetailsLogin(sub, false));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> showDetailsLogin(sub, true));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(detailsBtn);
        buttonPanel.add(cancelBtn);

        row.add(topRow, BorderLayout.NORTH);
        row.add(progressBar, BorderLayout.CENTER);
        row.add(buttonPanel, BorderLayout.SOUTH);

        return row;
    }

    private static boolean isSubscriptionActive(Subscription sub) {
        return sub.isActive() && sub.getProgressPercentage() < 100;
    }

    private static void updateProgressBar(JProgressBar bar, Subscription sub) {
        boolean active = isSubscriptionActive(sub);
        int percent = active ? (int) Math.round(sub.getProgressPercentage()) : 100;

        bar.setValue(Math.max(0, Math.min(percent, 100)));

        if (!active || percent >= 100) {
            bar.setUI(new BasicProgressBarUI() {
                @Override
                protected Color getSelectionForeground() {
                    return Color.WHITE;
                }
                @Override
                protected Color getSelectionBackground() {
                    return Color.WHITE;
                }
            });
            bar.setForeground(new Color(220, 53, 69)); // Bright Red bar fill
            bar.setString("This subscription has expired.");
        } else {
            bar.setUI(new BasicProgressBarUI() {
                @Override
                protected Color getSelectionForeground() {
                    return percent >= 80 ? Color.RED : Color.WHITE;
                }
                @Override
                protected Color getSelectionBackground() {
                    return percent >= 80 ? Color.RED : Color.DARK_GRAY;
                }
            });

            if (percent < 60) {
                bar.setForeground(new Color(46, 125, 50)); // Green
            } else if (percent < 80) {
                bar.setForeground(new Color(21, 101, 192)); // Blue
            } else { // Over 80%
                bar.setForeground(Color.YELLOW); // Yellow bar fill with Red text
            }
            bar.setString(percent + "% to renewal (" + sub.getRenewalDate() + ")");
        }
    }

    private static void refreshProgressBarsOnly() {
        for (int i = 0; i < rowSubscriptions.size(); i++) {
            updateProgressBar(rowProgressBars.get(i), rowSubscriptions.get(i));
        }
        SubscriptionWindow.refreshBudgetPanel();
    }

    // ===================== DETAILS & VERIFICATION =====================

    private static void showDetailsLogin(Subscription sub, boolean isCancelAction) {
        JDialog dialog = new JDialog(dashboardFrame, "Verify Account - " + sub.getName(), true);
        dialog.setSize(360, 220);
        dialog.setLocationRelativeTo(dashboardFrame);
        dialog.setLayout(new GridLayout(0, 1, 5, 5));

        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JLabel msg = new JLabel(" ");
        msg.setForeground(Color.RED.darker());
        JButton verifyBtn = new JButton("Verify");

        final int[] attempts = { 0 };
        final int maxAttempts = 3;

        verifyBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.equals(sub.getEmail()) && password.equals(sub.getPassword())) {
                dialog.dispose();
                if (isCancelAction) {
                    handleCancelSubscription(sub);
                } else {
                    showSubscriptionDetails(sub);
                }
            } else {
                attempts[0]++;
                if (attempts[0] >= maxAttempts) {
                    JOptionPane.showMessageDialog(dialog, "Too many failed attempts.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    dialog.dispose();
                } else {
                    msg.setText("Incorrect credentials. Attempts left: " + (maxAttempts - attempts[0]));
                }
            }
        });

        dialog.add(new JLabel("Enter Subscription Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Enter Subscription Password:"));
        dialog.add(passwordField);
        dialog.add(verifyBtn);
        dialog.add(msg);
        dialog.setVisible(true);
    }

    private static void showSubscriptionDetails(Subscription sub) {
        JDialog dialog = new JDialog(dashboardFrame, "Subscription Details - " + sub.getName(), true);
        dialog.setSize(420, 520);
        dialog.setLocationRelativeTo(dashboardFrame);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(detailLine("Category", sub.getCategory()));
        panel.add(detailLine("Name", sub.getName()));
        panel.add(detailLine("Plan", sub.getPlan()));
        panel.add(detailLine("Monthly Cost", "$" + String.format("%.2f", sub.getMonthlyCost())));

        if (sub instanceof AnnualSubscription) {
            panel.add(detailLine("Annual Cost", "$" + String.format("%.2f", sub.getCost())));
        }

        panel.add(detailLine("Purchase Date", sub.getPurchaseDate()));
        panel.add(detailLine("Purchase Time", sub.getPurchaseTime()));
        panel.add(detailLine("Renewal Date", sub.getRenewalDate()));
        panel.add(detailLine("Days Left", calculateDaysLeftText(sub)));
        panel.add(detailLine("Auto Renewal", sub.isAutoRenew() ? "Enabled" : "Disabled"));

        if (sub.getSellerWebsite() != null && !sub.getSellerWebsite().isEmpty()) {
            panel.add(detailLine("Seller Website", sub.getSellerWebsite()));
        }

        panel.add(Box.createVerticalStrut(10));

        JLabel paymentHeader = new JLabel("Payment Details (Protected)");
        paymentHeader.setFont(paymentHeader.getFont().deriveFont(Font.BOLD));
        paymentHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(paymentHeader);

        panel.add(detailLine("Payment Method", sub.getPaymentMethod()));
        panel.add(detailLine("Card/Account", sub.getMaskedCardNumber()));

        panel.add(Box.createVerticalStrut(15));

        JPanel websiteButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        websiteButtonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton websiteButton = new JButton("Open Website");
        websiteButton.setEnabled(sub.getSellerWebsite() != null && !sub.getSellerWebsite().trim().isEmpty());
        websiteButton.addActionListener(e -> openWebsite(sub.getSellerWebsite(), dialog));
        websiteButtonRow.add(websiteButton);

        JButton renewButton = new JButton("Renew");
        renewButton.addActionListener(e -> openWebsite(sub.getSellerWebsite(), dialog));
        websiteButtonRow.add(renewButton);
        panel.add(websiteButtonRow);

        JButton toggleAutoRenewButton = new JButton(
                sub.isAutoRenew() ? "Disable Auto Renewal" : "Enable Auto Renewal");

        toggleAutoRenewButton.addActionListener(e -> {
            sub.setAutoRenew(!sub.isAutoRenew());
            try {
                FileManager.saveFile();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Could not save changes: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
            dialog.dispose();
            showSubscriptionDetails(sub);
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonRow = new JPanel();
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(toggleAutoRenewButton);
        buttonRow.add(closeButton);
        panel.add(buttonRow);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private static void openWebsite(String website, Component parent) {
        if (website == null || website.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No subscription website has been saved.", "Website Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String url = website.trim();
            if (!url.matches("(?i)^[a-z][a-z0-9+.-]*://.*$")) {
                url = "https://" + url;
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Unable to open website: " + ex.getMessage(), "Website Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel detailLine(String label, String value) {
        JPanel line = new JPanel(new BorderLayout());
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel labelComp = new JLabel(label + ": ");
        labelComp.setFont(labelComp.getFont().deriveFont(Font.BOLD));

        JLabel valueComp = new JLabel(value);

        line.add(labelComp, BorderLayout.WEST);
        line.add(valueComp, BorderLayout.CENTER);

        return line;
    }

    private static String calculateDaysLeftText(Subscription sub) {
        try {
            LocalDate renewal = LocalDate.parse(sub.getRenewalDate(), DATE_FMT);
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), renewal);

            if (daysLeft > 0) {
                return daysLeft + " days";
            } else if (daysLeft == 0) {
                return "Renews today!";
            } else {
                return "Already renewed / Expired";
            }
        } catch (DateTimeParseException ex) {
            return "Unknown";
        }
    }

    private static void handleCancelSubscription(Subscription sub) {
        int confirm = JOptionPane.showConfirmDialog(dashboardFrame,
                "Are you sure you want to cancel \"" + sub.getName() + "\"?\nThis will remove it from active subscriptions.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (sub instanceof Cancelable) {
                ((Cancelable) sub).cancelSubscription();
            }
            try {
                FileManager.saveFile();
                JOptionPane.showMessageDialog(dashboardFrame, sub.getName() + " has been cancelled.", "Subscription Cancelled", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(dashboardFrame, "Cancelled, but failed to update file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}