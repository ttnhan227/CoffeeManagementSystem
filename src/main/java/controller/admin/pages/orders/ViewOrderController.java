package controller.admin.pages.orders;

import controller.admin.MainDashboardController; // Ensure this imports the correct controller
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewOrderController implements Initializable {
    private MainDashboardController mainDashboardController; // Adjusted to use the correct dashboard controller

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
    public Button printBtn;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<OrderDetail> orderDetailsList;

    private Order order;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTable(); // Load table columns on initialization
    }

    // Method to set the dashboard controller
    public void setAdminMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setOrder(Order order) {
        this.order = order;
        loadOrderDetails(); // Load order details when the order is set
    }

    public void setOrderDetailsList(List<OrderDetail> list){
        orderDetailsList = FXCollections.observableArrayList(list);
        loadProductList(); // Load product list based on order details
    }

    private void loadOrderDetails() {
        if (order != null) {
            orderIdField.setText(String.valueOf(order.getId()));
            employeeField.setText(Datasource.getInstance().searchOneEmployeeById(order.getEmployeeID()).getFullname());
            customerField.setText(order.getCustomerID() != null
                    ? Datasource.getInstance().searchOneCustomerById(order.getCustomerID()).getName()
                    : "");
            tableIdField.setText(order.getTableID() != null
                    ? String.valueOf(order.getTableID())
                    : "Take Away");
            tableCapacity.setText(order.getTableID() != null
                    ? String.valueOf(Datasource.getInstance().getOneTable(order.getTableID()).getCapacity())
                    : "");
            couponIdField.setText(order.getCouponID() != null
                    ? String.valueOf(order.getCouponID())
                    : "");
            totalText.setText(String.valueOf(order.getTotal()));
            finalText.setText(String.valueOf(order.getFin()));
            discountText.setText(order.getDiscount() + "%");
        }
    }

    public void loadProductList() {
        productList.clear(); // Clear existing items before loading new
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
            return new SimpleIntegerProperty(orderDetailsList.get(index).getQuantity()).asObject();
        });
        totalColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            int index = productTable.getItems().indexOf(product);
            return new SimpleDoubleProperty(orderDetailsList.get(index).getTotal()).asObject();
        });
    }

    @FXML
    private void toOrder() {
        mainDashboardController.btnOrdersOnClick(new ActionEvent()); // Navigate back to orders
    }
    @FXML
    public void printOnClick(){
        //Node parentNode = printBtn.getParent();

        Stage billPreview = new Stage();
        billPreview.initModality(Modality.APPLICATION_MODAL);
        billPreview.setTitle("Bill Review");

        VBox reviewLayout = new VBox();
        reviewLayout.setAlignment(Pos.CENTER);
        reviewLayout.setSpacing(5);
        reviewLayout.setPadding(new Insets(10));

        Label employee_label = new Label("Employee name: " + employeeField.getText());
        Label customer_label = new Label("Customer name: " + customerField.getText());
        Label order_label = new Label("Order id: " + orderIdField.getText());
        Label table_label = new Label("Table id: " + tableIdField.getText());
        Label capacity_label = new Label("Table capacity: " + tableCapacity.getText());
        Label coupon_label = new Label("Coupon id: " + couponIdField.getText());

        //Label space_label = new Label("-------------------------------------");

        reviewLayout.getChildren().addAll(employee_label, customer_label, order_label,
        table_label, capacity_label, coupon_label,
                new Label("-----------------------------------------------"));

//        GridPane column_title = new GridPane();
//        column_title.setHgap(20);
//        column_title.add(new Label("Product name"), 0, 0);
//        column_title.add(new Label("Category"), 1, 0);
//        column_title.add(new Label("Price"), 2, 0);
//        column_title.add(new Label("Quantity"), 3, 0);
//        column_title.add(new Label("Total"), 4, 0);
//        column_title.setAlignment(Pos.CENTER);

        //TextFlow textFlow = new TextFlow();

        Text header = new Text(String.format("%-20s %-15s %-10s %-10s %-10s",
                "Product name", "Category", "Price", "Quantity", "Total"));
        header.setFont(Font.font("Monospaced"));
        //textFlow.getChildren().add(header);

        reviewLayout.getChildren().addAll(header,
                new Label("-----------------------------------------------"));

        for(Product product : productList){
            int index = productList.indexOf(product);
            TableColumn<Product, ?> category_column = productTable.getColumns().get(2);
            TableColumn<Product, ?> quantity_column = productTable.getColumns().get(4);
            TableColumn<Product, ?> total_column = productTable.getColumns().get(5);
//            Label product_label = new Label(product.getName() + "    " + category_column.getCellData(index)
//            + "    " + product.getPrice() + "    " + quantity_column.getCellData(index) + "    "
//                    + total_column.getCellData(index));
//            reviewLayout.getChildren().add(product_label);
//            Label product_name = new Label(product.getName());
//            Label category_name = new Label("" + category_column.getCellData(index));
//            Label price_label = new Label("" + product.getPrice());
//            Label quantity_label = new Label("" + quantity_column.getCellData(index));
//            Label total_label = new Label("" + total_column.getCellData(index));
//
//            GridPane gridPane = new GridPane();
//            gridPane.setHgap(20);
//            gridPane.add(product_name, 0, 0);
//            gridPane.add(category_name, 1, 0);
//            gridPane.add(price_label, 2, 0);
//            gridPane.add(quantity_label, 3, 0);
//            gridPane.add(total_label, 4, 0);
//            gridPane.setAlignment(Pos.CENTER);
            Text row = new Text(String.format("%-20s %-15s %-10.2f %-10d %-10.2f",
                    product.getName(), category_column.getCellData(index), product.getPrice(),
                    quantity_column.getCellData(index), total_column.getCellData(index)));
            row.setFont(Font.font("Monospaced"));
            reviewLayout.getChildren().add(row);

        }

        Label payment_label = new Label("Payment: " + totalText.getText() +
                " \nDiscount: " + discountText.getText() +
                "\nPayment after discount: " + finalText.getText());
        reviewLayout.getChildren().add(new Label("-----------------------------------------------"));
        reviewLayout.getChildren().add(payment_label);

        Scene previewScene = new Scene(reviewLayout, 800, 600);
        billPreview.setScene(previewScene);

        billPreview.show();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PNG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            // Ensure rendering is complete before taking the snapshot
            Platform.runLater(() -> saveNodeAsPng(reviewLayout, file));
        }
    }

    private void saveNodeAsPng(Node node, File file) {
        try {
            // Check and print node bounds for debugging
            System.out.println("Node width: " + node.getBoundsInParent().getWidth());
            System.out.println("Node height: " + node.getBoundsInParent().getHeight());

            // Create the snapshot with the node's size
            WritableImage snapshot = new WritableImage(
                    (int) node.getBoundsInParent().getWidth(),
                    (int) node.getBoundsInParent().getHeight()
            );
            node.snapshot(null, snapshot);

            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

            ImageIO.write(bufferedImage, "PNG", file);
            System.out.println("PNG saved to: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
