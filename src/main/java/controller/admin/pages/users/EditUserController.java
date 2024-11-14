package controller.admin.pages.users;

import app.utils.HelperMethods;
import app.utils.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.User;
import model.User.Gender;
import model.Datasource;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.sql.SQLException;

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
    private Text viewCustomerResponse;
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

        // Clear any previous error styling
        fieldEditCustomerName.getStyleClass().remove("error");
        fieldEditCustomerEmail.getStyleClass().remove("error");
        fieldEditCustomerUsername.getStyleClass().remove("error");
        fieldEditCustomerPhone.getStyleClass().remove("error");
        fieldEditCustomerPassword.getStyleClass().remove("error");
        fieldEditCustomerConfirmPassword.getStyleClass().remove("error");

        // Validate Full Name
        if (!HelperMethods.validateFullName(fullname)) {
            viewCustomerResponse.setText("Full name must start with a capital letter and be 2-50 characters long.");
            viewCustomerResponse.setVisible(true);
            fieldEditCustomerName.getStyleClass().add("error");
            return;
        }

        // Validate Email
        if (!HelperMethods.validateEmail(email)) {
            viewCustomerResponse.setText("Please enter a valid email address.");
            viewCustomerResponse.setVisible(true);
            fieldEditCustomerEmail.getStyleClass().add("error");
            return;
        }

        // Validate Username
        if (!HelperMethods.validateUsername(username)) {
            viewCustomerResponse.setText("Username must be 3-30 characters long, start with a letter.");
            viewCustomerResponse.setVisible(true);
            fieldEditCustomerUsername.getStyleClass().add("error");
            return;
        }

        // Check if username is taken (excluding current user)
        try {
            User existingUser = Datasource.getInstance().getUserByUsername(username);
            if (existingUser != null && existingUser.getUsername() != null 
                && existingUser.getId() != customerId) {
                viewCustomerResponse.setText("Username is already taken.");
                viewCustomerResponse.setVisible(true);
                fieldEditCustomerUsername.getStyleClass().add("error");
                return;
            }
        } catch (SQLException e) {
            viewCustomerResponse.setText("Error checking username availability.");
            viewCustomerResponse.setVisible(true);
            return;
        }

        // Check if email is taken (excluding current user)
        try {
            User existingUser = Datasource.getInstance().getUserByEmail(email);
            if (existingUser != null && existingUser.getEmail() != null 
                && existingUser.getId() != customerId) {
                viewCustomerResponse.setText("Email is already registered.");
                viewCustomerResponse.setVisible(true);
                fieldEditCustomerEmail.getStyleClass().add("error");
                return;
            }
        } catch (SQLException e) {
            viewCustomerResponse.setText("Error checking email availability.");
            viewCustomerResponse.setVisible(true);
            return;
        }

        // Validate Password if provided
        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                viewCustomerResponse.setText("Passwords do not match!");
                viewCustomerResponse.setVisible(true);
                fieldEditCustomerPassword.getStyleClass().add("error");
                fieldEditCustomerConfirmPassword.getStyleClass().add("error");
                return;
            }
            
            if (!HelperMethods.validatePassword(newPassword)) {
                viewCustomerResponse.setText("Password must be 8-32 characters with at least one uppercase letter, one lowercase letter, and one number.");
                viewCustomerResponse.setVisible(true);
                fieldEditCustomerPassword.getStyleClass().add("error");
                return;
            }
        }

        // Validate other required fields
        if (dob == null || gender == null || status == null || phoneNumber.isEmpty()) {
            viewCustomerResponse.setText("Please fill in all required fields.");
            viewCustomerResponse.setVisible(true);
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
                viewCustomerResponse.setText("User updated successfully!");
                viewCustomerResponse.setVisible(true);
                fieldEditCustomerPassword.clear();
                fieldEditCustomerConfirmPassword.clear();
            } else {
                viewCustomerResponse.setText("Failed to update user.");
                viewCustomerResponse.setVisible(true);
            }
        });

        updateCustomerTask.setOnFailed(e -> {
            Throwable throwable = updateCustomerTask.getException();
            viewCustomerResponse.setText("Error: " + throwable.getMessage());
            viewCustomerResponse.setVisible(true);
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

                viewCustomerResponse.setVisible(false);

            } else {
                viewCustomerResponse.setText("User not found.");
                viewCustomerResponse.setVisible(true);
                clearFields();
            }
        });

        fillCustomerTask.setOnFailed(e -> {
            Throwable throwable = fillCustomerTask.getException();
            viewCustomerResponse.setText("Error: " + throwable.getMessage());
            viewCustomerResponse.setVisible(true);
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
}