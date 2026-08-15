//This part is done by Hassan Shayer Palok
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

// Thrown when a monthly budget entry would exceed the current annual budget.
class InvalidBudgetException extends Exception {
    public InvalidBudgetException(String message) {
        super(message);
    }
}

public class SubscriptionWindow {
    private static double monthlyBudget = 200;
    private static double annualBudget = 2400;

    private static JLabel monthlyLabel;
    private static JProgressBar monthlyProgressBar;
    
    private static JLabel annualLabel;
    private static JProgressBar annualProgressBar;

    public static double calculateMonthlyExpense() {
        return Main.subscriptions.stream()
                .filter(Subscription::isActive)
                .mapToDouble(Subscription::getMonthlyCost)
                .sum();
    }

    public static double calculateAnnualExpense() {
        return calculateMonthlyExpense() * 12;
    }

    public static void promptBudgets(Component parent) {
        String[] options = {"Monthly Budget", "Annual Budget"};
        int choice = JOptionPane.showOptionDialog(parent,
                "Which budget would you like to edit?",
                "Edit Budget",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) { // Edit Monthly Budget
            boolean success = false;
            while (!success) {
                String input = JOptionPane.showInputDialog(parent, "Enter new Monthly Budget ($):");
                if (input == null) break; // User canceled
                try {
                    double val = Double.parseDouble(input.trim());
                    if (val > annualBudget) {
                        throw new InvalidBudgetException("Monthly budget cannot be more than annual budget.");
                    }
                    monthlyBudget = val;
                    refreshBudgetPanel();
                    JOptionPane.showMessageDialog(parent, "Monthly budget updated successfully!");
                    success = true;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parent, "Please enter a valid numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
                } catch (InvalidBudgetException ex) {
                    JOptionPane.showMessageDialog(parent, ex.getMessage(), "Budget Limit Error", JOptionPane.ERROR_MESSAGE);
                    // Loop continues, asking the user to re-enter the monthly budget.
                }
            }
        } else if (choice == 1) { // Edit Annual Budget
            String input = JOptionPane.showInputDialog(parent, "Enter new Annual Budget ($):");
            if (input != null && !input.trim().isEmpty()) {
                try {
                    double val = Double.parseDouble(input.trim());
                    if (val < monthlyBudget) {
                        JOptionPane.showMessageDialog(parent, "Annual budget cannot be less than monthly budget.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        annualBudget = val;
                        refreshBudgetPanel();
                        JOptionPane.showMessageDialog(parent, "Annual budget updated successfully!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parent, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static JPanel createBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(235, 243, 250)); // Chrome light blue bg
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(66, 133, 244), 1),
                "Spending Overview", 0, 0, new Font("SansSerif", Font.BOLD, 14), new Color(26, 115, 232)));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setOpaque(false);

        // Monthly Budget Sub-Panel
        JPanel monthlyPanel = new JPanel(new BorderLayout(5, 5));
        monthlyPanel.setOpaque(false);
        monthlyLabel = new JLabel();
        monthlyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        monthlyLabel.setForeground(new Color(32, 33, 36));
        monthlyProgressBar = new JProgressBar(0, 100);
        monthlyProgressBar.setStringPainted(true);
        monthlyPanel.add(monthlyLabel, BorderLayout.NORTH);
        monthlyPanel.add(monthlyProgressBar, BorderLayout.CENTER);

        // Annual Budget Sub-Panel
        JPanel annualPanel = new JPanel(new BorderLayout(5, 5));
        annualPanel.setOpaque(false);
        annualLabel = new JLabel();
        annualLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        annualLabel.setForeground(new Color(32, 33, 36));
        annualProgressBar = new JProgressBar(0, 100);
        annualProgressBar.setStringPainted(true);
        annualPanel.add(annualLabel, BorderLayout.NORTH);
        annualPanel.add(annualProgressBar, BorderLayout.CENTER);

        centerPanel.add(monthlyPanel);
        centerPanel.add(annualPanel);

        // Edit Budget button restored to default system styling
        JButton editBudgetButton = new JButton("Edit Budget");
        editBudgetButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        editBudgetButton.addActionListener(e -> promptBudgets(panel));

        JPanel buttonContainer = new JPanel(new GridBagLayout());
        buttonContainer.setOpaque(false);
        buttonContainer.add(editBudgetButton);

        refreshBudgetPanel();

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonContainer, BorderLayout.EAST);

        return panel;
    }

    public static void refreshBudgetPanel() {
        if (monthlyLabel == null || monthlyProgressBar == null || annualLabel == null || annualProgressBar == null) {
            return;
        }

        double monthlyExpense = calculateMonthlyExpense();
        double annualExpense = calculateAnnualExpense();

        int monthlyPct = monthlyBudget > 0 ? (int) ((monthlyExpense / monthlyBudget) * 100) : 0;
        updateBarColors(monthlyProgressBar, monthlyPct, "Monthly");
        monthlyLabel.setText(String.format("Monthly Exp: $%.2f / $%.2f", monthlyExpense, monthlyBudget));

        int annualPct = annualBudget > 0 ? (int) ((annualExpense / annualBudget) * 100) : 0;
        updateBarColors(annualProgressBar, annualPct, "Annual");
        annualLabel.setText(String.format("Annual Exp: $%.2f / $%.2f", annualExpense, annualBudget));
    }

    private static void updateBarColors(JProgressBar bar, int pct, String type) {
        bar.setValue(Math.max(0, Math.min(pct, 100)));
        bar.setString(pct + "% of " + type + " Budget");

        bar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionForeground() {
                return pct >= 80 ? Color.RED : Color.WHITE;
            }
            @Override
            protected Color getSelectionBackground() {
                return pct >= 80 ? Color.RED : Color.DARK_GRAY;
            }
        });

        if (pct < 60) {
            bar.setForeground(new Color(46, 125, 50)); // Green
        } else if (pct < 80) {
            bar.setForeground(new Color(21, 101, 192)); // Blue
        } else { // Over 80%
            bar.setForeground(Color.YELLOW); // Yellow bar fill with Red text
        }
    }
}