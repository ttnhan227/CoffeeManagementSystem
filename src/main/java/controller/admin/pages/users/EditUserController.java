package controller.admin.pages.users;

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

        if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                viewCustomerResponse.setText("Passwords do not match!");
                viewCustomerResponse.setVisible(true);
                return;
            }
        }

        if (areCustomerInputsValid(fullname, email, username, phoneNumber, dob, gender, status)) {
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
        } else {
            viewCustomerResponse.setText("Please fill in all fields correctly.");
            viewCustomerResponse.setVisible(true);
        }
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