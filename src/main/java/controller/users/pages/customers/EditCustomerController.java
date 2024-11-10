package controller.users.pages.customers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
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
            // You'll need to add this method to your Datasource class
            boolean success = Datasource.getInstance().updateCustomer(customerId, name, address, contact);

            if (success) {
                editCustomerResponse.setText("Customer updated successfully");
                editCustomerResponse.setVisible(true);
            } else {
                editCustomerResponse.setText("Failed to update customer");
                editCustomerResponse.setVisible(true);
            }

        } catch (NumberFormatException e) {
            editCustomerResponse.setText("Invalid customer ID");
            editCustomerResponse.setVisible(true);
        }
    }
}
