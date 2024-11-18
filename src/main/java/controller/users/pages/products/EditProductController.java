package controller.users.pages.products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.Categories;
import model.Datasource;
import model.Product;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EditProductController extends ProductsController {

    @FXML
    public Text viewProductResponse;
    public TextField fieldEditProductName;
    public TextField fieldEditProductPrice;
    public TextField fieldEditProductQuantity;
    public ComboBox<Categories> fieldEditProductCategoryId;
    public TextArea fieldEditProductDescription;
    public TextField fieldEditProductId;
    public Text viewProductName;
    public ImageView productImageView; // New
    public Button selectImageButton; // New

    private File selectedImageFile; // New
    private static final String IMAGE_UPLOAD_PATH = "/view/resources/img/coffee_pictures/"; // New

    private Runnable onProductEdited;

    @FXML private VBox alertContainer;
    @FXML private Label alertMessage;

    public void setOnProductEdited(Runnable callback) {
        this.onProductEdited = callback;
    }

    @FXML
    private void initialize() {
        fieldEditProductCategoryId.setItems(FXCollections.observableArrayList(Datasource.getInstance().getProductCategories(Datasource.ORDER_BY_ASC)));

        // Update price formatter to allow exactly 100
        TextFormatter<Double> priceFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            try {
                double value = Double.parseDouble(change.getControlNewText());
                return value >= 0 && value <= 100.0 ? change : null;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        // Update quantity formatter to allow exactly 1000
        TextFormatter<Integer> quantityFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }
            try {
                int value = Integer.parseInt(change.getControlNewText());
                return value >= 0 && value <= 1000 ? change : null;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        fieldEditProductPrice.setTextFormatter(priceFormatter);
        fieldEditProductQuantity.setTextFormatter(quantityFormatter);

        // Initialize image view with placeholder
        productImageView.setFitHeight(200);
        productImageView.setFitWidth(200);
        productImageView.setPreserveRatio(true);
        productImageView.setImage(DEFAULT_IMAGE);

        // Initialize alert container
        alertContainer.setManaged(false);
        alertContainer.setVisible(false);
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        selectedImageFile = fileChooser.showOpenDialog(selectImageButton.getScene().getWindow());
        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            productImageView.setImage(image);
        }
    }

    private String saveImageFile() {
        if (selectedImageFile == null) {
            return null;
        }

        try {
            String fileName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
            String relativePath = "/view/resources/img/coffee_pictures/" + fileName;
            Path destinationPath = Paths.get(System.getProperty("user.dir"), "src/main/resources" + relativePath);

            // Create directories if they don't exist
            Files.createDirectories(destinationPath.getParent());

            // Copy the file
            Files.copy(selectedImageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            return relativePath;  // Return the relative path instead of just IMAGE_UPLOAD_PATH + fileName
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void btnEditProductOnAction() {
        Categories category = fieldEditProductCategoryId.getSelectionModel().getSelectedItem();
        final int cat_id = category != null ? category.getId() : 0;

        // Trim input values to remove leading/trailing whitespace
        final String productName = fieldEditProductName.getText().trim();
        final String productDescription = fieldEditProductDescription.getText().trim();
        final String productPrice = fieldEditProductPrice.getText().trim();
        final String productQuantity = fieldEditProductQuantity.getText().trim();
        final int productId = Integer.parseInt(fieldEditProductId.getText());

        // Special validation for edit - don't check name if it hasn't changed
        Product currentProduct = Datasource.getInstance().getOneProduct(productId);
        boolean isNameUnchanged = currentProduct != null && currentProduct.getName().equals(productName);

        if (areProductInputsValidForEdit(productName, productDescription, productPrice, productQuantity, cat_id, isNameUnchanged)) {
            final double price = Double.parseDouble(productPrice);
            final int quantity = Integer.parseInt(productQuantity);
            final String newImagePath = saveImageFile();
            final String finalImagePath = (newImagePath != null) ? newImagePath : getCurrentImagePath(productId);
            final boolean isEnabled = true;

            Task<Boolean> editProductTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return Datasource.getInstance().updateOneProduct(
                            productId, productName, productDescription,
                            price, quantity, cat_id, finalImagePath, isEnabled);
                }
            };

            editProductTask.setOnSucceeded(e -> {
                if (editProductTask.valueProperty().get()) {
                    displayAlert("Product updated successfully!", "success");
                    if (onProductEdited != null) {
                        onProductEdited.run();
                    }
                } else {
                    displayAlert("Failed to update product. Please try again.", "error");
                }
            });

            new Thread(editProductTask).start();
        }
    }

    // Add this new method for edit-specific validation
    protected boolean areProductInputsValidForEdit(String name, String description,
                                                   String price, String quantity, int categoryId, boolean isNameUnchanged) {

        // Check if product name already exists (only if name has changed)
        if (!isNameUnchanged && Datasource.getInstance().isProductNameExists(name)) {
            displayAlert("Product name already exists.", "error");
            return false;
        }

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            displayAlert("Product name is required.", "error");
            return false;
        } else if (name.length() < 3 || name.length() > 50) {
            displayAlert("Product name must be between 3 and 50 characters.", "error");
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
            if (priceValue <= 0 || priceValue > 100) {
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
            if (quantityValue <= 0 || quantityValue > 1000) {
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

    private String getCurrentImagePath(int productId) {
        Product product = Datasource.getInstance().getOneProduct(productId);
        return product != null ? product.getImage() : null;
    }




    public void fillEditingProductFields(int product_id) {
        Task<ObservableList<Product>> fillProductTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                return FXCollections.observableArrayList(Datasource.getInstance().getOneProduct(product_id));
            }
        };

        fillProductTask.setOnSucceeded(e -> {
            if (!fillProductTask.valueProperty().getValue().isEmpty()) {
                Product product = fillProductTask.valueProperty().getValue().get(0);
                viewProductName.setText("Editing: " + product.getName());
                fieldEditProductId.setText(String.valueOf(product.getId()));
                fieldEditProductName.setText(product.getName());
                fieldEditProductPrice.setText(String.valueOf(product.getPrice()));
                fieldEditProductQuantity.setText(String.valueOf(product.getQuantity()));
                fieldEditProductDescription.setText(product.getDescription());

                // Set the selected category correctly
                Categories category = new Categories();
                category.setId(product.getCategory_id());
                category.setName(product.getCategory_name());
                fieldEditProductCategoryId.getSelectionModel().select(category);

                // Load the product image
                loadProductImage(product.getImage());
            }
        });

        new Thread(fillProductTask).start();
    }

    private void loadProductImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                String resourcePath = imagePath.startsWith("/") ? imagePath : "/view/resources/img/coffee_pictures/" + imagePath;
                URL imageUrl = getClass().getResource(resourcePath);

                if (imageUrl != null) {
                    productImageView.setImage(new Image(imageUrl.toString()));
                } else {
                    // If the image is not found in resources, try to load from the file system
                    File imageFile = new File(System.getProperty("user.dir"), "src/main/resources" + resourcePath);
                    if (imageFile.exists()) {
                        productImageView.setImage(new Image(imageFile.toURI().toString()));
                    } else {
                        System.err.println("Image not found: " + imagePath);
                        productImageView.setImage(DEFAULT_IMAGE);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                productImageView.setImage(DEFAULT_IMAGE);
            }
        } else {
            productImageView.setImage(DEFAULT_IMAGE);
        }
    }


    private void loadDefaultImage() {
        productImageView.setImage(DEFAULT_IMAGE);
        System.out.println("Loaded default image.");
    }

    private void clearForm() {
        fieldEditProductName.clear();
        fieldEditProductDescription.clear();
        fieldEditProductPrice.clear();
        fieldEditProductQuantity.clear();
        fieldEditProductCategoryId.getSelectionModel().clearSelection();
        productImageView.setImage(DEFAULT_IMAGE);
        selectedImageFile = null;
    }

    @Override
    protected void displayAlert(String message, String type) {
        alertMessage.setText(message);
        alertContainer.getStyleClass().removeAll("success", "error", "warning");
        alertContainer.getStyleClass().add(type.toLowerCase());
        alertContainer.setManaged(true);
        alertContainer.setVisible(true);

        // Add listeners to text input fields only
        fieldEditProductName.setOnKeyPressed(e -> hideAlert());
        fieldEditProductPrice.setOnKeyPressed(e -> hideAlert());
        fieldEditProductQuantity.setOnKeyPressed(e -> hideAlert());
        fieldEditProductDescription.setOnKeyPressed(e -> hideAlert());
        fieldEditProductCategoryId.setOnAction(e -> hideAlert());

        // Add a listener to the image selection process
        productImageView.imageProperty().addListener((obs, oldImg, newImg) -> hideAlert());
    }

    private void hideAlert() {
        alertContainer.setManaged(false);
        alertContainer.setVisible(false);

        // Remove listeners from text input fields
        fieldEditProductName.setOnKeyPressed(null);
        fieldEditProductPrice.setOnKeyPressed(null);
        fieldEditProductQuantity.setOnKeyPressed(null);
        fieldEditProductDescription.setOnKeyPressed(null);
        fieldEditProductCategoryId.setOnAction(null);

        // Remove the image listener
        productImageView.imageProperty().removeListener(change -> {});
    }
}
