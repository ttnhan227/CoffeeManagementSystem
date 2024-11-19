package controller.admin.pages.products;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import app.utils.HelperMethods;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import model.Datasource;
import model.Product;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import java.io.File;
import java.net.URL;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ProductsController {

    @FXML
    public TextField fieldProductsSearch;
    @FXML
    public Text viewProductResponse;
    @FXML
    public GridPane formEditProductView;
    @FXML
    private FlowPane productsContainer;
    @FXML
    private StackPane productsContent;
    @FXML
    private TableView<Product> tableProductsPage;
    private TableColumn<Product, Void> colBtnEdit;
    @FXML
    private Button toggleStatusButton;
    private ObservableList<Product> allProducts;
    private ObservableList<Product> filteredProducts;

    public static TextFormatter<Double> formatDoubleField() {
        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");
        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c;
            } else {
                return null;
            }
        };
        StringConverter<Double> converter = new StringConverter<Double>() {
            @Override
            public Double fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0.0;
                } else {
                    return Double.valueOf(s);
                }
            }

            @Override
            public String toString(Double d) {
                return d.toString();
            }
        };

        return new TextFormatter<>(converter, 0.0, filter);
    }

    public static TextFormatter<Integer> formatIntField() {
        Pattern validEditingState = Pattern.compile("^[0-9]+$");
        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c;
            } else {
                return null;
            }
        };
        StringConverter<Integer> converter = new StringConverter<Integer>() {
            @Override
            public Integer fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0;
                } else {
                    return Integer.valueOf(s);
                }
            }

            @Override
            public String toString(Integer d) {
                return d.toString();
            }
        };

        return new TextFormatter<>(converter, 0, filter);
    }

    public static final Image DEFAULT_IMAGE = new Image(
            ProductsController.class.getResourceAsStream("/view/resources/img/coffee_pictures/placeholder.png"),
            250, 250, true, true
    );

    @FXML
    private void initialize() {
        System.out.println("Project Directory: " + System.getProperty("user.dir"));

        try {
            URL resourceUrl = getClass().getResource("/view/resources/img/coffee_pictures/");
            if (resourceUrl != null) {
                File resourceDir = new File(resourceUrl.toURI());
                if (resourceDir.exists() && resourceDir.isDirectory()) {
                    for (File file : resourceDir.listFiles()) {
                        System.out.println(" - " + file.getName());
                    }
                }
            } else {
                System.out.println("Resources directory not found!");
            }
        } catch (Exception e) {
            System.err.println("Error checking resources:");
            e.printStackTrace();
        }

        setupImageColumn();
        setupSearch();
        listProducts();
    }

    private void setupImageColumn() {
        TableColumn<Product, ImageView> imageColumn = (TableColumn<Product, ImageView>) tableProductsPage.getColumns()
                .stream()
                .filter(col -> col.getText().equals("Image"))
                .findFirst()
                .orElse(null);

        if (imageColumn != null) {
            imageColumn.setCellValueFactory(null);
            imageColumn.setCellFactory(col -> new TableCell<Product, ImageView>() {
                @Override
                protected void updateItem(ImageView item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Product product = getTableRow().getItem();
                        if (product.getImageView() != null) {
                            setGraphic(product.getImageView());
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }
    }

    @FXML
    public void listProducts() {
        Task<ObservableList<Product>> getAllProductsTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                return FXCollections.observableArrayList(Datasource.getInstance().getAllProducts(Datasource.ORDER_BY_CREATION_DATE_DESC)); // Order by newest first
            }
        };

        getAllProductsTask.setOnSucceeded(e -> {
            productsContainer.getChildren().clear();
            ObservableList<Product> products = getAllProductsTask.getValue();
            // Add products in reverse order to show newest first
            for (int i = products.size() - 1; i >= 0; i--) {
                try {
                    addProductCard(products.get(i));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        new Thread(getAllProductsTask).start();
    }

    private void addProductCard(Product product) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin/pages/products/product-card.fxml"));
        VBox productCard = loader.load();

        // Get UI elements
        ImageView productImage = (ImageView) productCard.lookup("#productImage");
        Text productName = (Text) productCard.lookup("#productName");
        Text productCategory = (Text) productCard.lookup("#productCategory");
        Text productPrice = (Text) productCard.lookup("#productPrice");
        Text productStock = (Text) productCard.lookup("#productStock");
        Button editButton = (Button) productCard.lookup("#editButton");
        Button toggleStatusButton = (Button) productCard.lookup("#toggleStatusButton");

        productImage.setOnMouseClicked(event -> showProductDescription(product));
        updateProductCardStatus(product, productCard, productName, toggleStatusButton);

        // Toggle status button action
        toggleStatusButton.setOnAction(event -> {
            product.setDisabled(!product.isDisabled());
            if (Datasource.getInstance().updateProductStatus(product.getId(), product.isDisabled())) {
                updateProductCardStatus(product, productCard, productName, toggleStatusButton);
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update the product status. Please try again.").showAndWait();
            }
        });

        // Load image asynchronously
        Task<Image> loadImageTask = new Task<Image>() {
            @Override
            protected Image call() {
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    try {
                        URL resourceUrl = getClass().getResource(product.getImage());
                        if (resourceUrl != null) {
                            return new Image(resourceUrl.toString(), 350, 250, false, true);
                        }
                        Path absolutePath = Paths.get(System.getProperty("user.dir"), "src/main/resources" + product.getImage());
                        if (Files.exists(absolutePath)) {
                            return new Image(absolutePath.toUri().toString(), 350, 250, false, true);
                        }
                    } catch (Exception ignored) {}
                }
                return DEFAULT_IMAGE;
            }
        };

        loadImageTask.setOnSucceeded(event -> {
            productImage.setImage(loadImageTask.getValue());
            productImage.setFitWidth(350);
            productImage.setFitHeight(250);
            productImage.setPreserveRatio(false);
        });

        loadImageTask.setOnFailed(event -> productImage.setImage(DEFAULT_IMAGE));
        new Thread(loadImageTask).start();

        // Set product information
        productName.setText(product.getName());
        productCategory.setText(product.getCategory_name());
        productPrice.setText(String.format("$%.2f", product.getPrice()));
        productStock.setText(String.format("Stock: %d", product.getQuantity()));
        productCard.setStyle(productCard.getStyle() + "; -fx-cursor: hand;");

        // Set up edit button action
        editButton.setOnAction(event -> btnEditProduct(product.getId()));

        productsContainer.getChildren().add(productCard);
    }

    private void updateProductCardStatus(Product product, VBox productCard, Text productName, Button toggleStatusButton) {
        if (product.isDisabled()) {
            productCard.getStyleClass().add("disabled");
            productName.getStyleClass().add("unavailable");
            productName.setText(product.getName() + " (Unavailable)");
            toggleStatusButton.setText("Enable");
            toggleStatusButton.getStyleClass().remove("warning");
            toggleStatusButton.getStyleClass().add("enable");
        } else {
            productCard.getStyleClass().remove("disabled");
            productName.getStyleClass().remove("unavailable");
            productName.setText(product.getName());
            toggleStatusButton.setText("Disable");
            toggleStatusButton.getStyleClass().remove("enable");
            toggleStatusButton.getStyleClass().add("warning");
        }
    }

    private void showProductDescription(Product product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(product.getName() + " Description");
        alert.setHeaderText(null);
        alert.setContentText(product.getDescription());
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    private void setupSearch() {
        fieldProductsSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                listProducts();
            } else {
                filterProducts(newValue.trim().toLowerCase());
            }
        });
    }

    private void filterProducts(String searchText) {
        Task<Void> searchTask = new Task<>() {
            @Override
            protected Void call() {
                List<Product> searchResults = Datasource.getInstance().searchProducts(
                        searchText,
                        Datasource.ORDER_BY_NONE,
                        false // Set to true if you want to include disabled products
                );
                
                Platform.runLater(() -> {
                    productsContainer.getChildren().clear();
                    for (Product product : searchResults) {
                        try {
                            addProductCard(product);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                
                return null;
            }
        };

        new Thread(searchTask).start();
    }

    @FXML
    private void btnAddProductOnClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/products/add-product.fxml"));
            AnchorPane root = fxmlLoader.load();

            // Create new stage for popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Add New Product");

            // Apply CSS
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/css/form.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            popupStage.setScene(scene);

            // Get the controller and set up callback for refresh
            AddProductController controller = fxmlLoader.getController();
            controller.setOnProductAdded(() -> {
                listProducts();
                popupStage.close();
            });

            // Show the popup
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnEditProduct(int product_id) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/products/edit-product.fxml"));
            AnchorPane root = fxmlLoader.load();

            // Create new stage for popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Edit Product");

            // Apply CSS
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/css/form.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            popupStage.setScene(scene);

            // Get the controller and set up callback for refresh
            EditProductController controller = fxmlLoader.getController();
            controller.setOnProductEdited(() -> {
                listProducts();
                popupStage.close();
            });
            
            // Fill the form with product data
            controller.fillEditingProductFields(product_id);

            // Show the popup
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean areProductInputsValid(String name, String description, String price, String quantity, int categoryId) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            displayAlert("Product name is required.", "error");
            return false;
        } else if (name.length() < 3 || name.length() > 50) {
            displayAlert("Product name must be between 3 and 50 characters.", "error");
            return false;
        }

        // Check if product name exists
        if (Datasource.getInstance().isProductNameExists(name)) {
            displayAlert("Product name already exists.", "error");
            return false;
        }

        // Validate description
        if (description == null || description.trim().isEmpty()) {
            displayAlert("Product description is required.", "error");
            return false;
        } else if (description.length() < 10 || description.length() > 500) {
            displayAlert("Description must be between 10 and 500 characters.", "error");
            return false;
        }

        // Validate price
        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0 || priceValue > 100) { // Changed to > instead of >=
                displayAlert("Price must be between $0.01 and $100.00.", "error");
                return false;
            }
        } catch (NumberFormatException e) {
            displayAlert("Invalid price format.", "error");
            return false;
        }

        // Validate quantity
        try {
            int quantityValue = Integer.parseInt(quantity);
            if (quantityValue <= 0 || quantityValue > 1000) { // Changed to > instead of >=
                displayAlert("Quantity must be between 1 and 1000.", "error");
                return false;
            }
        } catch (NumberFormatException e) {
            displayAlert("Invalid quantity format.", "error");
            return false;
        }

        // Validate category
        if (categoryId <= 0) {
            displayAlert("Please select a category.", "error");
            return false;
        }

        return true;
    }

    // Add this method to be used by child classes
    protected void displayAlert(String message, String type) {
        // This will be overridden in child classes
    }

    @FXML
    public void btnManageCategoryOnClick(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/products/categories.fxml"));
            AnchorPane root = fxmlLoader.load();

            // Create new stage for popup
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Manage Categories");

            // Apply CSS
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/css/form.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}