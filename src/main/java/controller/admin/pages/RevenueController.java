package controller.admin.pages;

import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import model.Datasource;
import model.OrderDetail;

import java.net.URL;
import java.time.Year;
import java.util.*;

public class RevenueController implements Initializable {

    public BarChart<String, Number> barChart;
    public LineChart<String, Number> lineChart;
    //public CategoryAxis barMonth;
    //public CategoryAxis lineMonth;
    //public NumberAxis barSold;
    //public NumberAxis lineRevenue;
    public Pagination pagination;
    public ComboBox<Integer> yearComboBox;
    private Map<Integer, List<Number>> barData;
    private Map<Integer, List<Number>> lineData;
    private List<OrderDetail> detailList = new ArrayList<>();
    private double totalMonth = 0;
    private double totalYear = 0;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        barData = new HashMap<>();
        lineData = new HashMap<>();
        loadData();
        loadCombobox();

        BarChart<String, Number> barChart = createBarChart();
        LineChart<String, Number> lineChart = createLineChart();

        updateChart(2024);

        barChart.setPrefHeight(600);

        lineChart.setPrefHeight(600);
        barChart.setCategoryGap(20);

        barChart.getStylesheets().add(getClass().getResource("/view/resources/css/chart-style.css").toExternalForm());
        lineChart.getStylesheets().add(getClass().getResource("/view/resources/css/chart-style.css").toExternalForm());

        //loadTooltip();
        pagination.setPageFactory(pageIndex -> pageIndex == 0 ? barChart : lineChart);
    }

    public BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue ($)");
        barChart.setTitle("Monthly Revenue");

        yAxis.setTickUnit(500);

        barChart.setData(FXCollections.observableArrayList(new XYChart.Series<>()));

        return barChart;
    }

    public LineChart<String, Number> createLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        lineChart = new LineChart<>(xAxis, yAxis);
        xAxis.setLabel("Month");
        yAxis.setLabel("Cumulative Revenue ($)");
        lineChart.setTitle("Cumulative Revenue Growth");
//        lineChart.getYAxis().setAutoRanging(true);
//        lineChart.getXAxis().setAutoRanging(true);

        yAxis.setTickUnit(500);

        lineChart.setData(FXCollections.observableArrayList(new XYChart.Series<>()));

        return lineChart;
    }

    private void loadCombobox(){
        int currentYear = Year.now().getValue();
        for(int year = 2023; year <= currentYear; year++){
            yearComboBox.getItems().add(year);
        }
        yearComboBox.setValue(currentYear);
        yearComboBox.setOnAction(e -> updateChart(yearComboBox.getValue()));
    }

    public void updateChart(int year){
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
        //barChart.getData().clear();
        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName(year + " Monthly Revenue");
        CategoryAxis xAxis = (CategoryAxis) barChart.getXAxis();
        xAxis.setCategories(FXCollections.observableArrayList(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        ));
        for (int i = 0; i < barD.size(); i++) {
            barSeries.getData().add(new XYChart.Data<>(getMonth(i), barD.get(i)));
        }
        //System.out.println(barSeries.getData());
        barChart.getData().setAll(barSeries);

        //lineChart.getData().clear();
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName(year + " Cumulative Revenue");
        CategoryAxis xAxisb = (CategoryAxis) lineChart.getXAxis();
        xAxisb.setCategories(FXCollections.observableArrayList(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        ));
        for (int i = 0; i < lineD.size(); i++) {
            if(!lineD.get(i).equals(0)){
                lineSeries.getData().add(new XYChart.Data<>(getMonth(i), lineD.get(i)));
                //System.out.println(lineD.get(i));
            }
        }
        lineChart.getData().setAll(lineSeries);
        loadTooltip();
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
                    //System.out.println(detailList);
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
//                    System.out.println(totalMonth);
//                    System.out.println(totalYear);
                    barData.get(year).add(totalMonth);
                    //System.out.println(barData.get(year));
                    lineData.get(year).add(totalYear);
                    //System.out.println(lineData.get(year));
                }

            }
        }
        barData.put(2023, List.of(8000, 12000, 15000, 17000, 14000, 16000, 18000, 20000, 22000, 24000, 26000, 28000));
        lineData.put(2023, List.of(8000, 20000, 35000, 52000, 66000, 82000, 100000, 120000, 142000, 166000, 192000, 220000));

//        barData.put(2024, List.of(9000, 11000, 16000, 15000, 18000, 17000, 21000, 23000, 25000, 27000, 29000, 31000));
//        lineData.put(2024, List.of(9000, 20000, 36000, 51000, 69000, 86000, 107000, 130000, 155000, 182000, 211000, 242000));
    }

    private String getMonth(int monthIndex) {
        return List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").get(monthIndex);
    }

    private void loadTooltip(){
        for (XYChart.Series<String, Number> series : barChart.getData()) {
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

        for (XYChart.Series<String, Number> series : lineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                // Create the tooltip
                Tooltip tooltip = new Tooltip("Month: " + data.getXValue() + "\nValue: " + data.getYValue());

                // Get the node (which may be null)
                Node node = data.getNode();

                // If the node is null, force the node creation
                if (node == null) {
                    node = new StackPane(); // Create a temporary node if it's null
                    data.setNode(node);     // Set it back to the data
                }
                //System.out.println("Tooltip: " + tooltip.getText());
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.setHideDelay(Duration.millis(50));
                // Install the tooltip
                Tooltip.install(node, tooltip);

                // Handle mouse events with a noticeable color change
                final Node finalNode = node; // Make the node reference final for lambda
                finalNode.setOnMouseEntered(e -> {
                    finalNode.setStyle("-fx-background-color: red;"); // Use a very obvious color
                });
                finalNode.setOnMouseExited(e -> {
                    finalNode.setStyle(""); // Reset style when mouse exits
                });
            }
        }

    }
}
