package com.passwordmanager.controller;

import com.passwordmanager.model.PasswordEntry;
import com.passwordmanager.service.PasswordGeneratorService;
import com.passwordmanager.service.VaultService;
import com.passwordmanager.util.PasswordStrength;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController {
    @FXML
    private TableView<PasswordEntry> passwordTable;

    @FXML
    private TableColumn<PasswordEntry, String> websiteColumn;

    @FXML
    private TableColumn<PasswordEntry, String> usernameColumn;

    @FXML
    private TableColumn<PasswordEntry, String> passwordColumn;

    @FXML
    private TableColumn<PasswordEntry, Void> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    private final VaultService vaultService;
    private final PasswordGeneratorService passwordGenerator;
    private final ObservableList<PasswordEntry> entriesObservable;

    public MainController(VaultService vaultService) {
        this.vaultService = vaultService;
        this.passwordGenerator = new PasswordGeneratorService();
        this.entriesObservable = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        setupTable();
        loadEntries();
        setupSearch();
    }

    private void setupTable() {
        // Website column (editable)
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        websiteColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        websiteColumn.setOnEditCommit(event -> {
            PasswordEntry entry = event.getRowValue();
            PasswordEntry newEntry = new PasswordEntry(
                    event.getNewValue(),
                    entry.getUsername(),
                    entry.getPassword(),
                    entry.getNotes()
            );
            newEntry.setCreatedDate(entry.getCreatedDate());
            updateEntry(entry, newEntry);
        });

        // Username column (editable)
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        usernameColumn.setOnEditCommit(event -> {
            PasswordEntry entry = event.getRowValue();
            PasswordEntry newEntry = new PasswordEntry(
                    entry.getWebsite(),
                    event.getNewValue(),
                    entry.getPassword(),
                    entry.getNotes()
            );
            newEntry.setCreatedDate(entry.getCreatedDate());
            updateEntry(entry, newEntry);
        });

        // Password column (hidden with dots)
        passwordColumn.setCellValueFactory(cellData -> {
            String password = cellData.getValue().getPassword();
            return new SimpleStringProperty("•".repeat(password != null ? password.length() : 0));
        });

        // Actions column (Edit/Delete buttons)
        actionsColumn.setCellFactory(new Callback<TableColumn<PasswordEntry, Void>, TableCell<PasswordEntry, Void>>() {
            @Override
            public TableCell<PasswordEntry, Void> call(TableColumn<PasswordEntry, Void> param) {
                return new TableCell<>() {
                    private final Button copyBtn = new Button("Copy");
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox buttons = new HBox(5, copyBtn, editBtn, deleteBtn);

                    {
                        copyBtn.getStyleClass().add("action-button");
                        editBtn.getStyleClass().add("action-button");
                        deleteBtn.getStyleClass().add("action-button");
                        deleteBtn.getStyleClass().add("danger-button");

                        copyBtn.setOnAction(event -> {
                            PasswordEntry entry = getTableView().getItems().get(getIndex());
                            copyToClipboard(entry.getPassword());
                        });

                        editBtn.setOnAction(event -> {
                            PasswordEntry entry = getTableView().getItems().get(getIndex());
                            showEditDialog(entry);
                        });

                        deleteBtn.setOnAction(event -> {
                            PasswordEntry entry = getTableView().getItems().get(getIndex());
                            deleteEntry(entry);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttons);
                    }
                };
            }
        });

        passwordTable.setEditable(true);
        passwordTable.setItems(entriesObservable);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEntries(newValue);
        });
    }

    private void loadEntries() {
        entriesObservable.clear();
        entriesObservable.addAll(vaultService.getEntries());
        updateStatus("Loaded " + entriesObservable.size() + " entries");
    }

    private void filterEntries(String keyword) {
        entriesObservable.clear();
        entriesObservable.addAll(vaultService.searchEntries(keyword));
    }

    @FXML
    private void handleAddEntry() {
        showAddDialog();
    }

    @FXML
    private void handleGeneratePassword() {
        showPasswordGeneratorDialog();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Password Manager");
        alert.setHeaderText("JavaFX Password Manager v1.0");
        alert.setContentText("⚠️ EDUCATIONAL PROJECT - NOT FOR PRODUCTION USE\n\n" +
                "This is a learning project demonstrating JavaFX, encryption,\n" +
                "and password management concepts.\n\n" +
                "DO NOT use this to store real, sensitive passwords.\n" +
                "Use established password managers like Bitwarden,\n" +
                "1Password, or KeePass for actual password storage.");
        alert.showAndWait();
    }

    private void showAddDialog() {
        Dialog<PasswordEntry> dialog = new Dialog<>();
        dialog.setTitle("Add Password Entry");
        dialog.setHeaderText("Enter password details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = createEntryForm(null);
        dialog.getDialogPane().setContent(grid);

        TextField websiteField = (TextField) grid.getChildren().get(1);
        TextField usernameField = (TextField) grid.getChildren().get(3);
        TextField passwordField = (TextField) grid.getChildren().get(5);
        TextArea notesArea = (TextArea) grid.getChildren().get(7);
        Button generateBtn = (Button) grid.getChildren().get(8);

        generateBtn.setOnAction(e -> {
            String generated = passwordGenerator.generateStrongPassword(16);
            passwordField.setText(generated);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new PasswordEntry(
                        websiteField.getText(),
                        usernameField.getText(),
                        passwordField.getText(),
                        notesArea.getText()
                );
            }
            return null;
        });

        Optional<PasswordEntry> result = dialog.showAndWait();
        result.ifPresent(entry -> {
            try {
                vaultService.addEntry(entry);
                loadEntries();
                updateStatus("Entry added successfully");
            } catch (Exception e) {
                showError("Error adding entry: " + e.getMessage());
            }
        });
    }

    private void showEditDialog(PasswordEntry entry) {
        Dialog<PasswordEntry> dialog = new Dialog<>();
        dialog.setTitle("Edit Password Entry");
        dialog.setHeaderText("Edit password details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = createEntryForm(entry);
        dialog.getDialogPane().setContent(grid);

        TextField websiteField = (TextField) grid.getChildren().get(1);
        TextField usernameField = (TextField) grid.getChildren().get(3);
        TextField passwordField = (TextField) grid.getChildren().get(5);
        TextArea notesArea = (TextArea) grid.getChildren().get(7);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                PasswordEntry newEntry = new PasswordEntry(
                        websiteField.getText(),
                        usernameField.getText(),
                        passwordField.getText(),
                        notesArea.getText()
                );
                newEntry.setCreatedDate(entry.getCreatedDate());
                return newEntry;
            }
            return null;
        });

        Optional<PasswordEntry> result = dialog.showAndWait();
        result.ifPresent(newEntry -> updateEntry(entry, newEntry));
    }

    private GridPane createEntryForm(PasswordEntry entry) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField websiteField = new TextField();
        websiteField.setPromptText("Website");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes (optional)");
        notesArea.setPrefRowCount(3);

        if (entry != null) {
            websiteField.setText(entry.getWebsite());
            usernameField.setText(entry.getUsername());
            passwordField.setText(entry.getPassword());
            notesArea.setText(entry.getNotes());
        }

        Button generateBtn = new Button("Generate");
        generateBtn.getStyleClass().add("action-button");

        grid.add(new Label("Website:"), 0, 0);
        grid.add(websiteField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(generateBtn, 2, 2);
        grid.add(new Label("Notes:"), 0, 3);
        grid.add(notesArea, 1, 3, 2, 1);

        return grid;
    }

    private void showPasswordGeneratorDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Password Generator");
        dialog.setHeaderText("Generate a strong password");

        ButtonType generateButtonType = new ButtonType("Copy to Clipboard", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CLOSE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Slider lengthSlider = new Slider(8, 32, 16);
        lengthSlider.setShowTickLabels(true);
        lengthSlider.setShowTickMarks(true);
        lengthSlider.setMajorTickUnit(4);
        lengthSlider.setBlockIncrement(1);
        lengthSlider.setSnapToTicks(true);

        Label lengthLabel = new Label("Length: 16");
        lengthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lengthLabel.setText("Length: " + newVal.intValue());
        });

        CheckBox uppercaseCheck = new CheckBox("Uppercase (A-Z)");
        uppercaseCheck.setSelected(true);
        CheckBox lowercaseCheck = new CheckBox("Lowercase (a-z)");
        lowercaseCheck.setSelected(true);
        CheckBox numbersCheck = new CheckBox("Numbers (0-9)");
        numbersCheck.setSelected(true);
        CheckBox symbolsCheck = new CheckBox("Symbols (!@#$...)");
        symbolsCheck.setSelected(true);

        TextField generatedField = new TextField();
        generatedField.setEditable(false);
        generatedField.setPromptText("Generated password will appear here");

        Label strengthLabel = new Label();
        strengthLabel.setStyle("-fx-font-weight: bold;");

        Button generateBtn = new Button("Generate");
        generateBtn.getStyleClass().add("primary-button");
        generateBtn.setOnAction(e -> {
            int length = (int) lengthSlider.getValue();
            String password = passwordGenerator.generatePassword(
                    length,
                    uppercaseCheck.isSelected(),
                    lowercaseCheck.isSelected(),
                    numbersCheck.isSelected(),
                    symbolsCheck.isSelected()
            );
            generatedField.setText(password);

            PasswordStrength strength = passwordGenerator.calculateStrength(password);
            strengthLabel.setText("Strength: " + strength.getLabel());
            strengthLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + strength.getColor() + ";");
        });

        vbox.getChildren().addAll(
                new Label("Password Length:"),
                lengthSlider,
                lengthLabel,
                new Label("Include:"),
                uppercaseCheck,
                lowercaseCheck,
                numbersCheck,
                symbolsCheck,
                generateBtn,
                new Label("Generated Password:"),
                generatedField,
                strengthLabel
        );

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generateButtonType && !generatedField.getText().isEmpty()) {
                return generatedField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            copyToClipboard(password);
            updateStatus("Password copied to clipboard");
        });
    }

    private void updateEntry(PasswordEntry oldEntry, PasswordEntry newEntry) {
        try {
            vaultService.updateEntry(oldEntry, newEntry);
            loadEntries();
            updateStatus("Entry updated successfully");
        } catch (Exception e) {
            showError("Error updating entry: " + e.getMessage());
        }
    }

    private void deleteEntry(PasswordEntry entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete password entry?");
        alert.setContentText("Are you sure you want to delete the entry for " + entry.getWebsite() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                vaultService.deleteEntry(entry);
                loadEntries();
                updateStatus("Entry deleted successfully");
            } catch (Exception e) {
                showError("Error deleting entry: " + e.getMessage());
            }
        }
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        updateStatus("Copied to clipboard");
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
