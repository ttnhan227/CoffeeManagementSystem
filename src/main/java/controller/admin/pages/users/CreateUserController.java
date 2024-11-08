package controller.admin.pages.users;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.Datasource;  // Assuming UserModel is a class where your insert function is defined
import app.utils.PasswordUtils; // Assuming PasswordUtils contains your password hashing methods

public class CreateUserController {

    @FXML
    private TextField fieldCreateUserName;

    @FXML
    private TextField fieldCreateUserEmail;

    @FXML
    private TextField fieldCreateUserUsername;

    @FXML
    private PasswordField fieldCreateUserPassword;

    @FXML
    private ComboBox<String> fieldCreateUserStatus;

    @FXML
    private Text viewCreateUserResponse;

    public void initializeForm() {
        // Initialize combo box or other fields here
        fieldCreateUserStatus.getItems().addAll("Active", "Inactive");
    }


    @FXML
    public void btnCreateUserOnAction() {
        String fullName = fieldCreateUserName.getText();
        String email = fieldCreateUserEmail.getText();
        String username = fieldCreateUserUsername.getText();
        String password = fieldCreateUserPassword.getText();

        // Validate the input
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            viewCreateUserResponse.setText("All fields are required.");
            viewCreateUserResponse.setVisible(true);
            return;
        }

        // Generate salt and hash the password
        String salt = PasswordUtils.getSalt(30); // Generating a salt (length 30)
        String securePassword = PasswordUtils.generateSecurePassword(password, salt); // Hash the password

        // Use the UserModel to insert the new user into the database
        Datasource userModel = Datasource.getInstance();  // Singleton pattern to get the instance
        boolean success = userModel.insertNewUserForm(fullName, username, email, securePassword, salt);

        if (success) {
            viewCreateUserResponse.setText("User created successfully!");
            viewCreateUserResponse.setFill(javafx.scene.paint.Color.GREEN);
        } else {
            viewCreateUserResponse.setText("Failed to create user.");
            viewCreateUserResponse.setFill(javafx.scene.paint.Color.RED);
        }

        viewCreateUserResponse.setVisible(true);
    }
}
