package controller.admin.pages;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;
import java.util.ResourceBundle;

import app.utils.HelperMethods;
import controller.admin.MainDashboardController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Coupon;
import model.Datasource;

public class CouponController implements Initializable {

    public DatePicker expiryPicker;
    public TextField discountField;
    public Button generateBtn;
    public Label statusLabel;
    public TableView<Coupon> tableView = new TableView<>();
    public TableColumn<Coupon, Integer> idColumn = new TableColumn<>("ID");
    public TableColumn<Coupon, Integer> discountColumn = new TableColumn<>("Discount(%)");
    public TableColumn<Coupon, String> dateColumn = new TableColumn<>("Expiry");
    @FXML
    private TextField searchField;

    private ObservableList<Coupon> list;
    private ObservableList<Coupon> filteredList = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private MainDashboardController mainDashboardController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabel.setText("");
        loadDatePicker();
        loadTableView();
        
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilter(newText));
    }

    private void loadDatePicker(){
        expiryPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (DateTimeParseException e) {
                        //statusLabel.setText("Invalid date format, please use dd-MM-yyyy.");
                        return null;
                    }
                }
                return null;
            }
        });

        expiryPicker.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateDateInput();
            }
            else {
                //expiryPicker.setValue(null);
                statusLabel.setText("");
            }
        });

        discountField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if(isFocused){
                if(statusLabel.getText().equals("Please fill in all details.") ||
                        statusLabel.getText().equals("Discount must be > 0 and <= 80.") ||
                        statusLabel.getText().equals("Discount must be a valid integer.")){
                    statusLabel.setText("");
                }
            }
        });
    }

    private boolean validateDateInput() {
        String inputDate = expiryPicker.getEditor().getText();
        try {
            LocalDate parsedDate = LocalDate.parse(inputDate, dateFormatter);
            expiryPicker.setValue(parsedDate);
            statusLabel.setText("");
            return true;
        } catch (DateTimeParseException e) {
            //expiryPicker.setValue(null);
            statusLabel.setText("Invalid date format, please use dd-MM-yyyy.");
            return false;
        }
    }

    @FXML
    private void generateCoupon(){
        LocalDate expiryDate = expiryPicker.getValue();
        String discountStr = discountField.getText();

        if(!validateDateInput()){
            return;
        }

        if (expiryDate == null || discountStr.isEmpty()) {
            statusLabel.setText("Please fill in all details.");
            return;
        }

        if (expiryDate.isBefore(LocalDate.now())) {
            statusLabel.setText("Expiry date must be in the future.");
            return;
        }

        try {
            int discount = Integer.parseInt(discountStr);

            // Check if discount is between 1 and 79
            if (discount <= 0 || discount > 80) {
                statusLabel.setText("Discount must be > 0 and <= 80.");
                return;
            }

            int couponId = generateUnique8DigitId();
            Datasource.getInstance().saveToDatabase(couponId, expiryDate.format(dateFormatter), discount);

            statusLabel.setText("Coupon saved with ID: " + couponId);
            HelperMethods.alertBox("Insert coupon to Database successfully\nNew coupon id: " + couponId, "", "DB");
            //mainDashboardController.btnHomeOnClick(new ActionEvent());
            list = FXCollections.observableArrayList(Datasource.getInstance().getAllCoupon());
//            filteredList.addAll(list);
//            tableView.setItems(filteredList);
            applyFilter("");
        } catch (NumberFormatException e) {
            statusLabel.setText("Discount must be a valid integer.");
        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
        }
    }

    private int generateUnique8DigitId() throws SQLException {
        Random random = new Random();
        int couponId;

        do {
            couponId = 10000000 + random.nextInt(90000000);  // Generates an 8-digit ID between 10000000 and 99999999
        } while (Datasource.getInstance().isExistCouponId(couponId));

        return couponId;
    }

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }

    private void loadTableView(){
        list = FXCollections.observableArrayList(Datasource.getInstance().getAllCoupon());
        filteredList.addAll(list);
        
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("expiry"));
        
        idColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.33));
        discountColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.33));
        dateColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.33));
        
        tableView.getColumns().addAll(idColumn, discountColumn, dateColumn);
        tableView.setItems(filteredList);
    }

    private void applyFilter(String search){
        filteredList.clear();
        if(search.isEmpty()){
            filteredList.addAll(list);
            return;
        }
        filteredList.addAll(list.filtered(coupon -> String.valueOf(coupon.getId()).contains(search)));
    }
}
