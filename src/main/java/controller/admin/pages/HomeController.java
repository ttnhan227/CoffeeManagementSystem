package controller.admin.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Datasource;
import model.OrderDetail;
import model.Product;

import java.awt.print.Pageable;

public class HomeController {

    @FXML
    public Label productsCount;
    @FXML
    public Label customersCount;
    @FXML
    public LineChart<String, Number> dataChart;
    public Pagination pagination;
    public GridPane gridPane;

    private VBox vbox = new VBox();

    @FXML
    public void initialize() {
        // Initialize the chart with product and customer data
        setupLineChart();
        // Load product and customer counts
        getDashboardProdCount();
        getDashboardCostCount();
        loadTable();
    }

    private void setupLineChart() {
        XYChart.Series<String, Number> productSeries = new XYChart.Series<>();
        productSeries.setName("Products");

        XYChart.Series<String, Number> customerSeries = new XYChart.Series<>();
        customerSeries.setName("Customers");

        // Adding placeholder values (0) initially
        productSeries.getData().add(new XYChart.Data<>("Product Count", 0));
        customerSeries.getData().add(new XYChart.Data<>("Employee Count", 0));

        // Add the series to the chart
        dataChart.getData().addAll(productSeries, customerSeries);
    }

    public void getDashboardProdCount() {
        Task<Integer> getDashProdCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllProducts();
            }
        };

        getDashProdCount.setOnSucceeded(e -> {
            int productCount = getDashProdCount.valueProperty().getValue();
            productsCount.setText(String.valueOf(productCount));

            // Update the chart with actual product count
            XYChart.Series<String, Number> productSeries = dataChart.getData().get(0); // Products series is the first one
            productSeries.getData().clear(); // Clear placeholder data
            productSeries.getData().add(new XYChart.Data<>("Product Count", productCount));
        });

        new Thread(getDashProdCount).start();
    }

    public void getDashboardCostCount() {
        Task<Integer> getDashCostCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllCustomers();
            }
        };

        getDashCostCount.setOnSucceeded(e -> {
            int customerCount = getDashCostCount.valueProperty().getValue();
            customersCount.setText(String.valueOf(customerCount));

            // Update the chart with actual customer count
            XYChart.Series<String, Number> customerSeries = dataChart.getData().get(1); // Customers series is the second one
            customerSeries.getData().clear(); // Clear placeholder data
            customerSeries.getData().add(new XYChart.Data<>("Employee Count", customerCount));
        });

        new Thread(getDashCostCount).start();
    }

    private void loadTable(){
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        Text text = new Text("Best selling products");
        text.setFill(Color.RED);
        text.setFont(new Font(30));
        TableView<OrderDetail> tableView = new TableView<>();
        TableColumn<OrderDetail, String> productColumn = new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<OrderDetail, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<OrderDetail, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        productColumn.setMinWidth(150);
        quantityColumn.setMinWidth(150);
        totalColumn.setMinWidth(150);

        tableView.getColumns().addAll(productColumn, quantityColumn, totalColumn);
        ObservableList<OrderDetail> list = FXCollections.observableArrayList(Datasource.getInstance().getTopThreeProducts());
        tableView.setItems(list);
        vbox.getChildren().add(text);
        vbox.getChildren().add(tableView);

        pagination.setPageFactory(pageIndex -> pageIndex == 0 ? gridPane : vbox);
    }
}
