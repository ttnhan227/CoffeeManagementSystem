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
    private Text nameError;
    @FXML
    private Text emailError;
    @FXML
    private Text usernameError;
    @FXML
    private Text passwordError;
    @FXML
    private Text phoneError;
    @FXML
    private Text dobError;
    @FXML
    private Text genderError;
    @FXML
    private Text statusError;

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

        // Initialize error texts as invisible
        nameError.setVisible(false);
        emailError.setVisible(false);
        usernameError.setVisible(false);
        passwordError.setVisible(false);
        phoneError.setVisible(false);
        dobError.setVisible(false);
        genderError.setVisible(false);
        statusError.setVisible(false);
    }

    private void clearErrors() {
        nameError.setVisible(false);
        emailError.setVisible(false);
        usernameError.setVisible(false);
        passwordError.setVisible(false);
        phoneError.setVisible(false);
        dobError.setVisible(false);
        genderError.setVisible(false);
        statusError.setVisible(false);
        
        // Clear error styling
        fieldCreateUserName.getStyleClass().remove("error");
        fieldCreateUserEmail.getStyleClass().remove("error");
        fieldCreateUserUsername.getStyleClass().remove("error");
        fieldCreateUserPassword.getStyleClass().remove("error");
        fieldCreateUserPhone.getStyleClass().remove("error");
        fieldCreateUserDOB.getStyleClass().remove("error");
        fieldCreateUserGender.getStyleClass().remove("error");
        fieldCreateUserStatus.getStyleClass().remove("error");
    }

    @FXML
    public void btnCreateUserOnAction() {
        // Clear previous errors
        clearErrors();

        String fullName = fieldCreateUserName.getText();
        String email = fieldCreateUserEmail.getText();
        String username = fieldCreateUserUsername.getText();
        String password = fieldCreateUserPassword.getText();
        LocalDate dob = fieldCreateUserDOB.getValue();
        String gender = fieldCreateUserGender.getValue();
        String phone = fieldCreateUserPhone.getText();
        String status = fieldCreateUserStatus.getValue();

        // Validate Full Name
        if (fullName.isEmpty() || !HelperMethods.validateFullName(fullName)) {
            nameError.setText("Full name must start with a capital letter and be 2-50 characters long.");
            nameError.setVisible(true);
            fieldCreateUserName.getStyleClass().add("error");
            return;
        }

        // Validate Username
        if (username.isEmpty() || !HelperMethods.validateUsername(username)) {
            usernameError.setText("Username must be 3-30 characters long, start with a letter.");
            usernameError.setVisible(true);
            fieldCreateUserUsername.getStyleClass().add("error");
            return;
        }

        // Check username availability
        try {
            User userByUsername = Datasource.getInstance().getUserByUsername(username);
            if (userByUsername != null && userByUsername.getUsername() != null) {
                usernameError.setText("Username is already taken.");
                usernameError.setVisible(true);
                fieldCreateUserUsername.getStyleClass().add("error");
                return;
            }
        } catch (SQLException e) {
            usernameError.setText("Error checking username availability.");
            usernameError.setVisible(true);
            return;
        }

        // Validate Email
        if (email.isEmpty() || !HelperMethods.validateEmail(email)) {
            emailError.setText("Enter a valid email address (e.g., user@example.com).");
            emailError.setVisible(true);
            fieldCreateUserEmail.getStyleClass().add("error");
            return;
        }

        // Check if email exists
        if (Datasource.getInstance().isEmailExists(email)) {
            emailError.setText("Email is already registered.");
            emailError.setVisible(true);
            fieldCreateUserEmail.getStyleClass().add("error");
            return;
        }

        // Validate Password
        if (password.isEmpty() || !HelperMethods.validatePassword(password)) {
            passwordError.setText("Password must be 8-32 characters with at least one uppercase letter, one lowercase letter, and one number.");
            passwordError.setVisible(true);
            fieldCreateUserPassword.getStyleClass().add("error");
            return;
        }

        // Validate Date of Birth
        if (dob == null) {
            dobError.setText("Please select a date of birth.");
            dobError.setVisible(true);
            fieldCreateUserDOB.getStyleClass().add("error");
            return;
        }

        // Calculate age
        Period age = Period.between(dob, LocalDate.now());
        if (age.getYears() < 18) {
            dobError.setText("User must be at least 18 years old.");
            dobError.setVisible(true);
            fieldCreateUserDOB.getStyleClass().add("error");
            return;
        }

        // Validate Phone Number
        if (!phone.matches("\\d+")) {
            phoneError.setText("Phone number must contain numbers only.");
            phoneError.setVisible(true);
            fieldCreateUserPhone.getStyleClass().add("error");
            return;
        }

        if (phone.length() < 10 || phone.length() > 15) {
            phoneError.setText("Phone number must be between 10 and 15 digits.");
            phoneError.setVisible(true);
            fieldCreateUserPhone.getStyleClass().add("error");
            return;
        }

        // Validate Gender and Status
        if (gender == null) {
            genderError.setText("Please select a gender.");
            genderError.setVisible(true);
            fieldCreateUserGender.getStyleClass().add("error");
            return;
        }

        if (status == null) {
            statusError.setText("Please select a status.");
            statusError.setVisible(true);
            fieldCreateUserStatus.getStyleClass().add("error");
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
            nameError.setText("Failed to create user.");
            nameError.setVisible(true);
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
        clearErrors();
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
