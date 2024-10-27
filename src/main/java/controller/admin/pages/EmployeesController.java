package controller.admin.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import model.Employee;
import model.Datasource;

import java.io.IOException;
import java.util.Optional;
public class EmployeesController {

    @FXML
    public TextField fieldEmployeesSearch;
    @FXML
    private StackPane employeesContent;
    @FXML
    private TableView<Employee> tableCustomersPage;
    @FXML
    public void listEmployees() {

        Task<ObservableList<Employee>> getAllCustomersTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return FXCollections.observableArrayList(Datasource.getInstance().getAllEmployees(Datasource.ORDER_BY_NONE));
            }
        };

        tableCustomersPage.itemsProperty().bind(getAllCustomersTask.valueProperty());
        addActionButtonsToTable();
        new Thread(getAllCustomersTask).start();

    }

    @FXML
    private void addActionButtonsToTable() {
        TableColumn colBtnEdit = new TableColumn("Actions");

        Callback<TableColumn<Employee, Void>, TableCell<Employee, Void>> cellFactory = new Callback<TableColumn<Employee, Void>, TableCell<Employee, Void>>() {
            @Override
            public TableCell<Employee, Void> call(final TableColumn<Employee, Void> param) {
                return new TableCell<Employee, Void>() {

                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttonsPane = new HBox();
//
//                    {
//                        viewButton.getStyleClass().add("button");
//                        viewButton.getStyleClass().add("xs");
//                        viewButton.getStyleClass().add("info");
//                        viewButton.setOnAction((ActionEvent event) -> {
//                            Employee employeeData = getTableView().getItems().get(getIndex());
////                            btnViewCustomer((int) employeeData.getId());
//                            System.out.println("View Employee");
//                            System.out.println("employee id: " + employeeData.getId());
//                            System.out.println("employee name: " + employeeData.getFullname());
//                        });
//                    }

                    {
                        editButton.getStyleClass().add("button");
                        editButton.getStyleClass().add("xs");
                        editButton.getStyleClass().add("primary");
                        editButton.setOnAction((ActionEvent event) -> {
                            Employee employeeData = getTableView().getItems().get(getIndex());
                            btnEditCustomer((int) employeeData.getId());
                            System.out.println("Edit Employee");
                            System.out.println("employee id: " + employeeData.getId());
                            System.out.println("employee name: " + employeeData.getFullname());
                        });
                    }

                    {
                        deleteButton.getStyleClass().add("button");
                        deleteButton.getStyleClass().add("xs");
                        deleteButton.getStyleClass().add("danger");
                        deleteButton.setOnAction((ActionEvent event) -> {
                            Employee employeeData = getTableView().getItems().get(getIndex());

                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setHeaderText("Are you sure that you want to delete " + employeeData.getFullname() + " ?");
                            alert.setTitle("Delete " + employeeData.getFullname() + " ?");
                            Optional<ButtonType> deleteConfirmation = alert.showAndWait();

                            if (deleteConfirmation.get() == ButtonType.OK) {
                                System.out.println("Delete Employee");
                                System.out.println("employee id: " + employeeData.getId());
                                System.out.println("empoyee name: " + employeeData.getFullname());
                                if (Datasource.getInstance().deleteSingleCustomer(employeeData.getId())) {
                                    getTableView().getItems().remove(getIndex());
                                }
                            }
                        });
                    }

                    {
                        buttonsPane.setSpacing(10);
//                        buttonsPane.getChildren().add(viewButton);
//                        buttonsPane.getChildren().add(editButton);
                        buttonsPane.getChildren().add(deleteButton);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonsPane);
                        }
                    }
                };
            }
        };

        colBtnEdit.setCellFactory(cellFactory);

        tableCustomersPage.getColumns().add(colBtnEdit);

    }

    /**
     * This private method handles the customers search functionality.
     * It creates a new task, gets the search results from the database and binds them to the view table.
     *
     * @since 1.0.0
     */
    public void btnCustomersSearchOnAction() {
        Task<ObservableList<Employee>> searchCustomersTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().searchCustomers(fieldEmployeesSearch.getText().toLowerCase(), Datasource.ORDER_BY_NONE));
            }
        };
        tableCustomersPage.itemsProperty().bind(searchCustomersTask.valueProperty());

        new Thread(searchCustomersTask).start();
    }
    @FXML
    private void btnEditCustomer(int customer_id) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.load(getClass().getResource("/view/admin/pages/employees/edit-employee.fxml").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        AnchorPane root = fxmlLoader.getRoot();
        employeesContent.getChildren().clear();
        employeesContent.getChildren().add(root);

        fillEditCustomer(customer_id);

    }
//    @FXML
//    private void btnViewCustomer(int customer_id) {
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        try {
//            fxmlLoader.load(getClass().getResource("/view/admin/pages/employees/view-customer.fxml").openStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        AnchorPane root = fxmlLoader.getRoot();
//        employeesContent.getChildren().clear();
//        employeesContent.getChildren().add(root);
//
//        fillEditCustomer(customer_id);
//
//    }
    @FXML
    private void fillEditCustomer(int customer_id) {

        Task<ObservableList<Employee>> fillCustomerTask = new Task<ObservableList<Employee>>() {
            @Override
            protected ObservableList<Employee> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().getOneCustomer(customer_id));
            }
        };
        fillCustomerTask.setOnSucceeded(e -> {
//            fieldAddCustomerNameEdit.setText("test");
            System.out.println("pr name:" + fillCustomerTask.valueProperty().getValue().get(0).getFullname());
            // TODO
            //  fieldAddCustomerName.setText("test");
            //  fieldAddCustomerName.setText(fillCustomerTask.valueProperty().getValue().get(0).getName());
        });

        new Thread(fillCustomerTask).start();
    }

}
