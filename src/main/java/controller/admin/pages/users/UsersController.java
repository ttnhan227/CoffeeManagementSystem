package controller.admin.pages.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import model.User;
import model.Datasource;

import java.io.IOException;
import java.util.Optional;

/**
 * This class handles the admin customers page.
 */
public class UsersController {

    @FXML
    public TextField fieldCustomersSearch;
    @FXML
    private StackPane customersContent;
    @FXML
    private TableView<User> tableCustomersPage;

    @FXML
    public void listCustomers() {
        Task<ObservableList<User>> getAllCustomersTask = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                return FXCollections.observableArrayList(Datasource.getInstance().getAllCustomers(Datasource.ORDER_BY_NONE));
            }
        };

        tableCustomersPage.itemsProperty().bind(getAllCustomersTask.valueProperty());
        addActionButtonsToTable();
        new Thread(getAllCustomersTask).start();
    }

    @FXML
    private void addActionButtonsToTable() {
        TableColumn<User, Void> colBtnEdit = new TableColumn<>("Actions");

        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttonsPane = new HBox(editButton, deleteButton);

                    {
                        buttonsPane.setSpacing(10);
                        editButton.getStyleClass().addAll("button", "xs", "primary");
                        deleteButton.getStyleClass().addAll("button", "xs", "danger");

                        editButton.setOnAction(event -> {
                            User userData = getTableView().getItems().get(getIndex());
                            btnEditCustomer(userData.getId());
                        });

                        deleteButton.setOnAction(event -> {
                            User userData = getTableView().getItems().get(getIndex());
                            deleteCustomer(userData);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttonsPane);
                    }
                };
            }
        };

        colBtnEdit.setCellFactory(cellFactory);
        tableCustomersPage.getColumns().add(colBtnEdit);
    }

    private void deleteCustomer(User userData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure that you want to delete " + userData.getFullname() + "?");
        alert.setTitle("Delete " + userData.getFullname() + "?");
        Optional<ButtonType> deleteConfirmation = alert.showAndWait();

        if (deleteConfirmation.isPresent() && deleteConfirmation.get() == ButtonType.OK) {
            if (Datasource.getInstance().deleteSingleCustomer(userData.getId())) {
                tableCustomersPage.getItems().remove(userData);
            }
        }
    }

    @FXML
    public void btnCustomersSearchOnAction() {
        Task<ObservableList<User>> searchCustomersTask = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/users/edit-user.fxml"));
            AnchorPane root = fxmlLoader.load();
            customersContent.getChildren().clear();
            customersContent.getChildren().add(root);

            EditUserController editController = fxmlLoader.getController();
            editController.fillEditingCustomerFields(customerId); // Call the method to load customer data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // If you have a method to load customer details for viewing
    // You should implement a similar method in ViewCustomerController to handle this
}
