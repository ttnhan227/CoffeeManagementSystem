package controller.users.pages.orders;

import app.utils.HelperMethods;
import controller.UserSessionController;
import controller.users.UserMainDashboardController;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.*;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
    public TextField idField;
    public TextField productNameField;
    public Spinner<Integer> quantitySpinner;
    public TextField totalField;
    public HBox searchHBox;
    public Button addBtn;
    public TableView<Product> orderDetailView;
    //public TableColumn<Product, String> imageColumn;
    public TableColumn<Product, String> categoryColumn;
    public TableColumn<Product, String> nameColumn;
    public TableColumn<Product, Double> priceColumn;
    public TableColumn<Product, Integer> quantityColumn;
    public TableColumn<Product, Double> totalColumn;
    public HBox productHBox;
    public HBox paymentHBox;
    public Text totalText;
    public Text finalText;
    public Text discountText;
    public Button createOrderBtn;
    public CheckBox checkTakeAway;
    public TextField customerNameField;

    private UserMainDashboardController mainDashboardController;

    Text valid = new Text("valid");
    Text invalid = new Text("invalid");
    Text noStock = new Text("not enough stock");

    ObservableList<String> suggestions = FXCollections.observableArrayList(getProductNameList());

    //List<Product> productList = new ArrayList<>();
    ObservableList<Product> productList = FXCollections.observableArrayList();

    Product tempProduct = new Product();
    ObservableList<Integer> quantities = FXCollections.observableArrayList();
    ObservableList<Coupon> coupons = FXCollections.observableArrayList();

    Table order_table = new Table();
    Customer customer = new Customer();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userNameAndDateLoader();
        tableComboBoxLoader();
        couponLoader();
        suggestionListLoader();
        productDetailLoader();
        tableLoader();
        paymentBoxLoader();
        checkBoxLoader();
        setupCustomerSearch();

    }

    private void userNameAndDateLoader(){
        order_table = null;
        customer = null;
        tempProduct = null;
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
        tableComboBox.valueProperty().addListener((obs, oldVal, newVal) ->{
                if(newVal != null){
                    order_table = Datasource.getInstance().getOneTable(newVal);
                    checkTakeAway.setSelected(false);
                }
            }
        );
    }

    @FXML
    private boolean checkCoupon(){
        couponHBox.getChildren().remove(valid);
        couponHBox.getChildren().remove(invalid);
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
                    coupons.clear();
                    coupons.add(coupon);
                    return true;
                }
                else{
                    invalid.setText("coupon expired");
                    invalid.setFill(Color.RED);
                    invalid.setFont(new Font(15));
                    couponHBox.getChildren().add(invalid);
                    coupons.clear();
                    return false;
                }
            }
        }

        invalid.setText("invalid");
        invalid.setFill(Color.RED);
        invalid.setFont(new Font(15));
        couponHBox.getChildren().add(invalid);
        coupons.clear();
        return false;
    }

    @FXML
    private boolean resetCoupon(){
        couponHBox.getChildren().remove(valid);
        couponHBox.getChildren().remove(invalid);
        couponField.setText("");
        couponField.setEditable(true);
        coupons.clear();
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
            if(!product.isDisabled()){
                nameList.add(product.getName());
            }
        }
        return nameList;
    }

    private void couponLoader(){
        couponField.textProperty().addListener(((observableValue, oldValue, newValue) ->{
            couponHBox.getChildren().remove(valid);
            couponHBox.getChildren().remove(invalid);
        } ));
    }

    private void suggestionListLoader(){
        suggestionList.setVisible(false); // Initially hide the suggestions list
        //suggestionList.setPrefHeight(100); // Set a preferred height for the suggestion list
        suggestionList.prefWidthProperty().bind(searchField.widthProperty()); // Bind width to searchField
        //suggestionList.setItems(suggestions);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchHBox.getChildren().remove(invalid);
            if (newValue.isEmpty()) {
                suggestionList.setVisible(false);
            } else {
                // Filter suggestions
                List<String> filteredSuggestions = suggestions.stream()
                        .filter(s -> s.toLowerCase().contains(newValue.toLowerCase()))
                        .toList();
                ObservableList<String> inputList = FXCollections.observableArrayList(filteredSuggestions);
               // Integer value = quantitySpinner.getValue();
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
                    onClickSearch();
                });
            }
        });
    }

    @FXML
    private void onClickSearch(){
        if(productHBox.getChildren().contains(invalid) || productHBox.getChildren().contains(noStock)){
            productHBox.getChildren().remove(invalid);
            productHBox.getChildren().remove(noStock);
        }
        String searchName = searchField.getText();
        //Product product = new Product();
        tempProduct = Datasource.getInstance().searchOneProductByName(searchName);
        if(tempProduct != null && !tempProduct.isDisabled()){
            if(tempProduct.getQuantity() == 0){
                invalid.setText("Product has no stock remain");
                invalid.setFill(Color.RED);
                searchHBox.getChildren().add(invalid);
                return;
            }
            quantitySpinner.setDisable(false);
            idField.setText(String.valueOf(tempProduct.getId()));
            productNameField.setText(tempProduct.getName());
            SpinnerValueFactory<Integer> spinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, tempProduct.getQuantity(), 1);
            quantitySpinner.setValueFactory(spinnerValue);
            searchField.setText("");
            //return;
        }
        else if(tempProduct == null || tempProduct.isDisabled()){
            invalid.setText("No product found or product is disabled");
            invalid.setFill(Color.RED);
            searchHBox.getChildren().add(invalid);
            //return;
        }
    }

    private void productDetailLoader(){
        if(tempProduct == null){
            SpinnerValueFactory<Integer> spinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,999,0);
            quantitySpinner.setValueFactory(spinnerValue);
            totalField.setText("0");
            quantitySpinner.setDisable(true);
        }
        quantitySpinner.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if(tempProduct == null){
                    SpinnerValueFactory<Integer> spinnerValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,999,0);
                    quantitySpinner.setValueFactory(spinnerValue);
                    totalField.setText("0");
                    quantitySpinner.setDisable(true);
                }
                else{
                    double total = newValue * tempProduct.getPrice();
                    DecimalFormat formattedTotal = new DecimalFormat("#.##");
                    String formattedValue = formattedTotal.format(total);
                    totalField.setText(formattedValue);
                    quantitySpinner.setDisable(false);
                }
//                if(productHBox.getChildren().contains(invalid) || productHBox.getChildren().contains(noStock)){
//                    productHBox.getChildren().remove(invalid);
//                    productHBox.getChildren().remove(noStock);
//                }
            }
        });
    }

    private void tableLoader(){
        //imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(cellData -> {
            // Use the row index to get the corresponding quantity
            int index = orderDetailView.getItems().indexOf(cellData.getValue());
            return new SimpleIntegerProperty(getQuantity(index)).asObject();
        });
        totalColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            int index = orderDetailView.getItems().indexOf(product);
            double total = product.getPrice() * getQuantity(index);
            DecimalFormat format = new DecimalFormat("#.##");
            String formattedString = format.format(total);
            total = Double.parseDouble(formattedString);
            return new SimpleDoubleProperty(total).asObject();
        });
        categoryColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            String category = Datasource.getInstance().getCategoryName(product.getCategory_id());
            return new SimpleStringProperty(category);
        });
        addActionColumn();
    }

    @FXML
    private void addBtnClick(){
        if(tempProduct == null){
            return;
        }
        boolean check = false;
        for(Product product : productList){
            if(tempProduct.getName().equals(product.getName())){
                int index = productList.indexOf(product);
                quantities.set(index, quantities.get(index)+quantitySpinner.getValue());
                if(quantities.get(index) > product.getQuantity()){
                    quantities.set(index, product.getQuantity());
                    //invalid.setText("Not enough stock to add");
                    noStock.setFill(Color.RED);
                    productHBox.getChildren().add(noStock);
                }
                check = true;
            }
        }
        if(!check){
            productList.add(tempProduct);
            quantities.add(quantitySpinner.getValue());
        }
        orderDetailView.setItems(productList);
        tempProduct = null;
        idField.setText("");
        productNameField.setText("");
        SpinnerValueFactory<Integer> spinnerValue = new SpinnerValueFactory.ListSpinnerValueFactory<>(
                javafx.collections.FXCollections.observableArrayList(0));
        quantitySpinner.setValueFactory(spinnerValue);
        totalField.setText("0");
        orderDetailView.refresh();
    }

    private void addActionColumn(){
        TableColumn<Product, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                // Set the action for the Edit button
                editButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    System.out.println("Edit: " + product.getName());
                    //quantitySpinner.getValueFactory().setValue(quantities.get(getIndex()));
                    searchField.setText(product.getName());
                    onClickSearch();
                    Platform.runLater(() -> {
                        quantitySpinner.getValueFactory().setValue(quantities.get(getIndex()));
                        suggestionList.setVisible(false);
                        productList.remove(getIndex());
                        quantities.remove(getIndex());
                    });

                });

                // Set the action for the Delete button
                deleteButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    System.out.println("Delete: " + product.getName());
                    // Add your delete logic here
                    //getTableView().getItems().remove(product);
                    productList.remove(getIndex());
                    quantities.remove(getIndex());
                    //System.out.println(getIndex());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Add buttons to the cell
                    HBox hbox = new HBox(editButton, deleteButton);
                    setGraphic(hbox);
                }
            }
        });
        actionColumn.setMinWidth(200);
        actionColumn.setPrefWidth(300);
        actionColumn.setMaxWidth(5000);
        orderDetailView.getColumns().add(actionColumn);
    }

    private int getQuantity(int index) {
        return quantities.get(index);
    }

    private void paymentBoxLoader() {
        totalText.setText("");
        finalText.setText("0");
        discountText.setText("0");

        // Create a Text node for customer name
        Text customerLabel = new Text("Customer: ");
        customerLabel.setFont(new Font("System Bold", 15));
        Text customerNameText = new Text("No customer selected");
        customerNameText.setFont(new Font(15));

        // Add customer info to payment HBox
        HBox customerInfoBox = new HBox(5, customerLabel, customerNameText);
        customerInfoBox.setAlignment(javafx.geometry.Pos.CENTER);
        paymentHBox.getChildren().add(0, customerInfoBox);
        paymentHBox.setSpacing(20); // Add some spacing between elements

        ListChangeListener<Object> changeListener = change -> {
            while (change.next()) {
                Platform.runLater(() -> {
                    // Update customer name
                    if (customer != null && customer.getName() != null) {
                        customerNameText.setText(customer.getName());
                    } else {
                        customerNameText.setText("No customer selected");
                    }

                    double total = 0;
                    double fin = 0;
                    double temp = 0;
                    double discount = 0;
                    if (couponField.getText().isEmpty() || coupons.isEmpty()) {
                        discount = 0;
                    } else {
                        discount = (double) coupons.getFirst().getDiscount() / 100;
                    }
                    DecimalFormat format = new DecimalFormat("#.##");
                    for (int i = 0; i < productList.size(); i++) {
                        temp = productList.get(i).getPrice() * quantities.get(i);
                        total += temp;
                    }
                    fin = total * (1 - discount);

                    String formattedString = format.format(total);
                    if(discount != 0){
                        totalText.setText(formattedString);
                        totalText.setVisible(true);
                    }
                    else{
                        totalText.setText(formattedString);
                        totalText.setVisible(false);
                    }

                    formattedString = format.format(fin);
                    finalText.setText(String.valueOf(formattedString));
                    discountText.setText(discount * 100 + "%");
                });
            }
        };

        // Add listener for customer changes
        customerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (customer != null && customer.getName() != null) {
                    customerNameText.setText(customer.getName());
                } else {
                    customerNameText.setText("No customer selected");
                }
            });
        });

        productList.addListener(changeListener);
        quantities.addListener(changeListener);
        coupons.addListener(changeListener);
    }

    private void checkBoxLoader(){
        checkTakeAway.setOnAction(event -> {
            if (checkTakeAway.isSelected()) {
                //System.out.println("enabled");
                order_table = null;
                tableComboBox.setDisable(true);
                tableComboBox.setValue(null);
            } else {
                //System.out.println("disabled");
                tableComboBox.setDisable(false);
            }
        });
        Platform.runLater(() -> {
            checkTakeAway.setSelected(true);
        });

    }

    @FXML
    private void createClick() throws SQLException {
        Coupon coupon = new Coupon();
        double discount = 0;
        if(!coupons.isEmpty()){
            coupon = coupons.getFirst();
            discount = coupon.getDiscount();
        }
        else{
            coupon = null;
        }

        if(!productList.isEmpty()){
            Datasource.getInstance().createOrderWithDetails(
                    dateField.getText(),
                    customer,
                    order_table,
                    coupon,
                    productList,
                    quantities,
                    Double.parseDouble(totalText.getText()),
                    discount,
                    Double.parseDouble(finalText.getText())
            );
            System.out.println("ok");
            HelperMethods.alertBox("Insert Order to Database successfully", null, "Insert data");
            changeToOrderView();
            //return;
        }
        else{
            System.out.println("No product !");
        }
    }
    @FXML
    private void handleAddNewCustomer() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Add New Customer");
        dialog.setHeaderText("Enter customer details");

        // Create the custom dialog's content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField addressField = new TextField();
        TextField contactField = new TextField();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Contact:"), 0, 2);
        grid.add(contactField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nameField.getText().isEmpty() || addressField.getText().isEmpty() || contactField.getText().isEmpty()) {
                    HelperMethods.alertBox("All fields must be filled", null, "Validation Error");
                    return null;
                }
                
                boolean success = Datasource.getInstance().insertNewCustomer(
                    nameField.getText(),
                    addressField.getText(),
                    contactField.getText()
                );
                
                if (success) {
                    Customer newCustomer = Datasource.getInstance().getLastInsertedCustomer();
                    if (newCustomer != null) {
                        customerNameField.setText(newCustomer.getName());
                        customer = newCustomer;
                        return newCustomer;
                    }
                }
                HelperMethods.alertBox("Failed to add customer", null, "Error");
            }
            return null;
        });

        dialog.showAndWait();
    }
     private void setupCustomerSearch() {
        customerNameField.setOnKeyReleased(event -> {
            String searchText = customerNameField.getText().trim();
            if (!searchText.isEmpty()) {
                List<Customer> customers = Datasource.getInstance().searchCustomers(searchText, Datasource.ORDER_BY_NONE);
                if (customers != null && !customers.isEmpty()) {
                    // Show suggestions in a popup
                    ContextMenu contextMenu = new ContextMenu();
                    for (Customer c : customers) {
                        MenuItem item = new MenuItem(c.getName() + " - " + c.getContact_info());
                        item.setOnAction(e -> {
                            customerNameField.setText(c.getName());
                            customer = c;
                            contextMenu.hide();
                        });
                        contextMenu.getItems().add(item);
                    }
                    customerNameField.setContextMenu(contextMenu);
                    contextMenu.show(customerNameField, Side.BOTTOM, 0, 0);
                }
            }
        });
    }


    private void changeToOrderView(){
        mainDashboardController.btnOrdersOnClick(new ActionEvent());
    }

    public void setMainDashboardController(UserMainDashboardController controller){
        this.mainDashboardController = controller;
    }
}

