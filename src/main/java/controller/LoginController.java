package controller;

import app.utils.HelperMethods;
import app.utils.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;  // Ensure this line is present
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label messageLabel;  // Declare the messageLabel here
    Stage dialogStage = new Stage();
    Scene scene;

    public void handleLoginButtonAction(ActionEvent event) throws SQLException, IOException {
        String username = usernameField.getText();
        String providedPassword = passwordField.getText();
        messageLabel.setText(""); // Clear previous messages

        if (username.isEmpty() || providedPassword.isEmpty()) {
            messageLabel.setText("Please enter the Username and Password.");
        } else if (!HelperMethods.validateUsername(username)) {
            messageLabel.setText("Please enter a valid Username!");
        } else {
            User user = model.Datasource.getInstance().getUserByUsername(username);
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                messageLabel.setText("There is no user registered with that username!");
            } else {
                boolean passwordMatch = PasswordUtils.verifyUserPassword(providedPassword, user.getPassword(), user.getSalt());

                if (passwordMatch) {
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
                    dialogStage.setScene(scene);
                    dialogStage.show();
                } else {
                    messageLabel.setText("Incorrect username or password.");
                }
            }
        }
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
