package controller.admin.pages.products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    public void setOnProductEdited(Runnable callback) {
        this.onProductEdited = callback;
    }

    @FXML
    private void initialize() {
        fieldEditProductCategoryId.setItems(FXCollections.observableArrayList(Datasource.getInstance().getProductCategories(Datasource.ORDER_BY_ASC)));

        TextFormatter<Double> textFormatterDouble = formatDoubleField();
        TextFormatter<Integer> textFormatterInt = formatIntField();
        fieldEditProductPrice.setTextFormatter(textFormatterDouble);
        fieldEditProductQuantity.setTextFormatter(textFormatterInt);

        // Initialize image view with placeholder
        productImageView.setFitHeight(200);
        productImageView.setFitWidth(200);
        productImageView.setPreserveRatio(true);
        productImageView.setImage(DEFAULT_IMAGE);
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
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Product updated successfully!");
                    successAlert.showAndWait();
                    
                    // Notify parent and refresh
                    if (onProductEdited != null) {
                        onProductEdited.run();
                    }
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to update product. Please try again.");
                    errorAlert.showAndWait();
                }
            });

            new Thread(editProductTask).start();
        }
    }

    // Add this new method for edit-specific validation
    protected boolean areProductInputsValidForEdit(String name, String description, 
        String price, String quantity, int categoryId, boolean isNameUnchanged) {
        
        StringBuilder errorMessage = new StringBuilder();
        boolean isValid = true;

        // Check if product name already exists (only if name has changed)
        if (!isNameUnchanged && Datasource.getInstance().isProductNameExists(name)) {
            errorMessage.append("- Product name already exists\n");
            isValid = false;
        }

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errorMessage.append("- Product name is required\n");
            isValid = false;
        } else if (name.length() < 3 || name.length() > 50) {
            errorMessage.append("- Product name must be between 3 and 50 characters\n");
            isValid = false;
        }

        // Validate description
        if (description == null || description.trim().isEmpty()) {
            errorMessage.append("- Product description is required\n");
            isValid = false;
        } else if (description.length() < 10 || description.length() > 500) {
            errorMessage.append("- Description must be between 10 and 500 characters\n");
            isValid = false;
        }

        // Validate price
        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue <= 0) {
                errorMessage.append("- Price must be greater than 0\n");
                isValid = false;
            } else if (priceValue > 300) {
                errorMessage.append("- Price cannot exceed $300\n");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            errorMessage.append("- Invalid price format\n");
            isValid = false;
        }

        // Validate quantity
        try {
            int quantityValue = Integer.parseInt(quantity);
            if (quantityValue <= 0) {
                errorMessage.append("- Quantity must be greater than 0\n");
                isValid = false;
            } else if (quantityValue > 1000) {
                errorMessage.append("- Quantity cannot exceed 1000 units\n");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            errorMessage.append("- Invalid quantity format\n");
            isValid = false;
        }

        // Validate category
        if (categoryId <= 0) {
            errorMessage.append("- Please select a category\n");
            isValid = false;
        }

        if (!isValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
        }

        return isValid;
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
}
