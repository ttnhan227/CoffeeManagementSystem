package controller.admin.pages.users;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.Datasource;
import app.utils.PasswordUtils;
import java.time.LocalDate;

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

    public void initializeForm() {
        // Initialize combo boxes
        fieldCreateUserStatus.getItems().addAll("Active", "Inactive");
        fieldCreateUserGender.getItems().addAll("Male", "Female", "Other");

        // Set default values
        fieldCreateUserStatus.setValue("Active");
        fieldCreateUserDOB.setValue(LocalDate.now());
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

        // Validate the input
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() ||
                password.isEmpty() || dob == null || gender == null || phone.isEmpty()) {
            viewCreateUserResponse.setText("All fields are required.");
            viewCreateUserResponse.setVisible(true);
            return;
        }

        // Generate salt and hash the password
        String salt = PasswordUtils.getSalt(30);
        String securePassword = PasswordUtils.generateSecurePassword(password, salt);

        // Use the UserModel to insert the new user into the database
        Datasource userModel = Datasource.getInstance();
        boolean success = userModel.insertNewUserForm(
                fullName, username, email, securePassword, salt,
                java.sql.Date.valueOf(dob), gender, phone,
                status.toLowerCase()
        );

        if (success) {
            viewCreateUserResponse.setText("User created successfully!");
            viewCreateUserResponse.setFill(javafx.scene.paint.Color.GREEN);
            clearForm();
        } else {
            viewCreateUserResponse.setText("Failed to create user.");
            viewCreateUserResponse.setFill(javafx.scene.paint.Color.RED);
        }

        viewCreateUserResponse.setVisible(true);
    }

    private void clearForm() {
        fieldCreateUserName.clear();
        fieldCreateUserEmail.clear();
        fieldCreateUserUsername.clear();
        fieldCreateUserPassword.clear();
        fieldCreateUserDOB.setValue(LocalDate.now());
        fieldCreateUserGender.setValue(null);
        fieldCreateUserPhone.clear();
        fieldCreateUserStatus.setValue("Active");
    }
}