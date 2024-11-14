package controller;

import java.io.IOException;
import java.sql.SQLException;

import app.utils.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;  // Ensure this line is present
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

public class LoginController {
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label messageLabel;  // Declare the messageLabel here
    Stage dialogStage = new Stage();
    Scene scene;

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setManaged(true);
        messageLabel.getStyleClass().add("visible");
        
        // Add error style to fields
        if (usernameField.getText().isEmpty()) {
            usernameField.getStyleClass().add("error");
        }
        if (passwordField.getText().isEmpty()) {
            passwordField.getStyleClass().add("error");
        }
        
        // Remove error styles after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    messageLabel.getStyleClass().remove("visible");
                    messageLabel.setManaged(false);
                    usernameField.getStyleClass().remove("error");
                    passwordField.getStyleClass().remove("error");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void handleLoginButtonAction(ActionEvent event) throws SQLException, IOException {
        String username = usernameField.getText();
        String providedPassword = passwordField.getText();
        messageLabel.setText("");

        if (username.isEmpty() || providedPassword.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        User user = model.Datasource.getInstance().getUserByUsername(username);
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            showError("No user found with this username");
            return;
        }

        if (user.getStatus() != null && user.getStatus().equalsIgnoreCase("disabled")) {
            showError("Your account has been disabled. Please contact administrator.");
            return;
        }

        boolean passwordMatch = PasswordUtils.verifyUserPassword(providedPassword, user.getPassword(), user.getSalt());
        if (!passwordMatch) {
            showError("Incorrect username or password");
            return;
        }

        // Login successful - proceed with existing code
        UserSessionController.setUserId(user.getId());
        UserSessionController.setUserFullName(user.getFullname());
        UserSessionController.setUserName(user.getUsername());
        UserSessionController.setUserEmail(user.getEmail());
        UserSessionController.setUserAdmin(user.getAdmin());
        UserSessionController.setUserStatus(user.getStatus());

        Node node = (Node) event.getSource();
        dialogStage = (Stage) node.getScene().getWindow();
        dialogStage.close();

        // Load appropriate dashboard based on user role
        if (user.getAdmin() == 0) {
            scene = new Scene(FXMLLoader.load(getClass().getResource("/view/users/main-dashboard.fxml")));
        } else {
            scene = new Scene(FXMLLoader.load(getClass().getResource("/view/admin/main-dashboard.fxml")));
        }
        
        // Set up the stage for full screen
        dialogStage.setScene(scene);
        dialogStage.setMaximized(true);  // This will make the window maximized
        dialogStage.setFullScreen(true); // This will make it truly full screen
        dialogStage.show();
    }

    public void handleRegisterButtonAction(ActionEvent actionEvent) throws IOException {
        Stage dialogStage;
        Node node = (Node) actionEvent.getSource();
        dialogStage = (Stage) node.getScene().getWindow();
        dialogStage.close();
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/register.fxml")));
        dialogStage.setScene(scene);
        dialogStage.show();
    }
}
