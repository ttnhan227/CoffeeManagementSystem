package controller.admin.pages.customers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import model.User;
import model.Datasource;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class CustomerController {
    @FXML
    private StackPane customersContent;
    public TextField fieldCustomersSearch;
    public TableView tableCustomersPage;

    public void btnCustomerSearchOnAction(ActionEvent actionEvent) {
    }

    public void btnCustomerSearchOnAction(javafx.event.ActionEvent actionEvent) {
    }

    @FXML
    private void btnAddCustomerOnAction(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin/pages/customers/add-customer.fxml"));
            Parent addCustomerPage = loader.load();
            // Assuming customersContent is the StackPane in your FXML
            customersContent.getChildren().clear();
            customersContent.getChildren().add(addCustomerPage);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately
        }
    }
}
