package controller.users;

import controller.UserSessionController;
import controller.users.pages.customers.CustomerController;
import controller.users.pages.*;
import controller.users.pages.orders.NewOrderController;
import controller.users.pages.orders.UserOrdersController;
import controller.users.pages.orders.ViewOrderController;
import controller.users.pages.products.ProductsController;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
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
import javafx.util.Duration;
import model.Datasource;
import model.Order;
import controller.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserMainDashboardController implements Initializable {
    @FXML
    public Button btnHome;
    @FXML
    public Button btnProducts;
    @FXML
    public Button btnOrders;
    @FXML
    public Button lblLogOut;
    @FXML
    public AnchorPane dashHead;
    @FXML
    public Button btnNewOrder;
    @FXML
    public Button btnTable;
    @FXML
    private StackPane dashContent;
    @FXML
    private Label lblUsrName;
    @FXML
    public Button btnCustomer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set user name from session
        lblUsrName.setText(UserSessionController.getUserFullName());
        
        // Load home page by default
        btnHomeOnClick(null);
        
        // Add hover animations to menu buttons
        addButtonAnimation(btnHome);
        addButtonAnimation(btnProducts);
        addButtonAnimation(btnOrders);
        addButtonAnimation(btnNewOrder);
        addButtonAnimation(btnCustomer);
        addButtonAnimation(btnTable);
    }

    private void addButtonAnimation(Button button) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        
        button.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.playFromStart();
        });
        
        button.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.playFromStart();
        });
    }

    private FXMLLoader loadFxmlPage(String viewPath) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.setLocation(getClass().getResource(viewPath));
            Node node = fxmlLoader.load();
            dashContent.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error Loading Page", "Failed to load the requested page. Please try again.");
        }
        return fxmlLoader;
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void btnHomeOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/home.fxml");
    }

    @FXML
    public void btnOrdersOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/orders/orders.fxml");
        UserOrdersController ordersController = fxmlLoader.getController();
        ordersController.setMainDashboardController(this);
    }

    @FXML
    public void btnProductsOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/products/products.fxml");
        ProductsController productsController = fxmlLoader.getController();
        productsController.listProducts();
    }

    @FXML
    public void btnLogOutOnClick(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out?");
        alert.setHeaderText("Are you sure that you want to log out?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserSessionController.cleanUserSession();
            SessionManager.getInstance().clearSession();
            
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.close();
            
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml")));
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.show();
        }
    }

    @FXML
    public void onClickNewOrder(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/orders/newOrder.fxml");
        NewOrderController controller = fxmlLoader.getController();
        controller.setMainDashboardController(this);
    }

    @FXML
    public void btnCustomerOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/customers/customers.fxml");
        CustomerController controller = fxmlLoader.getController();
    }

    @FXML
    public void btnTableOnClick(ActionEvent actionEvent) {
        loadFxmlPage("/view/users/pages/tables/tables.fxml");
    }

    public void viewOrderDetail(ActionEvent actionEvent, Order order) throws IOException {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/users/pages/orders/viewOrder.fxml");
        ViewOrderController controller = fxmlLoader.getController();
        controller.setMainDashboardController(this);
        controller.setOrder(order);
        controller.loadProductList();
        controller.totalText.setText(String.valueOf(order.getTotal()));
        controller.finalText.setText(String.valueOf(order.getFin()));
        controller.discountText.setText(order.getDiscount() + "%");
    }
}
