package controller.admin.pages.customers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    public TextField fieldCustomersSearch;
    public TableView tableCustomersPage;

    public void btnCustomerSearchOnAction(ActionEvent actionEvent) {
    }



    @FXML
    private void btnAddCustomerOnAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/customers/add-customer.fxml"));
            AnchorPane root = fxmlLoader.load();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnCustomerSearchOnAction(javafx.event.ActionEvent actionEvent) {
    }
}
