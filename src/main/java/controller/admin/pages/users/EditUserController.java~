package controller.admin.pages.users;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.User;
import model.Datasource;
import javafx.scene.text.Text;

import java.util.List;

public class EditUserController {

    @FXML
    private TextField fieldEditCustomerName;
    @FXML
    private TextField fieldEditCustomerEmail;
    @FXML
    private TextField fieldEditCustomerUsername;
    @FXML
    private ComboBox<String> fieldEditCustomerStatus;
    @FXML
    private Text viewCustomerResponse;
    @FXML
    private TextField fieldEditCustomerId;

    @FXML
    private void initialize() {
        // Populate the status combo box with predefined statuses
        fieldEditCustomerStatus.setItems(FXCollections.observableArrayList("Active", "Inactive", "Banned"));
    }

    @FXML
    private void btnEditCustomerOnAction() {
        int customerId = Integer.parseInt(fieldEditCustomerId.getText());
        String fullname = fieldEditCustomerName.getText();
        String email = fieldEditCustomerEmail.getText();
        String username = fieldEditCustomerUsername.getText();
        String status = fieldEditCustomerStatus.getValue();

        if (areCustomerInputsValid(fullname, email, username, status)) {
            Task<Boolean> updateCustomerTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return Datasource.getInstance().updateOneUser(customerId, fullname, username, email, status);
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
                return Datasource.getInstance().getOneUser(customer_id); // Returns a list of customers
            }
        };

        fillCustomerTask.setOnSucceeded(e -> {
            List<User> users = fillCustomerTask.getValue();
            if (users != null && !users.isEmpty()) { // Check if the list is not null and not empty
                User user = users.get(0); // Get the first user from the list
                fieldEditCustomerId.setText(String.valueOf(user.getId()));
                fieldEditCustomerName.setText(user.getFullname());
                fieldEditCustomerEmail.setText(user.getEmail());
                fieldEditCustomerUsername.setText(user.getUsername());
                fieldEditCustomerStatus.setValue(user.getStatus());
            } else {
                viewCustomerResponse.setText("User not found.");
                viewCustomerResponse.setVisible(true);
            }
        });

        fillCustomerTask.setOnFailed(e -> {
            Throwable throwable = fillCustomerTask.getException();
            viewCustomerResponse.setText("Error: " + throwable.getMessage());
            viewCustomerResponse.setVisible(true);
        });

        new Thread(fillCustomerTask).start();
    }

    /**
     * Validates customer inputs.
     */
    private boolean areCustomerInputsValid(String fullname, String email, String username, String status) {
        // Implement validation logic (e.g., check for nulls, email format, etc.)
        return !fullname.isEmpty() && !email.isEmpty() && !username.isEmpty() && (status != null);
    }
}
