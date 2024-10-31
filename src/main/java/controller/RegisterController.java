package controller;

import app.utils.HelperMethods;
import app.utils.PasswordUtils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label; // Import Label for error messages
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Datasource;
import model.User;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel; // Declare messageLabel for displaying errors

    Stage dialogStage = new Stage();
    Scene scene;

    public void handleLoginButtonAction(ActionEvent actionEvent) throws IOException {
        Node node = (Node) actionEvent.getSource();
        dialogStage = (Stage) node.getScene().getWindow();
        dialogStage.close();
        scene = new Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml")));
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    public void handleRegisterButtonAction(ActionEvent actionEvent) throws SQLException {
        String validationErrors = "";
        boolean errors = false;
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String providedPassword = passwordField.getText();

        // Validate Full Name
        if (fullName == null || fullName.isEmpty()) {
            validationErrors += "Please enter your Name and Surname!\n";
            errors = true;
        } else if (!HelperMethods.validateFullName(fullName)) {
            validationErrors += "Please enter a valid Name and Surname!\n";
            errors = true;
        }

        // Validate Username
        if (username == null || username.isEmpty()) {
            validationErrors += "Please enter a username!\n";
            errors = true;
        } else if (!HelperMethods.validateUsername(username)) {
            validationErrors += "Please enter a valid Username!\n";
            errors = true;
        } else {
            User userByUsername = Datasource.getInstance().getUserByUsername(username);
            if (userByUsername != null && userByUsername.getUsername() != null) {
                validationErrors += "There is already a user registered with this username!\n";
                errors = true;
            }
        }

        // Validate Email
        if (email == null || email.isEmpty()) {
            validationErrors += "Please enter an email address!\n";
            errors = true;
        } else if (!HelperMethods.validateEmail(email)) {
            validationErrors += "Please enter a valid Email address!\n";
            errors = true;
        } else {
            User userByEmail = Datasource.getInstance().getUserByEmail(email);
            if (userByEmail != null && userByEmail.getEmail() != null) {
                validationErrors += "There is already a user registered with this email address!\n";
                errors = true;
            }
        }

        // Validate Password
        if (providedPassword == null || providedPassword.isEmpty()) {
            validationErrors += "Please enter the password!\n";
            errors = true;
        } else if (!HelperMethods.validatePassword(providedPassword)) {
            validationErrors += "Password must be at least 6 and maximum 16 characters!\n";
            errors = true;
        }

        // Display validation errors or proceed
        if (errors) {
            messageLabel.setText(validationErrors); // Set error message in the label
            return; // Early exit if there are validation errors
        }

        // Proceed with user registration
        String salt = PasswordUtils.getSalt(30);
        String securePassword = PasswordUtils.generateSecurePassword(providedPassword, salt);

        Task<Boolean> addUserTask = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                return Datasource.getInstance().insertNewUser(fullName, username, email, securePassword, salt);
            }
        };

        addUserTask.setOnSucceeded(e -> {
            if (addUserTask.getValue()) {
                User user;
                try {
                    user = Datasource.getInstance().getUserByEmail(email);
                } catch (SQLException err) {
                    err.printStackTrace();
                    return;
                }

                if (user != null) {
                    UserSessionController.setUserId(user.getId());
                    UserSessionController.setUserFullName(user.getFullname());
                    UserSessionController.setUserName(user.getUsername());
                    UserSessionController.setUserEmail(user.getEmail());
                    UserSessionController.setUserAdmin(user.getAdmin());
                    UserSessionController.setUserStatus(user.getStatus());

                    Node node = (Node) actionEvent.getSource();
                    dialogStage = (Stage) node.getScene().getWindow();
                    dialogStage.close();
                    try {
                        scene = new Scene(FXMLLoader.load(getClass().getResource(user.getAdmin() == 0 ? "/view/users/main-dashboard.fxml" : "/view/admin/main-dashboard.fxml")));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    dialogStage.setScene(scene);
                    dialogStage.show();
                }
            } else {
                messageLabel.setText("Registration failed! Please try again.");
            }
        });

        new Thread(addUserTask).start();
    }
}
