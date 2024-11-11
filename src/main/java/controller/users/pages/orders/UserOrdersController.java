package controller.users.pages.orders;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import controller.users.UserMainDashboardController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Customer;
import model.Datasource;
import model.Order;
import model.User;

public class UserOrdersController implements Initializable {
    public TableView<Order> tableOrdersPage;
    public TableColumn<Order, Integer> idColumn;
    public TableColumn<Order, Double> paidColumn;
    public TableColumn<Order, String> dateColumn;
    public TableColumn<Order, String> employeeColumn;
    public TableColumn<Order, Integer> couponColumn;
    public TableColumn<Order, Integer> tableColumn;
    public TableColumn<Order, String> customerColumn;
    public TextField searchField;
    public ComboBox<String> searchComboBox;

    private ObservableList<Order> orderList;
    private ObservableList<Order> filteredList;

    public UserMainDashboardController mainDashboardController;

    @FXML
    public void listOrders() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        paidColumn.setCellValueFactory(new PropertyValueFactory<>("fin"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("order_date"));
        couponColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("tableID"));

        employeeColumn.setCellValueFactory(cellData -> {
            int index = tableOrdersPage.getItems().indexOf(cellData.getValue());
            Integer emid = filteredList.get(index).getEmployeeID();
            if (emid != null) {
                // Fetch the employee and check for null
                User employee = Datasource.getInstance().searchOneEmployeeById(emid);
                if (employee != null) {
                    return new SimpleStringProperty(employee.getFullname());
                }
            }
            // Return an empty string or a default message if the employee is not found
            return new SimpleStringProperty("Unknown Employee");
        });

        customerColumn.setCellValueFactory(cellData -> {
            int index = tableOrdersPage.getItems().indexOf(cellData.getValue());
            Integer cusid = filteredList.get(index).getCustomerID();
            if (cusid == null) {
                return new SimpleStringProperty(""); // Default value when the customer ID is null
            } else {
                Customer customer = Datasource.getInstance().searchOneCustomerById(cusid);
                if (customer != null) {
                    return new SimpleStringProperty(customer.getName());
                } else {
                    return new SimpleStringProperty(""); // Return an empty string or default if customer is not found
                }
            }
        });

        addActionButton();
        tableOrdersPage.setItems(filteredList);
    }


    @FXML
    private void addActionButton(){
        TableColumn<Order, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button viewButton = new Button("View");
            private final Button deleteButton = new Button("Delete");
            private final HBox hbox = new HBox(5); // 5 is spacing between buttons

            {
                // Set the action for the View button
                viewButton.setOnAction(e -> {
                    try {
                        mainDashboardController.viewOrderDetail(new ActionEvent(), getTableRow().getItem());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                // Set the action for the Delete button
                deleteButton.setOnAction(e -> {
                    Order order = getTableRow().getItem();
                    if (order != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete Order");
                        alert.setHeaderText("Delete Order #" + order.getId());
                        alert.setContentText("Are you sure you want to delete this order?");

                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                if (Datasource.getInstance().deleteOrder(order.getId())) {
                                    orderList.remove(order);
                                    filteredList.remove(order);
                                } else {
                                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                    errorAlert.setTitle("Error");
                                    errorAlert.setHeaderText("Delete Failed");
                                    errorAlert.setContentText("Failed to delete the order.");
                                    errorAlert.show();
                                }
                            }
                        });
                    }
                });

                hbox.getChildren().addAll(viewButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        actionColumn.setMinWidth(100);
        actionColumn.setPrefWidth(150);
        actionColumn.setMaxWidth(5000);
        tableOrdersPage.getColumns().add(actionColumn);
    }

    public void btnOrdersSearchOnAction(ActionEvent actionEvent) {
        // TODO
        //  Add orders search functionality.
        System.out.println("TODO: Add orders search functionality.");
    }

    private void viewClick(){

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        orderList = FXCollections.observableArrayList(Datasource.getInstance().getAllOrder());
        filteredList = FXCollections.observableArrayList(orderList);
        listOrders();
        loadSearch();
    }

    public void setMainDashboardController(UserMainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void loadSearch(){
        searchComboBox.getItems().addAll("All", "By id", "By employee name", "By customer name", "By date", "By table", "By coupon id");

        searchComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            searchField.setText("");
            searchField.setDisable(newValue.equals("All"));
        });
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            applyFilter(searchComboBox.getValue(), newText);
            if(newText == null){
                applyFilter("All", newText);
            }
            searchField.setDisable(searchComboBox.getValue().equals("All"));
        });
        searchComboBox.setValue("All");
    }

    private void applyFilter(String filterOption, String search) {
        filteredList.clear();
        if(search.isEmpty()){
            filteredList.addAll(orderList);
            return;
        }
        switch (filterOption) {
            case "All":
                filteredList.addAll(orderList);
                break;
            case "By id":
                filteredList.addAll(orderList.filtered(order -> String.valueOf(order.getId()).contains(search)));
                break;
            case "By employee name":
                filteredList.addAll(orderList.filtered(order -> {
                    if(order.getEmployeeID() == null){
                        return true;
                    }
                    //boolean check =
                    String name = Datasource.getInstance().searchOneEmployeeById(order.getEmployeeID()).getFullname().toLowerCase();
                    //System.out.println(name);
                    return name.contains(search.toLowerCase());
                    //return check;
                }));
                break;
            case "By customer name":
                filteredList.addAll(orderList.filtered(order -> {
                    if(order.getCustomerID() == null){
                        return true;
                    }
                    String name = Datasource.getInstance().searchOneCustomerById(order.getCustomerID()).getName().toLowerCase();
                    return name.contains(search.toLowerCase());
                }));
                break;
            case "By date":
                filteredList.addAll(orderList.filtered(order -> {
                    String date = order.getOrder_date();
                    return date.contains(search);
                }));
                break;
            case "By table":
                filteredList.addAll(orderList.filtered(order -> {
                    if(order.getTableID() == null){
                        return "take away".contains(search.toLowerCase());
                    }
                    String table = String.valueOf(order.getTableID());
                    return table.contains(search);
                }));
                break;
            case "By coupon id":
                filteredList.addAll(orderList.filtered(order -> {
                    if(order.getCouponID() == null){
                        return "no coupon".contains(search.toLowerCase());
                    }

                    String id = String.valueOf(order.getCouponID());
                    return id.contains(search);
                }));
                break;
            default:
                filteredList.addAll(orderList);
                break;

        }
    }
}
