package controller.users.pages.orders;

import controller.users.UserMainDashboardController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Datasource;
import model.Order;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
            return new SimpleStringProperty(Datasource.getInstance().searchOneEmployeeById(emid).getFullname());
        });
        customerColumn.setCellValueFactory(cellData -> {
            int index = tableOrdersPage.getItems().indexOf(cellData.getValue());
            Integer cusid = filteredList.get(index).getCustomerID();
            if(cusid == null){
                return null;
            }
            else{
                return new SimpleStringProperty(Datasource.getInstance().searchOneCustomerById(cusid).getName());
            }

        });
        addActionButton();
        //filteredList = FXCollections.observableArrayList(orderList);
        tableOrdersPage.setItems(filteredList);
    }

    @FXML
    private void addActionButton(){
        TableColumn<Order, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button viewButton = new Button("View");
            //private final Button deleteButton = new Button("Delete");

            {
                // Set the action for the Edit button
                viewButton.setOnAction(e -> {
                    try {
                        mainDashboardController.viewOrderDetail(new ActionEvent(), getTableRow().getItem());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Add buttons to the cell
                    HBox hbox = new HBox(viewButton);
                    setGraphic(hbox);
                }
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
            if(newValue.equals("All")){
                searchField.setDisable(true);
            }
            else {
                searchField.setDisable(false);
            }
        });
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            applyFilter(searchComboBox.getValue(), newText);
            if(newText == null){
                applyFilter("All", newText);
            }
            if(searchComboBox.getValue().equals("All")){
                searchField.setDisable(true);
            }
            else {
                searchField.setDisable(false);
            }
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
                        if("take away".contains(search.toLowerCase())){
                            return true;
                        }
                        return false;
                    }
                    String table = String.valueOf(order.getTableID());
                    return table.contains(search);
                }));
                break;
            case "By coupon id":
                filteredList.addAll(orderList.filtered(order -> {
                    if(order.getCouponID() == null){
                        if("no coupon".contains(search.toLowerCase())){
                            return true;
                        }
                        return false;
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
