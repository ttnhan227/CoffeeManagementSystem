package controller.admin.pages.users;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

import app.utils.HelperMethods;
import app.utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import model.Datasource;
import model.User;


public class AddUserController {

    @FXML
    private TextField fieldCreateUserName;

    @FXML
    private TextField fieldCreateUserEmail;

    @FXML
    private TextField fieldCreateUserUsername;

    @FXML
    private PasswordField fieldCreateUserPassword;

    @FXML
    private DatePicker fieldCreateUserDOB;

    @FXML
    private ComboBox<String> fieldCreateUserGender;

    @FXML
    private TextField fieldCreateUserPhone;

    @FXML
    private ComboBox<String> fieldCreateUserStatus;

    @FXML
    private Text viewCreateUserResponse;

    @FXML
    public void initialize() {
        // Initialize combo boxes
        fieldCreateUserStatus.getItems().addAll("enabled", "disabled");
        fieldCreateUserGender.getItems().addAll("Male", "Female", "Other");

        // Set default values
        fieldCreateUserStatus.setValue("enabled");
        fieldCreateUserDOB.setValue(null);
        
        // Add listener to clear error styling on date selection
        fieldCreateUserDOB.setOnAction(e -> {
            fieldCreateUserDOB.getStyleClass().remove("error");
            viewCreateUserResponse.setVisible(false);
        });

        // Add listener for phone number validation while typing
        fieldCreateUserPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                fieldCreateUserPhone.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void showError(String message) {
        viewCreateUserResponse.setText(message);
        viewCreateUserResponse.setVisible(true);
    }

    private void highlightErrorField(TextField field) {
        field.getStyleClass().add("error");
        field.setOnKeyTyped(e -> {
            field.getStyleClass().remove("error");
            viewCreateUserResponse.setVisible(false);
        });
    }

    private void highlightErrorDatePicker(DatePicker datePicker) {
        datePicker.getStyleClass().add("error");
        datePicker.setOnAction(e -> {
            datePicker.getStyleClass().remove("error");
            viewCreateUserResponse.setVisible(false);
        });
    }

    @FXML
    public void btnCreateUserOnAction() {
        String fullName = fieldCreateUserName.getText();
        String email = fieldCreateUserEmail.getText();
        String username = fieldCreateUserUsername.getText();
        String password = fieldCreateUserPassword.getText();
        LocalDate dob = fieldCreateUserDOB.getValue();
        String gender = fieldCreateUserGender.getValue();
        String phone = fieldCreateUserPhone.getText();
        String status = fieldCreateUserStatus.getValue();

        // Clear previous error styling
        fieldCreateUserName.getStyleClass().remove("error");
        fieldCreateUserEmail.getStyleClass().remove("error");
        fieldCreateUserUsername.getStyleClass().remove("error");
        fieldCreateUserPassword.getStyleClass().remove("error");
        fieldCreateUserPhone.getStyleClass().remove("error");
        fieldCreateUserDOB.getStyleClass().remove("error");

        // Validate Full Name
        if (fullName.isEmpty() || !HelperMethods.validateFullName(fullName)) {
            showError("Full name must start with a capital letter and be 2-50 characters long. Each word should start with a capital letter.");
            highlightErrorField(fieldCreateUserName);
            return;
        }

        // Validate Username
        if (username.isEmpty() || !HelperMethods.validateUsername(username)) {
            showError("Username must be 3-30 characters long, start with a letter, and contain only letters, numbers, or underscores.");
            highlightErrorField(fieldCreateUserUsername);
            return;
        }

        // Check username availability
        try {
            User userByUsername = Datasource.getInstance().getUserByUsername(username);
            if (userByUsername != null && userByUsername.getUsername() != null) {
                showError("Username is already taken.");
                highlightErrorField(fieldCreateUserUsername);
                return;
            }
        } catch (SQLException e) {
            showError("Error checking username availability.");
            return;
        }

        // Validate Email
        if (email.isEmpty() || !HelperMethods.validateEmail(email)) {
            showError("Enter a valid email address (e.g., user@example.com).");
            highlightErrorField(fieldCreateUserEmail);
            return;
        }

        // Check if email exists using the new method
        if (Datasource.getInstance().isEmailExists(email)) {
            showError("Email is already registered.");
            highlightErrorField(fieldCreateUserEmail);
            return;
        }

        // Validate Password
        if (password.isEmpty() || !HelperMethods.validatePassword(password)) {
            showError("Password requirements:\n" +
                     "• 8-32 characters\n" +
                     "• At least one uppercase letter\n" +
                     "• At least one lowercase letter\n" +
                     "• At least one number");
            highlightErrorField(fieldCreateUserPassword);
            return;
        }

        // Validate Date of Birth
        if (dob == null) {
            showError("Please select a date of birth.");
            fieldCreateUserDOB.getStyleClass().add("error");
            return;
        }

        // Calculate age
        Period age = Period.between(dob, LocalDate.now());
        if (age.getYears() < 18) {
            showError("User must be at least 18 years old.");
            fieldCreateUserDOB.getStyleClass().add("error");
            return;
        }

        // Validate Phone Number (numbers only)
        if (!phone.matches("\\d+")) {
            showError("Phone number must contain numbers only.");
            highlightErrorField(fieldCreateUserPhone);
            return;
        }

        if (phone.length() < 10 || phone.length() > 15) {
            showError("Phone number must be between 10 and 15 digits.");
            highlightErrorField(fieldCreateUserPhone);
            return;
        }

        // Validate other required fields
        if (gender == null || status == null) {
            showError("Please fill in all required fields.");
            return;
        }

        // Generate salt and hash the password
        String salt = PasswordUtils.getSalt(30);
        String securePassword = PasswordUtils.generateSecurePassword(password, salt);

        // Insert the new user
        boolean success = Datasource.getInstance().insertNewUserForm(
                fullName, username, email, securePassword, salt,
                java.sql.Date.valueOf(dob), gender, phone,
                status.toLowerCase()
        );

        if (success) {
            showModernAlert("Success", "User created successfully!");
            redirectToUsersList();
        } else {
            viewCreateUserResponse.setFill(javafx.scene.paint.Color.RED);
            showError("Failed to create user.");
        }
    }

    private void clearForm() {
        fieldCreateUserName.clear();
        fieldCreateUserEmail.clear();
        fieldCreateUserUsername.clear();
        fieldCreateUserPassword.clear();
        fieldCreateUserDOB.setValue(LocalDate.now());
        fieldCreateUserGender.setValue(null);
        fieldCreateUserPhone.clear();
        fieldCreateUserStatus.setValue("enabled");
    }

    private void showModernAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        DialogPane dialogPane = alert.getDialogPane();
        
        // Apply styles directly
        String css = 
            ".dialog-pane {" +
            "    -fx-background-color: white;" +
            "    -fx-padding: 20px;" +
            "    -fx-border-radius: 5px;" +
            "    -fx-background-radius: 5px;" +
            "    -fx-border-color: #e0e0e0;" +
            "    -fx-border-width: 1px;" +
            "    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);" +
            "}" +
            ".dialog-pane > *.button-bar > *.container {" +
            "    -fx-background-color: white;" +
            "}" +
            ".dialog-pane > *.label.content {" +
            "    -fx-font-size: 14px;" +
            "    -fx-padding: 10px 0 15px 0;" +
            "}" +
            ".dialog-pane:header *.header-panel {" +
            "    -fx-background-color: white;" +
            "}" +
            ".dialog-pane *.button {" +
            "    -fx-background-color: #2196F3;" +
            "    -fx-text-fill: white;" +
            "    -fx-background-radius: 4px;" +
            "    -fx-padding: 8px 20px;" +
            "    -fx-cursor: hand;" +
            "}" +
            ".dialog-pane *.button:hover {" +
            "    -fx-background-color: #1976D2;" +
            "}" +
            ".dialog-pane *.button:pressed {" +
            "    -fx-background-color: #0D47A1;" +
            "}" +
            ".dialog-pane > *.graphic-container {" +
            "    -fx-padding: 0;" +
            "}" +
            ".dialog-pane > *.header-panel > *.label {" +
            "    -fx-font-size: 18px;" +
            "    -fx-font-weight: bold;" +
            "}";

        dialogPane.setStyle(css);
        dialogPane.getStyleClass().add("dialog-pane");
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        alert.initStyle(StageStyle.UNDECORATED);
        
        alert.showAndWait();
    }
    private void redirectToUsersList() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/users/users.fxml"));
            AnchorPane root = fxmlLoader.load();

            // Get the current StackPane (usersContent) and update its content
            StackPane usersContent = (StackPane) fieldCreateUserName.getScene().lookup("#usersContent");
            if (usersContent != null) {
                usersContent.getChildren().clear();
                usersContent.getChildren().add(root);

                // Get the controller and refresh the users list
                UsersController usersController = fxmlLoader.getController();
                usersController.listUsers();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
