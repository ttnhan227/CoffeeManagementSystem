package controller.admin.pages.products;
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

import java.io.File;
import java.net.URL;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                System.out.println("Resources directory exists at: " + resourceUrl);
                File resourceDir = new File(resourceUrl.toURI());
                if (resourceDir.exists() && resourceDir.isDirectory()) {
                    System.out.println("Contents of image directory:");
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
                        Product product = (Product) getTableRow().getItem();
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

    @FXML
    private void btnProductsSearchOnAction() {
        Task<ObservableList<Product>> searchProductsTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().searchProducts(
                                fieldProductsSearch.getText().toLowerCase(),
                                Datasource.ORDER_BY_NONE,
                                false // Set to true if you want to include disabled products
                        ));
            }
        };

        searchProductsTask.setOnSucceeded(e -> {
            productsContainer.getChildren().clear();
            ObservableList<Product> products = searchProductsTask.getValue();
            for (Product product : products) {
                try {
                    addProductCard(product);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        new Thread(searchProductsTask).start();
    }



    @FXML
    private void btnAddProductOnClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/products/add-product.fxml"));
            AnchorPane root = fxmlLoader.load();

            URL cssUrl = getClass().getResource("/css/form.css");
            if (cssUrl != null) {
                root.getStylesheets().add(cssUrl.toExternalForm());
            }

            productsContent.getChildren().clear();
            productsContent.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnEditProduct(int product_id) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/products/edit-product.fxml"));
            AnchorPane root = fxmlLoader.load();

            URL cssUrl = getClass().getResource("/css/form.css");
            if (cssUrl != null) {
                root.getStylesheets().add(cssUrl.toExternalForm());
            }

            productsContent.getChildren().clear();
            productsContent.getChildren().add(root);

            EditProductController controller = fxmlLoader.getController();
            controller.fillEditingProductFields(product_id);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    boolean areProductInputsValid(String fieldAddProductName, String fieldAddProductDescription, String fieldAddProductPrice, String fieldAddProductQuantity, int productCategoryId) {
        System.out.println("TODO: Better validate inputs.");
        String errorMessage = "";

        if (fieldAddProductName == null || fieldAddProductName.length() < 3) {
            errorMessage += "please enter a valid name!\n";
        }
        if (fieldAddProductDescription == null || fieldAddProductDescription.length() < 5) {
            errorMessage += "Description is not valid!\n";
        }
        if (!HelperMethods.validateProductPrice(fieldAddProductPrice)) {
            errorMessage += "Price is not valid!\n";
        }
        if (!HelperMethods.validateProductQuantity(fieldAddProductQuantity)) {
            errorMessage += "Not valid quantity!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    @FXML
    public void btnManageCategoryOnClick(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/admin/pages/products/categories.fxml"));
            AnchorPane root = fxmlLoader.load();
            productsContent.getChildren().clear();
            productsContent.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}