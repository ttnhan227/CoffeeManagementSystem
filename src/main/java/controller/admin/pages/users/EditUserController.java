package controller.admin.pages.users;

import app.utils.HelperMethods;
import app.utils.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import model.User;
import model.User.Gender;
import model.Datasource;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;
import java.time.Period;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class EditUserController {

    @FXML
    private TextField fieldEditCustomerName;
    @FXML
    private TextField fieldEditCustomerEmail;
    @FXML
    private TextField fieldEditCustomerUsername;
    @FXML
    private TextField fieldEditCustomerPhone;
    @FXML
    private DatePicker fieldEditCustomerDOB;
    @FXML
    private ComboBox<Gender> fieldEditCustomerGender;
    @FXML
    private ComboBox<String> fieldEditCustomerStatus;
    @FXML
    private Text nameError;
    @FXML
    private Text emailError;
    @FXML
    private Text usernameError;
    @FXML
    private Text phoneError;
    @FXML
    private Text dobError;
    @FXML
    private Text passwordError;
    @FXML
    private Text confirmPasswordError;
    @FXML
    private TextField fieldEditCustomerId;
    @FXML
    private PasswordField fieldEditCustomerPassword;
    @FXML
    private PasswordField fieldEditCustomerConfirmPassword;

    private String currentUserSalt;

    @FXML
    private void initialize() {
        // Populate the status combo box with predefined statuses
        fieldEditCustomerStatus.setItems(FXCollections.observableArrayList("enabled", "disabled"));

        // Populate the gender combo box with enum values
        fieldEditCustomerGender.setItems(FXCollections.observableArrayList(Gender.values()));

        // Initialize error texts as invisible
        nameError.setVisible(false);
        emailError.setVisible(false);
        usernameError.setVisible(false);
        phoneError.setVisible(false);
        dobError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
    }

    @FXML
    private void btnEditCustomerOnAction() {
        int customerId = Integer.parseInt(fieldEditCustomerId.getText());
        String fullname = fieldEditCustomerName.getText();
        String email = fieldEditCustomerEmail.getText();
        String username = fieldEditCustomerUsername.getText();
        String phoneNumber = fieldEditCustomerPhone.getText();
        LocalDate dob = fieldEditCustomerDOB.getValue();
        Gender gender = fieldEditCustomerGender.getValue();
        String status = fieldEditCustomerStatus.getValue();
        String newPassword = fieldEditCustomerPassword.getText();
        String confirmPassword = fieldEditCustomerConfirmPassword.getText();

        // Clear all previous error messages
        clearErrors();

        // Clear any previous error styling
        fieldEditCustomerName.getStyleClass().remove("error");
        fieldEditCustomerEmail.getStyleClass().remove("error");
        fieldEditCustomerUsername.getStyleClass().remove("error");
        fieldEditCustomerPhone.getStyleClass().remove("error");
        fieldEditCustomerPassword.getStyleClass().remove("error");
        fieldEditCustomerConfirmPassword.getStyleClass().remove("error");

        // Validate Full Name
        if (!HelperMethods.validateFullName(fullname)) {
            nameError.setText("Full name must start with a capital letter and be 2-50 characters long.");
            nameError.setVisible(true);
            fieldEditCustomerName.getStyleClass().add("error");
            return;
        }

        // Validate Email
        if (!HelperMethods.validateEmail(email)) {
            emailError.setText("Please enter a valid email address.");
            emailError.setVisible(true);
            fieldEditCustomerEmail.getStyleClass().add("error");
            return;
        }

        // Validate Username
        if (!HelperMethods.validateUsername(username)) {
            usernameError.setText("Username must be 3-30 characters long, start with a letter.");
            usernameError.setVisible(true);
            fieldEditCustomerUsername.getStyleClass().add("error");
            return;
        }

        // Check username availability
        try {
            User existingUser = Datasource.getInstance().getUserByUsername(username);
            if (existingUser != null && existingUser.getUsername() != null 
                && existingUser.getId() != customerId) {
                usernameError.setText("Username is already taken.");
                usernameError.setVisible(true);
                fieldEditCustomerUsername.getStyleClass().add("error");
                return;
            }
        } catch (SQLException e) {
            usernameError.setText("Error checking username availability.");
            usernameError.setVisible(true);
            return;
        }

        // Check email availability
        try {
            User existingUser = Datasource.getInstance().getUserByEmail(email);
            if (existingUser != null && existingUser.getEmail() != null 
                && existingUser.getId() != customerId) {
                emailError.setText("Email is already registered.");
                emailError.setVisible(true);
                fieldEditCustomerEmail.getStyleClass().add("error");
                return;
            }
        } catch (SQLException e) {
            emailError.setText("Error checking email availability.");
            emailError.setVisible(true);
            return;
        }

        // Validate Password if provided
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordError.setText("Passwords do not match!");
                confirmPasswordError.setVisible(true);
                fieldEditCustomerPassword.getStyleClass().add("error");
                fieldEditCustomerConfirmPassword.getStyleClass().add("error");
                return;
            }
            
            if (!HelperMethods.validatePassword(newPassword)) {
                passwordError.setText("Password must be 8-32 characters with at least one uppercase letter, one lowercase letter, and one number.");
                passwordError.setVisible(true);
                fieldEditCustomerPassword.getStyleClass().add("error");
                return;
            }
        }

        // Validate other required fields
        if (dob == null || gender == null || status == null || phoneNumber.isEmpty()) {
            if (dob == null) {
                dobError.setText("Date of birth is required.");
                dobError.setVisible(true);
                fieldEditCustomerDOB.getStyleClass().add("error");
            }
            if (gender == null) {
                nameError.setText("Gender is required.");
                nameError.setVisible(true);
                fieldEditCustomerGender.getStyleClass().add("error");
            }
            if (status == null) {
                emailError.setText("Status is required.");
                emailError.setVisible(true);
                fieldEditCustomerStatus.getStyleClass().add("error");
            }
            if (phoneNumber.isEmpty()) {
                phoneError.setText("Phone number is required.");
                phoneError.setVisible(true);
                fieldEditCustomerPhone.getStyleClass().add("error");
            }
            return;
        }

        // Validate Date of Birth (must be 18+ years old)
        if (dob == null) {
            dobError.setText("Please select a date of birth.");
            dobError.setVisible(true);
            fieldEditCustomerDOB.getStyleClass().add("error");
            return;
        }

        // Calculate age
        Period age = Period.between(dob, LocalDate.now());
        if (age.getYears() < 18) {
            dobError.setText("User must be at least 18 years old.");
            dobError.setVisible(true);
            fieldEditCustomerDOB.getStyleClass().add("error");
            return;
        }

        // Validate Phone Number (numbers only)
        if (!phoneNumber.matches("\\d+")) {
            phoneError.setText("Phone number must contain numbers only.");
            phoneError.setVisible(true);
            fieldEditCustomerPhone.getStyleClass().add("error");
            return;
        }

        if (phoneNumber.length() < 10 || phoneNumber.length() > 15) {
            phoneError.setText("Phone number must be between 10 and 15 digits.");
            phoneError.setVisible(true);
            fieldEditCustomerPhone.getStyleClass().add("error");
            return;
        }

        // Continue with update if all validations pass
        Task<Boolean> updateCustomerTask = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                java.sql.Date sqlDateOfBirth = java.sql.Date.valueOf(dob);
                
                String hashedPassword = null;
                String salt = null;
                
                if (!newPassword.isEmpty()) {
                    salt = PasswordUtils.getSalt(30);
                    hashedPassword = PasswordUtils.generateSecurePassword(newPassword, salt);
                }

                return Datasource.getInstance().updateOneUser(
                        customerId,
                        fullname,
                        username,
                        email,
                        status,
                        sqlDateOfBirth,
                        phoneNumber,
                        gender,
                        hashedPassword,
                        salt
                );
            }
        };

        updateCustomerTask.setOnSucceeded(e -> {
            if (updateCustomerTask.valueProperty().get()) {
                showModernAlert("Success", "User updated successfully!");
                redirectToUsersList();
            } else {
                nameError.setText("Failed to update user.");
                nameError.setVisible(true);
            }
        });

        updateCustomerTask.setOnFailed(e -> {
            Throwable throwable = updateCustomerTask.getException();
            nameError.setText("Error: " + throwable.getMessage());
            nameError.setVisible(true);
        });

        new Thread(updateCustomerTask).start();
    }

    public void fillEditingCustomerFields(int customer_id) {
        Task<List<User>> fillCustomerTask = new Task<List<User>>() {
            @Override
            protected List<User> call() {
                return Datasource.getInstance().getOneUser(customer_id);
            }
        };

        fillCustomerTask.setOnSucceeded(e -> {  
            List<User> users = fillCustomerTask.getValue();
            if (users != null && !users.isEmpty()) {
                User user = users.get(0);

                fieldEditCustomerId.setText(String.valueOf(user.getId()));
                fieldEditCustomerName.setText(user.getFullname() != null ? user.getFullname() : "");
                fieldEditCustomerEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                fieldEditCustomerUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                fieldEditCustomerPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

                // Handle Date of Birth - Modified conversion
                if (user.getDateOfBirth() != null) {
                    java.sql.Date sqlDate = (java.sql.Date) user.getDateOfBirth();
                    fieldEditCustomerDOB.setValue(sqlDate.toLocalDate());
                }

                // Handle Gender
                if (user.getGender() != null) {
                    fieldEditCustomerGender.setValue(user.getGender());
                }

                // Handle Status
                if (user.getStatus() != null && !user.getStatus().isEmpty()) {
                    fieldEditCustomerStatus.setValue(user.getStatus().toLowerCase());
                }

                // Clear any visible error messages
                clearErrors();
            } else {
                nameError.setText("User not found.");
                nameError.setVisible(true);
                clearFields();
            }
        });

        fillCustomerTask.setOnFailed(e -> {
            Throwable throwable = fillCustomerTask.getException();
            nameError.setText("Error: " + throwable.getMessage());
            nameError.setVisible(true);
            clearFields();
        });

        new Thread(fillCustomerTask).start();
    }

    private void clearFields() {
        fieldEditCustomerId.setText("");
        fieldEditCustomerName.setText("");
        fieldEditCustomerEmail.setText("");
        fieldEditCustomerUsername.setText("");
        fieldEditCustomerPhone.setText("");
        fieldEditCustomerDOB.setValue(null);
        fieldEditCustomerGender.setValue(null);
        fieldEditCustomerStatus.setValue(null);
        fieldEditCustomerPassword.clear();
        fieldEditCustomerConfirmPassword.clear();
    }

    private boolean areCustomerInputsValid(String fullname, String email, String username,
                                           String phoneNumber, LocalDate dob, Gender gender, String status) {
        return !fullname.isEmpty() &&
                !email.isEmpty() &&
                !username.isEmpty() &&
                !phoneNumber.isEmpty() &&
                dob != null &&
                gender != null &&
                status != null;
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
            StackPane usersContent = (StackPane) fieldEditCustomerName.getScene().lookup("#usersContent");
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

    // Clear all previous error messages
    private void clearErrors() {
        nameError.setVisible(false);
        emailError.setVisible(false);
        usernameError.setVisible(false);
        phoneError.setVisible(false);
        dobError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
    }
}