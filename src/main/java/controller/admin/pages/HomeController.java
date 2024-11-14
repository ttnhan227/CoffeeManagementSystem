package controller.admin.pages;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Customer;
import model.Datasource;
import model.OrderDetail;
import model.Product;
import model.Order;

import java.text.NumberFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeController {

    @FXML
    public Label productsCount;
    @FXML
    public Label customersCount;
    @FXML
    public Label employeesCount;
    @FXML
    public Label ordersCount;
    @FXML
    private TableView<OrderDetail> bestSellingTable;
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private BarChart<String, Number> revenueBarChart;
    @FXML
    private LineChart<String, Number> growthLineChart;
    @FXML
    private PieChart categoryPerformanceChart;
    public VBox revenueVBox;
    public VBox productVBox;
    public Pagination pagination;

    private Map<Integer, List<Number>> barData;
    private Map<Integer, List<Number>> lineData;
    private List<OrderDetail> detailList = new ArrayList<>();
    private double totalMonth = 0;
    private double totalYear = 0;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            setupBestSellingTable();
            loadBestSellingProducts();
            
            // Then load other components
            getDashboardProdCount();
            getDashboardEmployeeCount();
            getDashboardOrderCount();
            getDashboardCustomerCount();
            
            // Initialize revenue charts
            barData = new HashMap<>();
            lineData = new HashMap<>();
            loadData();
            setupCharts();
            loadCombobox();
            
            // Initial chart update
            updateChart(Year.now().getValue());
            
            // Set up table properties
            setupTableProperties();
            
            // Initialize category performance chart
            setupSalesAnalytics();
            
            loadPage();
        });
    }

    private void setupBestSellingTable() {
        if (bestSellingTable == null) {
            return;
        }
        
        // Create columns
        TableColumn<OrderDetail, String> productColumn = new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productColumn.setId("productNameColumn");

        TableColumn<OrderDetail, Integer> quantityColumn = new TableColumn<>("Quantity Sold");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setId("quantityColumn");

        TableColumn<OrderDetail, Double> totalColumn = new TableColumn<>("Total Revenue");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setId("totalColumn");

        // Format the total revenue column to show currency
        totalColumn.setCellFactory(tc -> new TableCell<OrderDetail, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
                    setText(currencyFormat.format(price));
                }
            }
        });

        // Add columns to table
        bestSellingTable.getColumns().addAll(productColumn, quantityColumn, totalColumn);

        // Center the table in its container
        bestSellingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Prevent table selection
        bestSellingTable.setSelectionModel(null);
        
        // Set table size constraints
        bestSellingTable.setMinHeight(300);
        bestSellingTable.setMaxHeight(400);
        bestSellingTable.setMinWidth(800);
        bestSellingTable.setMaxWidth(1200);
    }

    private void loadBestSellingProducts() {
        if (bestSellingTable == null) {
            return; // Exit if table is not yet initialized
        }
        
        ObservableList<OrderDetail> products = FXCollections.observableArrayList(
            Datasource.getInstance().getTopThreeProducts()
        );
        bestSellingTable.setItems(products);
        
        // Disable table selection
        bestSellingTable.setSelectionModel(null);
    }

    public void getDashboardProdCount() {
        Task<Integer> getDashProdCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllProducts();
            }
        };

        getDashProdCount.setOnSucceeded(e -> {
            productsCount.setText(String.valueOf(getDashProdCount.valueProperty().getValue()));
        });

        new Thread(getDashProdCount).start();
    }

    public void getDashboardEmployeeCount() {
        Task<Integer> getDashEmployeeCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllEmployees();
            }
        };

        getDashEmployeeCount.setOnSucceeded(e -> {
            employeesCount.setText(String.valueOf(getDashEmployeeCount.valueProperty().getValue()));
        });

        new Thread(getDashEmployeeCount).start();
    }

    public void getDashboardOrderCount() {
        Task<Integer> getDashOrderCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllOrders();
            }
        };

        getDashOrderCount.setOnSucceeded(e -> {
            ordersCount.setText(String.valueOf(getDashOrderCount.valueProperty().getValue()));
        });

        new Thread(getDashOrderCount).start();
    }

    public void getDashboardCustomerCount() {
        Task<Integer> getDashCustomerCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllCustomers();
            }
        };

        getDashCustomerCount.setOnSucceeded(e -> {
            customersCount.setText(String.valueOf(getDashCustomerCount.valueProperty().getValue()));
        });

        new Thread(getDashCustomerCount).start();
    }

    private void setupCharts() {
        // Setup Bar Chart
        CategoryAxis barXAxis = (CategoryAxis) revenueBarChart.getXAxis();
        NumberAxis barYAxis = (NumberAxis) revenueBarChart.getYAxis();
        barXAxis.setLabel("Month");
        barYAxis.setLabel("Revenue ($)");
        revenueBarChart.setTitle("Monthly Revenue");
        barYAxis.setTickUnit(500);

        // Setup Line Chart
        CategoryAxis lineXAxis = (CategoryAxis) growthLineChart.getXAxis();
        NumberAxis lineYAxis = (NumberAxis) growthLineChart.getYAxis();
        lineXAxis.setLabel("Month");
        lineYAxis.setLabel("Cumulative Revenue ($)");
        growthLineChart.setTitle("Cumulative Revenue Growth");
        lineYAxis.setTickUnit(500);

        // Apply CSS
        revenueBarChart.getStylesheets().add(getClass().getResource("/view/resources/css/home.css").toExternalForm());
    }

    private void loadCombobox() {
        int currentYear = Year.now().getValue();
        for(int year = 2023; year <= currentYear; year++) {
            yearComboBox.getItems().add(year);
        }
        yearComboBox.setValue(currentYear);
        yearComboBox.setOnAction(e -> updateChart(yearComboBox.getValue()));
    }

    private void loadData() {
        int currentYear = Year.now().getValue();
        for(int year = 2024; year <= currentYear; year++){
            barData.put(year, new ArrayList<>());
            lineData.put(year, new ArrayList<>());
            totalMonth = 0;
            totalYear = 0;
            for(int month = 1; month <= 12; month++){
                totalMonth = 0;
                detailList.clear();
                List<OrderDetail> temp = Datasource.getInstance().searchAllOrderDetailByYear(year, month);
                if(temp == null){
                    detailList.clear();
                }
                else{
                    detailList.addAll(temp);
                }

                if(detailList.isEmpty()){
                    barData.get(year).add(0);
                    lineData.get(year).add(0);
                }
                else{
                    for(OrderDetail detail : detailList){
                        totalMonth += detail.getTotal();
                    }
                    totalYear += totalMonth;
                    barData.get(year).add(totalMonth);
                    lineData.get(year).add(totalYear);
                }
            }
        }
        barData.put(2023, List.of(8000, 12000, 15000, 17000, 14000, 16000, 18000, 20000, 22000, 24000, 26000, 28000));
        lineData.put(2023, List.of(8000, 20000, 35000, 52000, 66000, 82000, 100000, 120000, 142000, 166000, 192000, 220000));
    }

    public void updateChart(int year) {
        List<Number> barD = barData.get(year);
        List<Number> lineD = lineData.get(year);

        if(barD == null){
            System.out.println("no data");
            return;
        }
        if(lineD == null){
            System.out.println("no data");
            return;
        }

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName(year + " Monthly Revenue");
        CategoryAxis xAxis = (CategoryAxis) revenueBarChart.getXAxis();
        xAxis.setCategories(FXCollections.observableArrayList(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        ));
        for (int i = 0; i < barD.size(); i++) {
            barSeries.getData().add(new XYChart.Data<>(getMonth(i), barD.get(i)));
        }
        revenueBarChart.getData().setAll(barSeries);

        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName(year + " Cumulative Revenue");
        CategoryAxis xAxisb = (CategoryAxis) growthLineChart.getXAxis();
        xAxisb.setCategories(FXCollections.observableArrayList(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        ));
        for (int i = 0; i < lineD.size(); i++) {
            if(!lineD.get(i).equals(0)){
                lineSeries.getData().add(new XYChart.Data<>(getMonth(i), lineD.get(i)));
            }
        }
        growthLineChart.getData().setAll(lineSeries);
        loadTooltip();
    }

    private String getMonth(int monthIndex) {
        return List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").get(monthIndex);
    }

    private void loadTooltip() {
        for (XYChart.Series<String, Number> series : revenueBarChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if(data == null || data.getYValue() == null){
                    continue;
                }
                Tooltip tooltip = new Tooltip(data.getYValue().toString());
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.setHideDelay(Duration.millis(50));
                data.getNode().setOnMouseEntered(e -> {
                    tooltip.show(data.getNode(), e.getScreenX() + 10, e.getScreenY() + 10);
                    data.getNode().setStyle("-fx-bar-fill: #4682B4;"); // Optional: Color change
                });

                data.getNode().setOnMouseExited(e -> {
                    tooltip.hide();
                    data.getNode().setStyle(""); // Reset color on exit
                });
            }
        }

        for (XYChart.Series<String, Number> series : growthLineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip("Month: " + data.getXValue() + "\nValue: " + data.getYValue());
                Node node = data.getNode();
                if (node == null) {
                    node = new StackPane();
                    data.setNode(node);
                }
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.setHideDelay(Duration.millis(50));
                Tooltip.install(node, tooltip);

                final Node finalNode = node;
                finalNode.setOnMouseEntered(e -> {
                    finalNode.setStyle("-fx-background-color: red;");
                });
                finalNode.setOnMouseExited(e -> {
                    finalNode.setStyle("");
                });
            }
        }
    }
    private void loadPage() {
        // Initially hide both boxes
        productVBox.setVisible(false);
        revenueVBox.setVisible(false);
        
        pagination.setPageFactory(pageIndex -> {
            // Hide both boxes first
            productVBox.setVisible(false);
            revenueVBox.setVisible(false);
            
            // Show and return the appropriate box
            if (pageIndex == 0) {
                productVBox.setVisible(true);
                return productVBox;
            } else {
                revenueVBox.setVisible(true);
                return revenueVBox;
            }
        });

        // Show initial page
        Platform.runLater(() -> {
            productVBox.setVisible(true);
            productVBox.setDisable(false);
            revenueVBox.setDisable(false);
        });
    }

    private void setupTableProperties() {
        bestSellingTable.setFixedCellSize(50);
        bestSellingTable.setPrefHeight(400);
        bestSellingTable.setMaxHeight(400);
        bestSellingTable.setMinHeight(400);
        bestSellingTable.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-table-cell-border-color: transparent;"
        );
    }

    private void setupSalesAnalytics() {
        Map<String, Double> categoryRevenue = new HashMap<>();
        List<OrderDetail> allOrders = Datasource.getInstance().getTopThreeProducts();
        
        for (OrderDetail order : allOrders) {
            Product product = Datasource.getInstance().searchOneProductById(order.getProductID());
            String categoryName = Datasource.getInstance().getCategoryName(product.getCategory_id());
            categoryRevenue.merge(categoryName, order.getTotal(), Double::sum);
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        categoryRevenue.forEach((category, revenue) -> {
            pieChartData.add(new PieChart.Data(category, revenue));
        });
        
        categoryPerformanceChart.setData(pieChartData);
        
        // Add percentage labels
        double total = categoryRevenue.values().stream().mapToDouble(Double::doubleValue).sum();
        pieChartData.forEach(data -> {
            double percentage = (data.getPieValue() / total) * 100;
            String text = String.format("%s\n%.1f%%", data.getName(), percentage);
            data.setName(text);
        });
        
        // Add tooltips
        categoryPerformanceChart.getData().forEach(data -> {
            Tooltip tooltip = new Tooltip(String.format(
                "Category: %s\nRevenue: $%.2f",
                data.getName().split("\n")[0],
                data.getPieValue()
            ));
            Tooltip.install(data.getNode(), tooltip);
        });
    }
}
