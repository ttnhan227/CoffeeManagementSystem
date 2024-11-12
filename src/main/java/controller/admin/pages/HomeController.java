package controller.admin.pages;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Datasource;
import model.OrderDetail;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeController {

    @FXML
    public Label productsCount;
    @FXML
    public Label customersCount;
    @FXML
    private TableView<OrderDetail> bestSellingTable;

    @FXML
    public void initialize() {
        setupBestSellingTable();
        getDashboardProdCount();
        getDashboardCostCount();
        loadBestSellingProducts();
        
        // Set fixed height for the table
        bestSellingTable.setFixedCellSize(50);
        bestSellingTable.setPrefHeight(200); // Height for 3 rows + header + padding
        bestSellingTable.setMaxHeight(200); // Prevent table from growing
        bestSellingTable.setMinHeight(200); // Prevent table from shrinking
        
        // Prevent table from showing empty rows
        bestSellingTable.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-table-cell-border-color: transparent;"
        );
    }

    private void setupBestSellingTable() {
        // Create columns
        TableColumn<OrderDetail, String> productColumn = new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<OrderDetail, Integer> quantityColumn = new TableColumn<>("Quantity Sold");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<OrderDetail, Double> totalColumn = new TableColumn<>("Total Revenue");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

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

        // Set column widths
        productColumn.prefWidthProperty().bind(bestSellingTable.widthProperty().multiply(0.4));
        quantityColumn.prefWidthProperty().bind(bestSellingTable.widthProperty().multiply(0.3));
        totalColumn.prefWidthProperty().bind(bestSellingTable.widthProperty().multiply(0.3));

        // Add columns to table
        bestSellingTable.getColumns().addAll(productColumn, quantityColumn, totalColumn);
        
        // Style the table header
        bestSellingTable.getStylesheets().add(getClass().getResource("/view/resources/css/table-style.css").toExternalForm());
    }

    private void loadBestSellingProducts() {
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

    public void getDashboardCostCount() {
        Task<Integer> getDashCostCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllCustomers();
            }
        };

        getDashCostCount.setOnSucceeded(e -> {
            customersCount.setText(String.valueOf(getDashCostCount.valueProperty().getValue()));
        });

        new Thread(getDashCostCount).start();
    }
}
