package com.passwordmanager.controller;

import com.passwordmanager.service.VaultService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private PasswordField masterPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button createVaultButton;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private TextField masterPasswordVisibleField;

    private final VaultService vaultService;

    public LoginController() {
        this.vaultService = new VaultService();
    }

    @FXML
    private void initialize() {
        errorLabel.setText("");
        
        // Set up password visibility toggle
        masterPasswordVisibleField.setManaged(false);
        masterPasswordVisibleField.setVisible(false);
        
        // Bind visible and hidden password fields
        masterPasswordField.textProperty().bindBidirectional(masterPasswordVisibleField.textProperty());
        
        showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                masterPasswordVisibleField.setManaged(true);
                masterPasswordVisibleField.setVisible(true);
                masterPasswordField.setManaged(false);
                masterPasswordField.setVisible(false);
            } else {
                masterPasswordField.setManaged(true);
                masterPasswordField.setVisible(true);
                masterPasswordVisibleField.setManaged(false);
                masterPasswordVisibleField.setVisible(false);
            }
        });

        // Update button visibility based on vault existence
        if (vaultService.vaultExists()) {
            createVaultButton.setDisable(true);
            createVaultButton.setVisible(false);
            loginButton.setDefaultButton(true);
        } else {
            loginButton.setDisable(true);
            loginButton.setVisible(false);
            createVaultButton.setDefaultButton(true);
        }
    }

    @FXML
    private void handleLogin() {
        String password = masterPasswordField.getText();

        if (password == null || password.isEmpty()) {
            showError("Please enter your master password");
            return;
        }

        try {
            if (vaultService.unlockVault(password)) {
                openMainWindow();
            } else {
                showError("Incorrect master password");
                masterPasswordField.clear();
                masterPasswordVisibleField.clear();
            }
        } catch (Exception e) {
            showError("Error unlocking vault: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateVault() {
        String password = masterPasswordField.getText();

        if (password == null || password.length() < 8) {
            showError("Master password must be at least 8 characters");
            return;
        }

        try {
            vaultService.createVault(password);
            openMainWindow();
        } catch (Exception e) {
            showError("Error creating vault: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #f44336;");
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            MainController mainController = new MainController(vaultService);
            loader.setController(mainController);
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) masterPasswordField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Password Manager");
            stage.centerOnScreen();

        } catch (IOException e) {
            showError("Error loading main window: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
