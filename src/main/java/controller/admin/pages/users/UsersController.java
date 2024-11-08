package controller.admin.pages.users;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.User;
import model.Datasource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    public void initialize() {
        // Get the date column
        tableCustomersPage.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, Date> dateColumn = (TableColumn<User, Date>) tableCustomersPage.getColumns().get(3);

        // Set a custom cell factory to format the date
        dateColumn.setCellFactory(column -> new TableCell<User, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

    }
    @FXML
    public void listUsers() {
        Task<ObservableList<User>> getAllCustomersTask = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                System.out.println("Fetching users from database..."); // Debug print
                ObservableList<User> users = FXCollections.observableArrayList(
                        Datasource.getInstance().getAllUsers(Datasource.ORDER_BY_NONE)
                );
                System.out.println("Found " + users.size() + " users"); // Debug print
                return users;
            }
        };

        getAllCustomersTask.setOnSucceeded(e -> {
            System.out.println("Task succeeded, updating table..."); // Debug adprint
            tableCustomersPage.setItems(getAllCustomersTask.getValue());
            addActionButtonsToTable();
        });

        getAllCustomersTask.setOnFailed(e -> {
            System.out.println("Task failed: " + getAllCustomersTask.getException()); // Debug print
            getAllCustomersTask.getException().printStackTrace();
        });

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
                            btnEditUser(userData.getId());
                        });

                        deleteButton.setOnAction(event -> {
                            User userData = getTableView().getItems().get(getIndex());
                            deleteUser(userData);
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

    private void deleteUser(User userData) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure that you want to delete " + userData.getFullname() + "?");
        alert.setTitle("Delete " + userData.getFullname() + "?");
        Optional<ButtonType> deleteConfirmation = alert.showAndWait();

        if (deleteConfirmation.isPresent() && deleteConfirmation.get() == ButtonType.OK) {
            if (Datasource.getInstance().deleteSingleUser(userData.getId())) {
                tableCustomersPage.getItems().remove(userData);
            }
        }
    }

    @FXML
    public void btnUsersSearchOnAction() {
        Task<ObservableList<User>> searchCustomersTask = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().searchUsers(fieldCustomersSearch.getText().toLowerCase(), Datasource.ORDER_BY_NONE));
            }
        };

        tableCustomersPage.itemsProperty().bind(searchCustomersTask.valueProperty());
        new Thread(searchCustomersTask).start();
    }

    @FXML
    private void btnEditUser(int customerId) {
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
    @FXML
    private void btnAddUserOnAction() {
        try {
            // Load the Create User form (FXML) into the existing layout
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/users/add-user.fxml"));
            AnchorPane root = fxmlLoader.load();

            // Clear current content and add the new "Create User" form
            customersContent.getChildren().clear();
            customersContent.getChildren().add(root);

            // Get the controller and initialize if needed
            CreateUserController createUserController = fxmlLoader.getController();
            createUserController.initializeForm(); // Add a method in CreateUserController to initialize the form if needed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
