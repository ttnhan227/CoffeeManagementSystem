package controller.admin.pages.customers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import model.Datasource;

public class AddCustomerController {
    @FXML
    private TextField fieldCreateCustomerName;
    @FXML
    private TextField fieldCreateCustomerAddress;
    @FXML
    private TextField fieldCreateCustomerContact;
    @FXML
    private Text viewCreateCustomerResponse;

    @FXML
    public void btnCreateCustomerOnAction() {
        String name = fieldCreateCustomerName.getText();
        String address = fieldCreateCustomerAddress.getText();
        String contact = fieldCreateCustomerContact.getText();

        // Validate the input
        if (name.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            viewCreateCustomerResponse.setText("All fields are required.");
            viewCreateCustomerResponse.setVisible(true);
            return;
        }

        // Use the Datasource to insert the new customer into the database
        Datasource datasource = Datasource.getInstance();
        boolean success = datasource.insertNewCustomer(name, address, contact);

        if (success) {
            viewCreateCustomerResponse.setText("Customer created successfully!");
            viewCreateCustomerResponse.setFill(javafx.scene.paint.Color.GREEN);
            clearForm();
        } else {
            viewCreateCustomerResponse.setText("Failed to create customer.");
            viewCreateCustomerResponse.setFill(javafx.scene.paint.Color.RED);
        }

        viewCreateCustomerResponse.setVisible(true);
    }

    private void clearForm() {
        fieldCreateCustomerName.clear();
        fieldCreateCustomerAddress.clear();
        fieldCreateCustomerContact.clear();
    }
}
