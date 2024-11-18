package controller.admin.pages.users;

import java.sql.SQLException;
import java.time.LocalDate;

import app.utils.HelperMethods;
import app.utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
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
        fieldCreateUserDOB.setValue(LocalDate.now());
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

        // Validate other required fields
        if (dob == null || gender == null || phone.isEmpty() || status == null) {
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
            viewCreateUserResponse.setFill(javafx.scene.paint.Color.GREEN);
            showError("User created successfully!");
            clearForm();
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
}
