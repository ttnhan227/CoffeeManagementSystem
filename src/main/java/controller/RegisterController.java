package controller;

import java.io.IOException;
import java.sql.SQLException;

import app.utils.HelperMethods;
import app.utils.PasswordUtils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Datasource;
import model.User;

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
    private Label messageLabel;

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

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setManaged(true);
        messageLabel.getStyleClass().add("visible");

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    messageLabel.getStyleClass().remove("visible");
                    messageLabel.setManaged(false);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void highlightErrorField(TextField field) {
        field.getStyleClass().add("error");
        field.setOnKeyTyped(e -> {
            field.getStyleClass().remove("error");
            // Clear error message when user starts typing
            messageLabel.getStyleClass().remove("visible");
            messageLabel.setManaged(false);
        });
    }

    public void handleRegisterButtonAction(ActionEvent actionEvent) throws SQLException {
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String providedPassword = passwordField.getText();

        messageLabel.setText("");
        fullNameField.getStyleClass().remove("error");
        usernameField.getStyleClass().remove("error");
        emailField.getStyleClass().remove("error");
        passwordField.getStyleClass().remove("error");

        // Validate Full Name
        if (fullName.isEmpty() || !HelperMethods.validateFullName(fullName)) {
            showError("Full name must contain only letters, start with an uppercase letter, and be 4+ characters.");
            highlightErrorField(fullNameField);
            return;
        }

        // Validate Username
        if (username.isEmpty() || !HelperMethods.validateUsername(username)) {
            showError("Username must be 5-30 characters, start with a letter, and contain only letters, numbers, or underscores.");
            highlightErrorField(usernameField);
            return;
        }

        // Check username availability
        User userByUsername = Datasource.getInstance().getUserByUsername(username);
        if (userByUsername != null && userByUsername.getUsername() != null) {
            showError("Username is already taken.");
            highlightErrorField(usernameField);
            return;
        }

        // Validate Email
        if (email.isEmpty() || !HelperMethods.validateEmail(email)) {
            showError("Enter a valid email address (e.g., user@example.com).");
            highlightErrorField(emailField);
            return;
        }

        // Check email availability
        User userByEmail = Datasource.getInstance().getUserByEmail(email);
        if (userByEmail != null && userByEmail.getEmail() != null) {
            showError("Email is already registered.");
            highlightErrorField(emailField);
            return;
        }

        // Validate Password
        if (providedPassword.isEmpty() || !HelperMethods.validatePassword(providedPassword)) {
            showError("Password must be 6-16 characters long.");
            highlightErrorField(passwordField);
            return;
        }

        String salt = PasswordUtils.getSalt(30);
        String securePassword = PasswordUtils.generateSecurePassword(providedPassword, salt);

        Task<Boolean> addUserTask = new Task<>() {
            @Override
            protected Boolean call() {
                return Datasource.getInstance().insertNewUser(fullName, username, email, securePassword, salt);
            }
        };

        addUserTask.setOnSucceeded(e -> {
            if (addUserTask.getValue()) {
                try {
                    // Show success message with username and email
                    HelperMethods.alertBox("Registration Successful",
                            "Welcome!\nYour username: " + username + "\nYour email: " + email, "Insert data");

                    // Redirect to login page
                    Node node = (Node) actionEvent.getSource();
                    dialogStage = (Stage) node.getScene().getWindow();
                    dialogStage.close();

                    // Load the login page and set it to the stage
                    scene = new Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml")));
                    dialogStage.setScene(scene);
                    dialogStage.show();

                } catch (IOException ex) {
                    showError("An error occurred while redirecting. Please try again.");
                    ex.printStackTrace();
                }
            } else {
                showError("Registration failed. Please try again.");
            }
        });

        new Thread(addUserTask).start();
    }

}