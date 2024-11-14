package controller.users.pages;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.Datasource;
import model.Table;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class tableController implements Initializable {
    
    @FXML
    private GridPane tableGrid;
    
    private Datasource datasource;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        datasource = Datasource.getInstance();
        if (!datasource.open()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Connection Failed");
            alert.setContentText("Could not connect to the database.");
            alert.showAndWait();
            return;
        }
        loadTables();
    }

    private void loadTables() {
        List<Integer> tableIds = datasource.getAllTableID();
        int row = 0;
        int col = 0;

        for (Integer tableId : tableIds) {
            Table table = datasource.getOneTable(tableId);
            if (table != null) {
                VBox tableCard = createTableCard(table);
                tableGrid.add(tableCard, col, row);
                
                // Adjust grid position
                col++;
                if (col > 3) { // 4 tables per row
                    col = 0;
                    row++;
                }
            }
        }
    }

    private VBox createTableCard(Table table) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-border-color: #cccccc; " +
                     "-fx-border-radius: 5; " +
                     "-fx-background-radius: 5; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Table number
        Label tableNumber = new Label("Table " + table.getId());
        tableNumber.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Capacity
        Label capacity = new Label("Capacity: " + table.getCapacity() + " people");
        capacity.setStyle("-fx-font-size: 14px;");

        // Status indicator
        Label status = new Label(table.getStatus() == 1 ? "Available" : "Occupied");
        status.setStyle("-fx-font-size: 14px; " +
                       "-fx-padding: 5 10; " +
                       "-fx-background-radius: 3; " +
                       "-fx-background-color: " + (table.getStatus() == 1 ? "#4CAF50" : "#F44336") + ";" +
                       "-fx-text-fill: white;");

        // Toggle button
        Button toggleButton = new Button(table.getStatus() == 1 ? "Set as Occupied" : "Set as Available");
        toggleButton.setStyle("-fx-background-color: " + (table.getStatus() == 1 ? "#F44336" : "#4CAF50") + ";" +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8 16;");

        toggleButton.setOnAction(e -> toggleTableStatus(table, status, toggleButton));

        card.getChildren().addAll(tableNumber, capacity, status, toggleButton);
        return card;
    }

    private void toggleTableStatus(Table table, Label statusLabel, Button toggleButton) {
        // Toggle the status
        int newStatus = table.getStatus() == 1 ? 0 : 1;
        
        // Try multiple times in case of database lock
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        
        while (!success && retryCount < maxRetries) {
            try {
                success = datasource.updateTableStatus(table.getId(), newStatus);
                if (success) {
                    break;
                }
                retryCount++;
                if (!success && retryCount < maxRetries) {
                    // Wait a bit before retrying
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (success) {
            // Only update UI if database update was successful
            table.setStatus(newStatus);
            
            // Update UI
            statusLabel.setText(newStatus == 1 ? "Available" : "Occupied");
            statusLabel.setStyle("-fx-font-size: 14px; " +
                               "-fx-padding: 5 10; " +
                               "-fx-background-radius: 3; " +
                               "-fx-background-color: " + (newStatus == 1 ? "#4CAF50" : "#F44336") + ";" +
                               "-fx-text-fill: white;");
            
            toggleButton.setText(newStatus == 1 ? "Set as Occupied" : "Set as Available");
            toggleButton.setStyle("-fx-background-color: " + (newStatus == 1 ? "#F44336" : "#4CAF50") + ";" +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 8 16;");
        } else {
            // Show error alert if update fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Update Failed");
            alert.setContentText("Failed to update table status after multiple attempts. Please try again.");
            alert.showAndWait();
        }
    }
}
