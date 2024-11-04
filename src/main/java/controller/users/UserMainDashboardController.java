package controller.users;

import controller.UserSessionController;
import controller.users.pages.*;
import controller.users.pages.orders.NewOrderController;
import controller.users.pages.orders.UserOrdersController;
import controller.users.pages.orders.ViewOrderController;
import javafx.event.ActionEvent;
import controller.users.pages.products.ProductsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Datasource;
import model.Order;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserMainDashboardController implements Initializable {
    public Button btnHome;
    public Button btnProducts;
    public Button btnOrders;
    public Button lblLogOut;
    public AnchorPane dashHead;
    public Button btnNewOrder;
    @FXML
    private StackPane dashContent;
    @FXML
    private Label lblUsrName;

    public void btnHomeOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/home/home.fxml");
        UserHomeController homeController = fxmlLoader.getController();
        homeController.getDashboardProdCount();
        homeController.getDashboardCostCount();
    }

    public void btnOrdersOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/orders/orders.fxml");
        UserOrdersController ordersController = fxmlLoader.getController();
        ordersController.setMainDashboardController(this);
    }


    public void btnProductsOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/products/products.fxml");
        ProductsController userController = fxmlLoader.getController();
        userController.listProducts();
    }


    public void btnLogOutOnClick(ActionEvent actionEvent) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure that you want to log out?");
        alert.setTitle("Log Out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            UserSessionController.cleanUserSession();
            Stage dialogStage;
            new Stage();
            Node node = (Node) actionEvent.getSource();
            dialogStage = (Stage) node.getScene().getWindow();
            dialogStage.close();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml")));
            dialogStage.setScene(scene);
            dialogStage.show();
        }
    }

    public void onClickNewOrder(ActionEvent actionEvent) throws IOException{
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/orders/newOrder.fxml");
        NewOrderController controller = fxmlLoader.getController();
        controller.setMainDashboardController(this);
    }

    private FXMLLoader loadFxmlPage(String view_path) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.load(getClass().getResource(view_path).openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        AnchorPane root = fxmlLoader.getRoot();
        dashContent.getChildren().clear();
        dashContent.getChildren().add(root);

        return fxmlLoader;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblUsrName.setText(UserSessionController.getUserFullName());

        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/home/home.fxml");
        UserHomeController homeController = fxmlLoader.getController();
        homeController.getDashboardProdCount();
        homeController.getDashboardCostCount();
    }

    public void viewOrderDetail(ActionEvent actionEvent, Order order) throws IOException{
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/orders/viewOrder.fxml");
        ViewOrderController controller = fxmlLoader.getController();
        controller.setMainDashboardController(this);
        controller.setOrder(order);
        controller.orderIdField.setText(String.valueOf(order.getId()));
        controller.employeeField.setText(Datasource.getInstance().searchOneEmployeeById(order.getEmployeeID()).getFullname());
        if(order.getCustomerID() == null){
            controller.customerField.setText("");
        }
        else{
            controller.customerField.setText(Datasource.getInstance().searchOneCustomerById(order.getCustomerID()).getName());
        }
        if(order.getTableID() == null){
            controller.tableIdField.setText("Take Away");
        }
        else{
            controller.tableIdField.setText(String.valueOf(order.getTableID()));
            controller.tableCapacity.setText(String.valueOf(Datasource.getInstance().getOneTable(order.getTableID()).getCapacity()));
        }
        if(order.getCouponID() == null){
            controller.couponIdField.setText("");
        }
        else{
            controller.couponIdField.setText(String.valueOf(order.getCouponID()));
        }

        controller.setOrderDetailsList(Datasource.getInstance().searchAllOrderDetailByOrderID(order.getId()));
        controller.loadProductList();
        controller.totalText.setText(String.valueOf(order.getTotal()));
        controller.finalText.setText(String.valueOf(order.getFin()));
        controller.discountText.setText(String.valueOf(order.getDiscount()) + "%");
    }
}