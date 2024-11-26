package controller.admin;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import controller.UserSessionController;
import controller.admin.pages.CouponController;
import controller.admin.pages.HomeController;
import controller.admin.pages.customers.CustomerController;
import controller.admin.pages.orders.NewOrderController;
import controller.admin.pages.orders.UserOrdersController;
import controller.admin.pages.orders.ViewOrderController;
import controller.admin.pages.products.ProductsController;
import controller.admin.pages.users.UsersController;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import model.Datasource;
import model.Order;
import controller.SessionManager;

public class MainDashboardController implements Initializable {
    @FXML
    public Button btnHome;
    @FXML
    public Button btnProducts;
    @FXML
    public Button btnUsers;
    @FXML
    public Button btnOrders;
    @FXML
    public Button btnSettings;
    @FXML
    public Button btnTable;
    @FXML
    public Button lblLogOut;
    @FXML
    public AnchorPane dashHead;
    public Button btnCoupon;
    public Button btnRevenue;
    @FXML
    private StackPane dashContent;
    @FXML
    private Label lblUsrName;
    public Button btnNewOrder;
    @FXML
    public Button btnCustomer;
    @FXML
    private StackPane loadingOverlay;

    public void btnHomeOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/home.fxml");
    }

    public void btnProductsOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/products/products.fxml");
        ProductsController controller = fxmlLoader.getController();
        controller.listProducts();
    }

    public void btnUsersOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/users/users.fxml");
        UsersController controller = fxmlLoader.getController();
        controller.listUsers();
    }

    public void btnCustomerOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/customers/customers.fxml");
        CustomerController controller = fxmlLoader.getController();
        controller.listCustomers();
    }

    public void btnSettingsOnClick(ActionEvent actionEvent) {
        loadFxmlPage("/view/admin/pages/settings/settings.fxml");
    }

    public void btnLogOutOnClick(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure that you want to log out?");
        alert.setTitle("Log Out?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            UserSessionController.cleanUserSession();
            SessionManager.getInstance().clearSession();

            Stage dialogStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            dialogStage.close();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/view/login.fxml")));
            dialogStage.setScene(scene);
            dialogStage.show();
        }
    }

    public void btnOrdersOnClick(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/orders/orders.fxml");
        UserOrdersController controller = fxmlLoader.getController();
        controller.setAdminMainDashboardController(this);
    }

    public void onClickNewOrder(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/orders/newOrder.fxml");
        NewOrderController controller = fxmlLoader.getController();
        controller.setAdminMainDashboardController(this);
    }

    public void viewOrderDetail(ActionEvent actionEvent, Order order) throws IOException {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/orders/viewOrder.fxml");
        ViewOrderController controller = fxmlLoader.getController();
        controller.setAdminMainDashboardController(this);
        controller.setOrder(order);

        controller.orderIdField.setText(String.valueOf(order.getId()));
        controller.employeeField.setText(Datasource.getInstance().searchOneEmployeeById(order.getEmployeeID()).getFullname());
        if (order.getCustomerID() == null) {
            controller.customerField.setText("");
        } else {
            controller.customerField.setText(Datasource.getInstance().searchOneCustomerById(order.getCustomerID()).getName());
        }
        if (order.getTableID() == null) {
            controller.tableIdField.setText("Take Away");
        } else {
            controller.tableIdField.setText(String.valueOf(order.getTableID()));
            controller.tableCapacity.setText(String.valueOf(Datasource.getInstance().getOneTable(order.getTableID()).getCapacity()));
        }
        if (order.getCouponID() == null) {
            controller.couponIdField.setText("");
        } else {
            controller.couponIdField.setText(String.valueOf(order.getCouponID()));
        }

        controller.setOrderDetailsList(Datasource.getInstance().searchAllOrderDetailByOrderID(order.getId()));
        controller.loadProductList();
        controller.totalText.setText(String.valueOf(order.getTotal()));
        controller.finalText.setText(String.valueOf(order.getFin()));
        controller.discountText.setText(order.getDiscount() + "%");
    }

    public void btnCouponOnClick() {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/coupon.fxml");
        CouponController controller = fxmlLoader.getController();
        controller.setMainDashboardController(this);
    }

    public void btnTableOnClick() {
        FXMLLoader fxmlLoader = loadFxmlPage("/view/admin/pages/table.fxml");
    }

    private void showLoading() {
        if (loadingOverlay == null) {
            loadingOverlay = new StackPane();
            loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(50, 50);
            progressIndicator.getStyleClass().add("spinner");

            loadingOverlay.getChildren().add(progressIndicator);
            dashContent.getChildren().add(loadingOverlay);
            StackPane.setAlignment(loadingOverlay, Pos.CENTER);
        }
        loadingOverlay.setVisible(true);

        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), loadingOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            // Add fade-out animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> loadingOverlay.setVisible(false));
            fadeOut.play();
        }
    }

    private FXMLLoader loadFxmlPage(String fxmlPath) {
        try {
            showLoading();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = fxmlLoader.load();

            // Add fade-in animation for the new content
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            // Clear existing content with fade-out
            if (!dashContent.getChildren().isEmpty()) {
                Node oldContent = dashContent.getChildren().get(0);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldContent);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    dashContent.getChildren().clear();
                    dashContent.getChildren().add(node);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                dashContent.getChildren().add(node);
                fadeIn.play();
            }

            hideLoading();
            return fxmlLoader;

        } catch (IOException e) {
            hideLoading();
            e.printStackTrace();
            showError("Error loading page", "Could not load " + fxmlPath);
            return null;
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblUsrName.setText(UserSessionController.getUserFullName());

        // Apply scale transition on buttons for hover effect
        applyScaleEffect(btnHome);
        applyScaleEffect(btnProducts);
        applyScaleEffect(btnUsers);
        applyScaleEffect(btnOrders);
        applyScaleEffect(lblLogOut);
        applyScaleEffect(btnNewOrder);
        applyScaleEffect(btnCustomer);

        // Load home page by default
        loadFxmlPage("/view/admin/pages/home.fxml");
    }

    private void applyScaleEffect(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), button);
        scaleIn.setToX(1.1);
        scaleIn.setToY(1.1);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        button.setOnMouseEntered(e -> scaleIn.play());
        button.setOnMouseExited(e -> scaleOut.play());
    }
}