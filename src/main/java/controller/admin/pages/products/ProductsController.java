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
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Datasource;
import model.Product;
import java.io.File;
import java.net.URL;
import java.io.IOException;
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
    private TableView<Product> tableProductsPage;
    private TableColumn<Product, Void> colBtnEdit;

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
//        Pattern validEditingState = Pattern.compile("-?(0|[1-9]\\d*)");
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
    private static final Image DEFAULT_IMAGE = new Image(
            ProductsController.class.getResourceAsStream("/view/resources/img/coffee_pictures/placeholder.png"),
            250, 250, true, true
    );

    @FXML
    private void initialize() {
        // Add this debug code
        System.out.println("Project Directory: " + System.getProperty("user.dir"));

        // Print out the available resources
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
        // Find the image column by its text/title
        TableColumn<Product, ImageView> imageColumn = (TableColumn<Product, ImageView>) tableProductsPage.getColumns()
                .stream()
                .filter(col -> col.getText().equals("Image"))
                .findFirst()
                .orElse(null);

        if (imageColumn != null) {
            // Clear the existing cell value factory
            imageColumn.setCellValueFactory(null);

            // Set up a custom cell factory for the image column
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
                return FXCollections.observableArrayList(Datasource.getInstance().getAllProducts(Datasource.ORDER_BY_NONE));
            }
        };

        getAllProductsTask.setOnSucceeded(e -> {
            productsContainer.getChildren().clear();
            ObservableList<Product> products = getAllProductsTask.getValue();
            for (Product product : products) {
                try {
                    addProductCard(product);
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

        // Set product data
        ImageView productImage = (ImageView) productCard.lookup("#productImage");
        Text productName = (Text) productCard.lookup("#productName");
        Text productCategory = (Text) productCard.lookup("#productCategory");
        Text productPrice = (Text) productCard.lookup("#productPrice");
        Text productStock = (Text) productCard.lookup("#productStock");
        Button editButton = (Button) productCard.lookup("#editButton");
        Button deleteButton = (Button) productCard.lookup("#deleteButton");

        // Handle image setting with consistent size
        if (product.getImageView() != null && product.getImageView().getImage() != null) {
            Image originalImage = product.getImageView().getImage();
            // Create a new image with desired dimensions while maintaining aspect ratio
            Image resizedImage = new Image(
                    originalImage.getUrl(),
                    250, 250,  // Width and height
                    false,     // Preserve ratio
                    true      // Smooth
            );
            productImage.setImage(resizedImage);
        } else {
            productImage.setImage(DEFAULT_IMAGE);
        }

        // Ensure the ImageView maintains consistent size
        productImage.setFitWidth(250);
        productImage.setFitHeight(250);
        productImage.setPreserveRatio(false);

        productName.setText(product.getName());
        productCategory.setText(product.getCategory_name());
        productPrice.setText(String.format("$%.2f", product.getPrice()));
        productStock.setText(String.format("Stock: %d", product.getQuantity()));

        // Set up button actions
        editButton.setOnAction(event -> btnEditProduct(product.getId()));
        deleteButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Are you sure you want to delete " + product.getName() + "?");
            alert.setTitle("Delete " + product.getName() + "?");
            Optional<ButtonType> deleteConfirmation = alert.showAndWait();

            if (deleteConfirmation.isPresent() && deleteConfirmation.get() == ButtonType.OK) {
                if (Datasource.getInstance().deleteSingleProduct(product.getId())) {
                    productsContainer.getChildren().remove(productCard);
                }
            }
        });

        productsContainer.getChildren().add(productCard);
    }
    @FXML
    private void addActionButtonsToTable() {
        if (colBtnEdit == null) { // Check if the column is already created
            colBtnEdit = new TableColumn<>("Actions");

            Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory = param -> new TableCell<Product, Void>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox buttonsPane = new HBox();

                {
                    editButton.getStyleClass().addAll("button", "xs", "primary");
                    editButton.setOnAction((ActionEvent event) -> {
                        Product productData = getTableView().getItems().get(getIndex());
                        btnEditProduct(productData.getId());
                    });

                    deleteButton.getStyleClass().addAll("button", "xs", "danger");
                    deleteButton.setOnAction((ActionEvent event) -> {
                        Product productData = getTableView().getItems().get(getIndex());
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setHeaderText("Are you sure you want to delete " + productData.getName() + "?");
                        alert.setTitle("Delete " + productData.getName() + "?");
                        Optional<ButtonType> deleteConfirmation = alert.showAndWait();

                        if (deleteConfirmation.isPresent() && deleteConfirmation.get() == ButtonType.OK) {
                            if (Datasource.getInstance().deleteSingleProduct(productData.getId())) {
                                getTableView().getItems().remove(getIndex());
                            }
                        }
                    });

                    buttonsPane.setSpacing(10);
                    buttonsPane.getChildren().addAll(editButton, deleteButton);
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttonsPane);
                }
            };

            colBtnEdit.setCellFactory(cellFactory);
            tableProductsPage.getColumns().add(colBtnEdit);
        }
    }
    @FXML
    private void btnProductsSearchOnAction() {
        Task<ObservableList<Product>> searchProductsTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().searchProducts(fieldProductsSearch.getText().toLowerCase(), Datasource.ORDER_BY_NONE));
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
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.load(getClass().getResource("/view/admin/pages/products/add-product.fxml").openStream());
            AnchorPane root = fxmlLoader.getRoot();
            productsContent.getChildren().clear();
            productsContent.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void btnEditProduct(int product_id) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.load(getClass().getResource("/view/admin/pages/products/edit-product.fxml").openStream());
            AnchorPane root = fxmlLoader.getRoot();
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
        // TODO
        //  Better validate inputs.
        System.out.println("TODO: Better validate inputs.");
        String errorMessage = "";

        if (productCategoryId == 0) {
            errorMessage += "Not valid category id!\n";
        }
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
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }

    }
}