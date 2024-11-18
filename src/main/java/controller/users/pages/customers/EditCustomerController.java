package controller.users.pages.customers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import model.Customer;
import model.Datasource;

public class EditCustomerController {
    @FXML
    private TextField fieldEditCustomerId;
    @FXML
    private TextField fieldEditCustomerName;
    @FXML
    private TextField fieldEditCustomerAddress;
    @FXML
    private TextField fieldEditCustomerContact;
    @FXML
    private TextField fieldEditCustomerPoints;
    @FXML
    private Text editCustomerResponse;

    @FXML
    public void fillEditingCustomerFields(int customerId) {
        Customer customer = Datasource.getInstance().searchOneCustomerById(customerId);
        if (customer != null) {
            fieldEditCustomerId.setText(String.valueOf(customer.getId()));
            fieldEditCustomerName.setText(customer.getName());
            fieldEditCustomerAddress.setText(customer.getAddress());
            fieldEditCustomerContact.setText(customer.getContact_info());
            fieldEditCustomerPoints.setText(String.valueOf(customer.getPoints()));
        }
    }

    @FXML
    private void btnEditCustomerOnAction() {
        try {
            int customerId = Integer.parseInt(fieldEditCustomerId.getText());
            String name = fieldEditCustomerName.getText();
            String address = fieldEditCustomerAddress.getText();
            String contact = fieldEditCustomerContact.getText();

            // Add validation here if needed
            if (name.isEmpty() || address.isEmpty() || contact.isEmpty()) {
                editCustomerResponse.setText("Please fill in all fields");
                editCustomerResponse.setVisible(true);
                return;
            }

            // Update customer in database
            boolean success = Datasource.getInstance().updateCustomer(customerId, name, address, contact);

            if (success) {
                showModernAlert("Success", "Customer updated successfully!");
                redirectToCustomersList();
            } else {
                editCustomerResponse.setText("Failed to update customer");
                editCustomerResponse.setVisible(true);
            }

        } catch (NumberFormatException e) {
            editCustomerResponse.setText("Invalid customer ID");
            editCustomerResponse.setVisible(true);
        }
    }

    private void showModernAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();

        // Apply styles directly
        String css =
                ".dialog-pane {" +
                        "    -fx-background-color: white;" +
                        "    -fx-padding: 20px;" +
                        "    -fx-border-radius: 5px;" +
                        "    -fx-background-radius: 5px;" +
                        "    -fx-border-color: #e0e0e0;" +
                        "    -fx-border-width: 1px;" +
                        "    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);" +
                        "}" +
                        ".dialog-pane > *.button-bar > *.container {" +
                        "    -fx-background-color: white;" +
                        "}" +
                        ".dialog-pane > *.label.content {" +
                        "    -fx-font-size: 14px;" +
                        "    -fx-padding: 10px 0 15px 0;" +
                        "}" +
                        ".dialog-pane:header *.header-panel {" +
                        "    -fx-background-color: white;" +
                        "}" +
                        ".dialog-pane *.button {" +
                        "    -fx-background-color: #2196F3;" +
                        "    -fx-text-fill: white;" +
                        "    -fx-background-radius: 4px;" +
                        "    -fx-padding: 8px 20px;" +
                        "    -fx-cursor: hand;" +
                        "}" +
                        ".dialog-pane *.button:hover {" +
                        "    -fx-background-color: #1976D2;" +
                        "}" +
                        ".dialog-pane *.button:pressed {" +
                        "    -fx-background-color: #0D47A1;" +
                        "}" +
                        ".dialog-pane > *.graphic-container {" +
                        "    -fx-padding: 0;" +
                        "}" +
                        ".dialog-pane > *.header-panel > *.label {" +
                        "    -fx-font-size: 18px;" +
                        "    -fx-font-weight: bold;" +
                        "}";

        dialogPane.setStyle(css);
        dialogPane.getStyleClass().add("dialog-pane");
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        alert.initStyle(StageStyle.UNDECORATED);

        alert.showAndWait();
    }

    private void redirectToCustomersList() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/users/pages/customers/customers.fxml"));
            AnchorPane root = fxmlLoader.load();

            // Get the current StackPane (customersContent) and update its content
            StackPane customersContent = (StackPane) fieldEditCustomerId.getScene().lookup("#customersContent");
            if (customersContent != null) {
                customersContent.getChildren().clear();
                customersContent.getChildren().add(root);

                // Get the controller and refresh the customers list
                CustomerController customerController = fxmlLoader.getController();
                customerController.listCustomers();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}