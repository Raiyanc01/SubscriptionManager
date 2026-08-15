// This Part is done by Hassan Shayer Palok

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;

// Thrown when the entered safe pass is not exactly 6 characters
// (letters/numbers), e.g. "Tafsir" or "Admin1".
class InvalidSafePassException extends Exception {
    public InvalidSafePassException(String message) {
        super(message);
    }
}

public class LoginManager {

    // Maximum number of login attempts allowed before the form locks
    private static final int MAX_ATTEMPTS = 3;
    private static int attempts = 0;

    private static JFrame loginFrame;
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private static JPasswordField safePassField;
    private static JLabel messageLabel;
    private static JButton loginButton;
    private static JButton registerButton;
    private static JButton forgotPasswordButton;

    // Builds and displays the login window
    public static void showLoginWindow() {

        loginFrame = new JFrame("Subscription Manager By R_T_P - Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(380, 300);
        loginFrame.setResizable(false);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginFrame.add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        loginFrame.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginFrame.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        loginFrame.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginFrame.add(new JLabel("Write your safe pass:"), gbc);

        safePassField = new JPasswordField(15);
        gbc.gridx = 1;
        loginFrame.add(safePassField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginFrame.add(loginButton, gbc);

        registerButton = new JButton("Register");
        gbc.gridx = 1;
        loginFrame.add(registerButton, gbc);

        forgotPasswordButton = new JButton("Forgot Password?");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginFrame.add(forgotPasswordButton, gbc);

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        loginFrame.add(messageLabel, gbc);

        loginButton.addActionListener(LoginManager::handleLogin);
        registerButton.addActionListener(e -> showRegisterDialog());
        forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog());

        // Allows pressing Enter inside the safe pass field to log in
        safePassField.addActionListener(LoginManager::handleLogin);

        loginFrame.setVisible(true);
    }

    // Validates that a safe pass is exactly 6 characters (letters/numbers),
    // e.g. "Tafsir" or "Admin1".
    private static void validateSafePass(String safePass) throws InvalidSafePassException {
        if (safePass == null || !safePass.matches("^[A-Za-z0-9]{6}$")) {
            throw new InvalidSafePassException("Safe pass must be exactly 6 letters/numbers.");
        }
    }

    // Validates the entered credentials (username, password, and safe pass)
    // against the saved credentials file
    private static void handleLogin(ActionEvent e) {

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String safePass = new String(safePassField.getPassword());

        try {

            if (username.isEmpty() || password.isEmpty()) {
                throw new IllegalArgumentException("Username and password cannot be empty.");
            }

            validateSafePass(safePass);

            Map<String, String[]> users = FileManager.readAllCredentials();

            boolean userExists = users.containsKey(username);
            boolean passwordMatches = userExists
                    && EncryptionUtils.decrypt(users.get(username)[1]).equals(password);
            boolean safePassMatches = userExists
                    && users.get(username).length > 3
                    && EncryptionUtils.decrypt(users.get(username)[3]).equals(safePass);

            if (passwordMatches && safePassMatches) {

                messageLabel.setForeground(new Color(0, 128, 0));
                messageLabel.setText("Login Successful!");

                attempts = 0;
                Main.currentUser = username;
                Main.loadUserSubscriptions();
                loginFrame.dispose();

                // Hands off to the main dashboard window
                SubscriptionManager.showDashboard();

            } else {

                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;

                if (remaining > 0) {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Incorrect credentials or safe pass. Attempts left: " + remaining);
                } else {
                    lockLogin();
                }
            }

        } catch (InvalidSafePassException ex) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText(ex.getMessage());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(loginFrame,
                    "Unable to read login credentials: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Disables the login form after the maximum number of failed attempts
    private static void lockLogin() {

        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        safePassField.setEnabled(false);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        messageLabel.setForeground(Color.RED);
        messageLabel.setText("<html>Access Denied.<br>Use \"Forgot Password?\" to reset.</html>");
    }

    // Displays the dialog to register a new user profile
    private static void showRegisterDialog() {
        JDialog dialog = new JDialog(loginFrame, "Register New User", true);
        dialog.setSize(320, 240);
        dialog.setLayout(new GridLayout(0, 2, 8, 8));
        dialog.setLocationRelativeTo(loginFrame);

        JTextField regUser = new JTextField();
        JPasswordField regPass = new JPasswordField();
        JTextField regPhone = new JTextField();
        JTextField regSafePass = new JTextField();

        dialog.add(new JLabel("Username:"));
        dialog.add(regUser);
        dialog.add(new JLabel("Password:"));
        dialog.add(regPass);
        dialog.add(new JLabel("Phone:"));
        dialog.add(regPhone);
        dialog.add(new JLabel("Write your safe pass:"));
        dialog.add(regSafePass);

        JButton submit = new JButton("Register");
        submit.addActionListener(e -> {
            try {
                String u = regUser.getText().trim();
                String p = new String(regPass.getPassword());
                String ph = regPhone.getText().trim();
                String sp = regSafePass.getText().trim();

                if (u.isEmpty() || p.isEmpty() || ph.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!ph.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(dialog, "Phone number must contain exactly 11 digits.", "Invalid Phone Number", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                validateSafePass(sp);

                if (FileManager.readAllCredentials().containsKey(u)) {
                    JOptionPane.showMessageDialog(dialog, "User exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                FileManager.saveCredentials(u, p, ph, sp);
                JOptionPane.showMessageDialog(dialog, "Registered Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

            } catch (InvalidSafePassException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Safe Pass Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Unable to save credentials: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(submit);
        dialog.setVisible(true);
    }

    // Opens a dialog that lets the user verify their identity using
    // their username and phone number before resetting their password
    private static void showForgotPasswordDialog() {

        JDialog dialog = new JDialog(loginFrame, "Reset Password", true);
        dialog.setSize(360, 220);
        dialog.setLocationRelativeTo(loginFrame);
        dialog.setLayout(new GridLayout(0, 2, 8, 8));

        JTextField usernameInput = new JTextField();
        JTextField phoneInput = new JTextField();
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);

        JButton verifyButton = new JButton("Verify");

        dialog.add(new JLabel("Username:"));
        dialog.add(usernameInput);
        dialog.add(new JLabel("Phone Number:"));
        dialog.add(phoneInput);
        dialog.add(verifyButton);
        dialog.add(statusLabel);

        verifyButton.addActionListener(e -> {

            try {

                String enteredUsername = usernameInput.getText().trim();
                String enteredPhone = phoneInput.getText().trim();

                if (enteredUsername.isEmpty() || enteredPhone.isEmpty()) {
                    throw new IllegalArgumentException("Username and phone number are required.");
                }

                if (!enteredPhone.matches("\\d{11}")) {
                    throw new IllegalArgumentException("Phone number must contain exactly 11 digits.");
                }

                Map<String, String[]> users = FileManager.readAllCredentials();

                if (users.containsKey(enteredUsername) && users.get(enteredUsername)[2].equals(enteredPhone)) {
                    dialog.dispose();
                    showNewPasswordDialog(enteredUsername, enteredPhone);
                } else {
                    statusLabel.setText("Username or phone number is incorrect.");
                }

            } catch (IllegalArgumentException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Unable to read login credentials: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    // Opens a dialog that lets the now-verified user choose a new password
    private static void showNewPasswordDialog(String username, String phone) {

        JDialog dialog = new JDialog(loginFrame, "Set New Password", true);
        dialog.setSize(360, 200);
        dialog.setLocationRelativeTo(loginFrame);
        dialog.setLayout(new GridLayout(0, 2, 8, 8));

        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);

        JButton saveButton = new JButton("Save New Password");

        dialog.add(new JLabel("New Password:"));
        dialog.add(newPasswordField);
        dialog.add(new JLabel("Confirm Password:"));
        dialog.add(confirmPasswordField);
        dialog.add(saveButton);
        dialog.add(statusLabel);

        saveButton.addActionListener(e -> {

            try {

                String newPassword = new String(newPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                if (newPassword.isEmpty()) {
                    throw new IllegalArgumentException("Password cannot be empty.");
                }

                if (!newPassword.equals(confirmPassword)) {
                    throw new IllegalArgumentException("Passwords do not match.");
                }

                FileManager.updatePassword(username, newPassword, phone);

                JOptionPane.showMessageDialog(dialog,
                        "Password updated successfully! Please log in again.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();

                // Resets the login attempt counter and re-enables the login form
                attempts = 0;
                usernameField.setEnabled(true);
                passwordField.setEnabled(true);
                safePassField.setEnabled(true);
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
                usernameField.setText(username);
                passwordField.setText("");
                safePassField.setText("");
                messageLabel.setForeground(Color.RED);
                messageLabel.setText(" ");

            } catch (IllegalArgumentException ex) {
                statusLabel.setText(ex.getMessage());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Unable to save the new password: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}