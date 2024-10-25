package controller.employee.pages;

import controller.UserSessionController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Coupon;
import model.Datasource;
import model.Product;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NewOrderController implements Initializable {
    public TextField eName;
    public TextField dateField;
    public ComboBox<Integer> tableComboBox;
    public HBox couponHBox;
    public TextField couponField;
    public Button checkCouponBtn;
    public Button resetCouponBtn;
    public TextField searchField;
    public ListView<String> suggestionList;

    Text valid = new Text("valid");
    Text invalid = new Text("invalid");

    ObservableList<String> suggestions = FXCollections.observableArrayList(getProductNameList());;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userNameAndDateLoader();
        tableComboBoxLoader();
        couponLoader();
        suggestionListLoader();
    }

    private void userNameAndDateLoader(){
        String username = UserSessionController.getUserFullName();
        eName.setText(username);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formatted_date = today.format(formatter);
        dateField.setText(formatted_date);
    }

    private void tableComboBoxLoader(){
        List<Integer> list = Datasource.getInstance().getAllTableID();
        ObservableList<Integer> options = FXCollections.observableArrayList(list);
        tableComboBox.setItems(options);
    }

    @FXML
    private boolean checkCoupon(){
        if(couponHBox.getChildren().contains(valid)){
            couponHBox.getChildren().remove(valid);
        }
        if(couponHBox.getChildren().contains(invalid)){
            couponHBox.getChildren().remove(invalid);
        }
        List<Coupon> list = Datasource.getInstance().getAllCoupon();
        String cf = couponField.getText();
        for(Coupon coupon: list){
            if(String.valueOf(coupon.getId()).equals(cf)){
                if(!checkExpire(coupon.getExpiry())){
                    valid.setText("valid");
                    valid.setFill(Color.GREEN);
                    valid.setFont(new Font(15));
                    couponHBox.getChildren().add(valid);
                    couponField.setEditable(false);
                    return true;
                }
                else{
                    invalid.setText("coupon expired");
                    invalid.setFill(Color.RED);
                    invalid.setFont(new Font(15));
                    couponHBox.getChildren().add(invalid);
                    return false;
                }
            }
        }

        invalid.setText("invalid");
        invalid.setFill(Color.RED);
        invalid.setFont(new Font(15));
        couponHBox.getChildren().add(invalid);
        return false;
    }

    @FXML
    private boolean resetCoupon(){
        if(couponHBox.getChildren().contains(valid)){
            couponHBox.getChildren().remove(valid);
        }
        if(couponHBox.getChildren().contains(invalid)){
            couponHBox.getChildren().remove(invalid);
        }
        couponField.setText("");
        couponField.setEditable(true);
        return true;
    }

    private boolean checkExpire(String expiry){
        //System.out.println(expiry);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate expiryDate = LocalDate.parse(expiry, formatter);
        LocalDate today = LocalDate.now();
        return expiryDate.isBefore(today);
        //return false;
    }

    private List<String> getProductNameList(){
        List<Product> list = Datasource.getInstance().getAllProducts(Datasource.ORDER_BY_NONE);
        List<String> nameList = new ArrayList<>();
        for(Product product: list){
            nameList.add(product.getName());
        }
        return nameList;
    }

    private void couponLoader(){
        couponField.textProperty().addListener(((observableValue, oldValue, newValue) ->{
            if(couponHBox.getChildren().contains(valid)){
                couponHBox.getChildren().remove(valid);
            }
            if(couponHBox.getChildren().contains(invalid)){
                couponHBox.getChildren().remove(invalid);
            }
        } ));
    }

    private void suggestionListLoader(){
        suggestionList.setVisible(false); // Initially hide the suggestions list
        //suggestionList.setPrefHeight(100); // Set a preferred height for the suggestion list
        suggestionList.prefWidthProperty().bind(searchField.widthProperty()); // Bind width to searchField
        //suggestionList.setItems(suggestions);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                suggestionList.setVisible(false);
            } else {
                // Filter suggestions
                List<String> filteredSuggestions = suggestions.stream()
                        .filter(s -> s.toLowerCase().contains(newValue.toLowerCase()))
                        .toList();
                ObservableList<String> inputList = FXCollections.observableArrayList(filteredSuggestions);

                if (!filteredSuggestions.isEmpty()) {
                    suggestionList.setItems(inputList);
                    suggestionList.setVisible(true);
                } else {
                    suggestionList.setVisible(false);
                }

                // Update position of the suggestion list to be directly below the searchField
                //suggestionList.setTranslateX(searchField.getLayoutX());
                //suggestionList.setTranslateY(searchField.getLayoutY() + searchField.getHeight());
            }
        });

        // Handle selection from the suggestions list
        suggestionList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                searchField.setText(newValue);
                searchField.positionCaret(searchField.getText().length());
                suggestionList.setVisible(false);// Hide suggestions after selection

                Platform.runLater(() -> {
                    suggestionList.getSelectionModel().clearSelection();
                });
            }
        });
    }
}
