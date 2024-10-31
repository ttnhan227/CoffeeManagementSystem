package controller.admin.pages;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import model.Datasource;

public class HomeController {

    @FXML
    public Label productsCount;
    @FXML
    public Label customersCount;
    @FXML
    public LineChart<String, Number> dataChart;

    @FXML
    public void initialize() {
        // Initialize the chart with product and customer data
        setupLineChart();
        // Load product and customer counts
        getDashboardProdCount();
        getDashboardCostCount();
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
}
