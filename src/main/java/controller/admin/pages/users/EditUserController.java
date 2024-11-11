package controller.admin.pages.users;

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
    private void initialize() {
        // Populate the status combo box with predefined statuses
        fieldEditCustomerStatus.setItems(FXCollections.observableArrayList("Active", "Inactive"));

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

        if (areCustomerInputsValid(fullname, email, username, phoneNumber, dob, gender, status)) {
            Task<Boolean> updateCustomerTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    java.sql.Date sqlDateOfBirth = java.sql.Date.valueOf(dob);  // Convert LocalDate directly to java.sql.Date
                    return Datasource.getInstance().updateOneUser(
                            customerId,
                            fullname,
                            username,
                            email,
                            status,
                            sqlDateOfBirth,
                            phoneNumber,
                            gender
                    );
                }
            };

            updateCustomerTask.setOnSucceeded(e -> {
                if (updateCustomerTask.valueProperty().get()) {
                    viewCustomerResponse.setText("User updated successfully!");
                    viewCustomerResponse.setVisible(true);
                } else {
                    viewCustomerResponse.setText("Failed to update customer.");
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

                // Set the ID field
                fieldEditCustomerId.setText(String.valueOf(user.getId()));

                // Set text fields, handling potential nulls
                fieldEditCustomerName.setText(user.getFullname() != null ? user.getFullname() : "");
                fieldEditCustomerEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                fieldEditCustomerUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                fieldEditCustomerPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

                // Handle DatePicker field
                if (user.getDateOfBirth() != null) {
                    fieldEditCustomerDOB.setValue(user.getDateOfBirth().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
                } else {
                    fieldEditCustomerDOB.setValue(null);
                }

                // Handle Gender ComboBox
                fieldEditCustomerGender.setValue(user.getGender());  // ComboBox can handle null values

                // Handle Status ComboBox
                fieldEditCustomerStatus.setValue(user.getStatus() != null ? user.getStatus() : "");

                // Reset any previous error messages
                viewCustomerResponse.setVisible(false);

            } else {
                // Handle case where user is not found
                viewCustomerResponse.setText("User not found.");
                viewCustomerResponse.setVisible(true);

                // Clear all fields
                clearFields();
            }
        });

        fillCustomerTask.setOnFailed(e -> {
            Throwable throwable = fillCustomerTask.getException();
            viewCustomerResponse.setText("Error: " + throwable.getMessage());
            viewCustomerResponse.setVisible(true);

            // Clear all fields on error
            clearFields();
        });

        new Thread(fillCustomerTask).start();
    }

    // Helper method to clear all fields
    private void clearFields() {
        fieldEditCustomerId.setText("");
        fieldEditCustomerName.setText("");
        fieldEditCustomerEmail.setText("");
        fieldEditCustomerUsername.setText("");
        fieldEditCustomerPhone.setText("");
        fieldEditCustomerDOB.setValue(null);
        fieldEditCustomerGender.setValue(null);
        fieldEditCustomerStatus.setValue(null);
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