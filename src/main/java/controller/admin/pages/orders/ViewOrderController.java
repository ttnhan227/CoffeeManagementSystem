package controller.admin.pages.orders;

import controller.admin.MainDashboardController;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import model.*;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class ViewOrderController implements Initializable {
    public MainDashboardController mainDashboardController;

    @FXML
    public TextField orderIdField;
    @FXML
    public TextField employeeField;
    @FXML
    public TextField customerField;
    @FXML
    public TextField tableIdField;
    @FXML
    public TextField tableCapacity;
    @FXML
    public TextField couponIdField;
    @FXML
    public Text totalText;
    @FXML
    public Text finalText;
    @FXML
    public Text discountText;
    @FXML
    public TableView<Product> productTable;
    @FXML
    public TableColumn<Product, Integer> idColumn;
    @FXML
    public TableColumn<Product, String> nameColumn;
    @FXML
    public TableColumn<Product, Double> priceColumn;
    @FXML
    public TableColumn<Product, Integer> quantityColumn;
    @FXML
    public TableColumn<Product, Double> totalColumn;
    @FXML
    public TableColumn<Product, String> categoryColumn;
    @FXML
    public Button backToOrderBtn;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<OrderDetail> orderDetailsList;
    private Order order;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTable();
    }

    public void setAdminMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setOrder(Order order) {
        this.order = order;
        orderIdField.setText(String.valueOf(order.getId()));
        employeeField.setText(Datasource.getInstance().searchOneEmployeeById(order.getEmployeeID()).getFullname());

        if (order.getCustomerID() == null) {
            customerField.setText("");
        } else {
            customerField.setText(Datasource.getInstance().searchOneCustomerById(order.getCustomerID()).getName());
        }

        if (order.getTableID() == null) {
            tableIdField.setText("Take Away");
        } else {
            tableIdField.setText(String.valueOf(order.getTableID()));
            tableCapacity.setText(String.valueOf(Datasource.getInstance().getOneTable(order.getTableID()).getCapacity()));
        }

        if (order.getCouponID() == null) {
            couponIdField.setText("");
        } else {
            couponIdField.setText(String.valueOf(order.getCouponID()));
        }

        totalText.setText(String.valueOf(order.getTotal()));
        finalText.setText(String.valueOf(order.getFin()));
        discountText.setText(String.valueOf(order.getDiscount()) + "%");

        // Load order details and products
        setOrderDetailsList(Datasource.getInstance().searchAllOrderDetailByOrderID(order.getId()));
        loadProductList();
    }

    public void setOrderDetailsList(List<OrderDetail> list) {
        orderDetailsList = FXCollections.observableArrayList(list);
    }

    public void loadProductList() {
        for (OrderDetail detail : orderDetailsList) {
            Product product = Datasource.getInstance().searchOneProductById(detail.getProductID());
            productList.add(product);
        }
        productTable.setItems(productList);
    }

    public void loadTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        categoryColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            String category = Datasource.getInstance().getCategoryName(product.getCategory_id());
            return new SimpleStringProperty(category);
        });

        quantityColumn.setCellValueFactory(cellData -> {
            int index = productTable.getItems().indexOf(cellData.getValue());
            if (index >= 0 && index < orderDetailsList.size()) {  // Ensure index is within bounds
                return new SimpleIntegerProperty(orderDetailsList.get(index).getQuantity()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject();  // Default value if index out of bounds
        });

        totalColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            int index = productTable.getItems().indexOf(product);
            if (index >= 0 && index < orderDetailsList.size()) {  // Ensure index is within bounds
                return new SimpleDoubleProperty(orderDetailsList.get(index).getTotal()).asObject();
            }
            return new SimpleDoubleProperty(0.0).asObject();  // Default value if index out of bounds
        });
    }


    @FXML
    private void toOrder() {
        mainDashboardController.btnOrdersOnClick(new ActionEvent());
    }
}
