package controller.admin.pages.customers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import model.Customer;
import model.Datasource;

import java.io.IOException;
import java.util.Optional;

public class CustomerController {
    @FXML
    private StackPane customersContent;
    @FXML
    public TextField fieldCustomersSearch;
    @FXML
    public TableView<Customer> tableCustomersPage;

    @FXML
    public void initialize() {
        tableCustomersPage.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Create and add the Actions column
        TableColumn<Customer, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(150);

        actionsColumn.setCellFactory(new Callback<TableColumn<Customer, Void>, TableCell<Customer, Void>>() {
            @Override
            public TableCell<Customer, Void> call(final TableColumn<Customer, Void> param) {
                return new TableCell<Customer, Void>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttonsPane = new HBox(editButton, deleteButton);

                    {
                        buttonsPane.setSpacing(10);
                        editButton.getStyleClass().addAll("button", "xs", "primary");
                        deleteButton.getStyleClass().addAll("button", "xs", "danger");

                        editButton.setOnAction(event -> {
                            Customer customerData = getTableView().getItems().get(getIndex());
                            btnEditCustomer(customerData.getId());
                        });

                        deleteButton.setOnAction(event -> {
                            Customer customerData = getTableView().getItems().get(getIndex());
                            deleteCustomer(customerData);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttonsPane);
                    }
                };
            }
        });

        tableCustomersPage.getColumns().add(actionsColumn);
        listCustomers();
    }

    @FXML
    public void listCustomers() {
        Task<ObservableList<Customer>> getAllCustomersTask = new Task<ObservableList<Customer>>() {
            @Override
            protected ObservableList<Customer> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().getAllCustomers(Datasource.ORDER_BY_NONE)
                );
            }
        };

        getAllCustomersTask.setOnSucceeded(e -> tableCustomersPage.setItems(getAllCustomersTask.getValue()));
        new Thread(getAllCustomersTask).start();
    }

    private void deleteCustomer(Customer customerData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure you want to delete " + customerData.getName() + "?");
        alert.setTitle("Delete " + customerData.getName() + "?");
        Optional<ButtonType> deleteConfirmation = alert.showAndWait();

        if (deleteConfirmation.isPresent() && deleteConfirmation.get() == ButtonType.OK) {
            if (Datasource.getInstance().deleteSingleCustomer(customerData.getId())) {
                tableCustomersPage.getItems().remove(customerData);
            }
        }
    }

    @FXML
    public void btnCustomerSearchOnAction() {
        Task<ObservableList<Customer>> searchCustomersTask = new Task<ObservableList<Customer>>() {
            @Override
            protected ObservableList<Customer> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().searchCustomers(fieldCustomersSearch.getText().toLowerCase(), Datasource.ORDER_BY_NONE));
            }
        };

        tableCustomersPage.itemsProperty().bind(searchCustomersTask.valueProperty());
        new Thread(searchCustomersTask).start();
    }

    @FXML
    private void btnEditCustomer(int customerId) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/customers/edit-customer.fxml"));
            Parent root = fxmlLoader.load();
            customersContent.getChildren().clear();
            customersContent.getChildren().add(root);

            EditCustomerController editController = fxmlLoader.getController();
            editController.fillEditingCustomerFields(customerId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnAddCustomerOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin/pages/customers/add-customer.fxml"));
            Parent addCustomerPage = loader.load();
            customersContent.getChildren().clear();
            customersContent.getChildren().add(addCustomerPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
