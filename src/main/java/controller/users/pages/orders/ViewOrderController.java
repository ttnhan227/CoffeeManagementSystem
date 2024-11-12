package controller.users.pages.orders;

import controller.users.UserMainDashboardController;
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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

public class ViewOrderController implements Initializable {
    public UserMainDashboardController mainDashboardController;

    public TextField orderIdField;
    public TextField employeeField;
    public TextField customerField;
    public TextField tableIdField;
    public TextField tableCapacity;
    public TextField couponIdField;
    public Text totalText;
    public Text finalText;
    public Text discountText;
    public TableView<Product> productTable;
    public TableColumn<Product, Integer> idColumn;
    public TableColumn<Product, String> nameColumn;
    public TableColumn<Product, Double> priceColumn;
    public TableColumn<Product, Integer> quantityColumn;
    public TableColumn<Product, Double> totalColumn;
    public TableColumn<Product, String> categoryColumn;
    public Button backToOrderBtn;
    public Button printBtn;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<OrderDetail> orderDetailsList;

    private Order order;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //orderIdField.setText(String.valueOf(order.getId()));
        loadTable();
    }

    public void setMainDashboardController(UserMainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
    public void setProductList(List<Product> list){
        productList = FXCollections.observableArrayList(list);
    }
    public void setOrderDetailsList(List<OrderDetail> list){
        orderDetailsList = FXCollections.observableArrayList(list);
    }

    public void loadProductList(){
        for(OrderDetail detail: orderDetailsList){
            Product product = Datasource.getInstance().searchOneProductById(detail.getProductID());
            productList.add(product);
            System.out.println(product.getName());
        }
        productTable.setItems(productList);
    }

    public void loadTable(){

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            String category = Datasource.getInstance().getCategoryName(product.getCategory_id());
            return new SimpleStringProperty(category);
        });
        quantityColumn.setCellValueFactory(cellData -> {
            // Use the row index to get the corresponding quantity
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
    private void toOrder(){
        mainDashboardController.btnOrdersOnClick(new ActionEvent());
    }

    @FXML
    public void printOnClick(){
        Node parentNode = printBtn.getParent();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PNG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            // Ensure rendering is complete before taking the snapshot
            Platform.runLater(() -> saveNodeAsPng(parentNode, file));
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
