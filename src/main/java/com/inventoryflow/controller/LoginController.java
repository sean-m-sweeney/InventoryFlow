package com.inventoryflow.controller;

import com.inventoryflow.App;
import com.inventoryflow.util.DatabaseManager;
import com.inventoryflow.util.HelpDialog;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the login screen handling PIN authentication and initial setup.
 */
public class LoginController {

    @FXML private VBox pinSection;
    @FXML private VBox setupSection;
    @FXML private Label pinPromptLabel;
    @FXML private PasswordField pinField;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;

    // Setup fields
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmPinField;
    @FXML private TextField shopifyDomainField;
    @FXML private PasswordField shopifyTokenField;
    @FXML private Label setupErrorLabel;

    @FXML private HBox loadingBox;

    private final DatabaseManager dbManager = DatabaseManager.getInstance();
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    @FXML
    public void initialize() {
        // Check if PIN is already configured
        if (!dbManager.isPinConfigured()) {
            showSetupSection();
        } else {
            showPinSection();
        }

        // Limit PIN field to 4 digits
        addPinFieldListener(pinField);
        addPinFieldListener(newPinField);
        addPinFieldListener(confirmPinField);
    }

    private void addPinFieldListener(PasswordField field) {
        if (field != null) {
            field.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    // Only allow digits, max 4 characters
                    String filtered = newValue.replaceAll("[^0-9]", "");
                    if (filtered.length() > 4) {
                        filtered = filtered.substring(0, 4);
                    }
                    if (!filtered.equals(newValue)) {
                        field.setText(filtered);
                    }
                }
            });
        }
    }

    private void showSetupSection() {
        pinSection.setVisible(false);
        pinSection.setManaged(false);
        setupSection.setVisible(true);
        setupSection.setManaged(true);

        // Show setup guide automatically on first launch
        Platform.runLater(() -> {
            // Delay slightly to ensure the window is fully rendered
            Platform.runLater(() -> {
                Stage stage = (Stage) setupSection.getScene().getWindow();
                if (stage != null) {
                    HelpDialog.showSetupGuide(stage);
                }
            });
        });
    }

    private void showPinSection() {
        setupSection.setVisible(false);
        setupSection.setManaged(false);
        pinSection.setVisible(true);
        pinSection.setManaged(true);
    }

    @FXML
    private void handlePinSubmit() {
        hideError();
        String pin = pinField.getText();

        if (pin == null || pin.length() != 4) {
            showError("Please enter a 4-digit PIN");
            return;
        }

        if (failedAttempts >= MAX_ATTEMPTS) {
            showError("Too many failed attempts. Please restart the application.");
            submitButton.setDisable(true);
            return;
        }

        showLoading(true);

        // Validate PIN in background thread
        new Thread(() -> {
            boolean valid = dbManager.validatePin(pin);

            Platform.runLater(() -> {
                showLoading(false);
                if (valid) {
                    navigateToDashboard();
                } else {
                    failedAttempts++;
                    int remaining = MAX_ATTEMPTS - failedAttempts;
                    if (remaining > 0) {
                        showError("Invalid PIN. " + remaining + " attempts remaining.");
                    } else {
                        showError("Too many failed attempts. Please restart the application.");
                        submitButton.setDisable(true);
                    }
                    pinField.clear();
                }
            });
        }).start();
    }

    @FXML
    private void handleSetup() {
        hideSetupError();

        String newPin = newPinField.getText();
        String confirmPin = confirmPinField.getText();
        String domain = shopifyDomainField.getText();
        String token = shopifyTokenField.getText();

        // Validate PIN
        if (newPin == null || newPin.length() != 4) {
            showSetupError("PIN must be exactly 4 digits");
            return;
        }

        if (!newPin.equals(confirmPin)) {
            showSetupError("PINs do not match");
            return;
        }

        // Validate Shopify credentials
        if (domain == null || domain.trim().isEmpty()) {
            showSetupError("Please enter your Shopify store domain");
            return;
        }

        if (token == null || token.trim().isEmpty()) {
            showSetupError("Please enter your Shopify Admin API token");
            return;
        }

        // Clean up domain
        domain = domain.trim().toLowerCase();
        if (!domain.contains(".myshopify.com")) {
            if (!domain.contains(".")) {
                domain = domain + ".myshopify.com";
            }
        }

        showLoading(true);

        final String finalDomain = domain;
        new Thread(() -> {
            try {
                // Store credentials
                dbManager.storePin(newPin);
                dbManager.storeShopifyDomain(finalDomain);
                dbManager.storeShopifyToken(token.trim());

                Platform.runLater(() -> {
                    showLoading(false);
                    navigateToDashboard();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    showSetupError("Failed to save settings: " + e.getMessage());
                });
            }
        }).start();
    }

    private void navigateToDashboard() {
        try {
            App.setRoot("dashboard");
        } catch (Exception e) {
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showSetupError(String message) {
        setupErrorLabel.setText(message);
        setupErrorLabel.setVisible(true);
        setupErrorLabel.setManaged(true);
    }

    private void hideSetupError() {
        setupErrorLabel.setVisible(false);
        setupErrorLabel.setManaged(false);
    }

    private void showLoading(boolean show) {
        loadingBox.setVisible(show);
        loadingBox.setManaged(show);
        submitButton.setDisable(show);
    }

    @FXML
    private void handleShowSetupGuide() {
        Stage stage = (Stage) setupSection.getScene().getWindow();
        HelpDialog.showSetupGuide(stage);
    }
}
