package controller.admin.pages.products;

import javafx.collections.FXCollections;
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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class AddProductController extends ProductsController {

    @FXML
    public ComboBox<Categories> fieldAddProductCategoryId;
    public TextField fieldAddProductName;
    public TextField fieldAddProductPrice;
    public TextField fieldAddProductQuantity;
    public TextArea fieldAddProductDescription;
    public Text viewProductResponse;
    public ImageView productImageView; // New
    public Button selectImageButton; // New

    private File selectedImageFile; // New
    private static final String IMAGE_UPLOAD_PATH = "/view/resources/img/coffee_pictures/"; // New

    private Runnable onProductAdded;

    @FXML private VBox alertContainer;
    @FXML private Label alertMessage;

    public void setOnProductAdded(Runnable callback) {
        this.onProductAdded = callback;
    }

    @FXML
    private void initialize() {
        fieldAddProductCategoryId.setItems(FXCollections.observableArrayList(
                Datasource.getInstance().getProductCategories(Datasource.ORDER_BY_ASC)));

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

        fieldAddProductPrice.setTextFormatter(priceFormatter);
        fieldAddProductQuantity.setTextFormatter(quantityFormatter);

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
            String projectPath = System.getProperty("user.dir");
            String fileName = System.currentTimeMillis() + "_" + selectedImageFile.getName();
            Path destinationPath = Paths.get(projectPath, "src/main/resources" + IMAGE_UPLOAD_PATH, fileName);

            // Create directories if they don't exist
            Files.createDirectories(destinationPath.getParent());

            // Copy the file
            Files.copy(selectedImageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            return IMAGE_UPLOAD_PATH + fileName;
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void displayAlert(String message, String type) {
        alertMessage.setText(message);
        alertContainer.getStyleClass().removeAll("success", "error", "warning");
        alertContainer.getStyleClass().add(type.toLowerCase());
        alertContainer.setManaged(true);
        alertContainer.setVisible(true);
        
        // Add listeners to text input fields only
        fieldAddProductName.setOnKeyPressed(e -> hideAlert());
        fieldAddProductPrice.setOnKeyPressed(e -> hideAlert());
        fieldAddProductQuantity.setOnKeyPressed(e -> hideAlert());
        fieldAddProductDescription.setOnKeyPressed(e -> hideAlert());
        fieldAddProductCategoryId.setOnAction(e -> hideAlert());
        
        // Add a listener to the image selection process instead of the button
        productImageView.imageProperty().addListener((obs, oldImg, newImg) -> hideAlert());
    }

    private void hideAlert() {
        alertContainer.setManaged(false);
        alertContainer.setVisible(false);
        
        // Remove listeners from text input fields only
        fieldAddProductName.setOnKeyPressed(null);
        fieldAddProductPrice.setOnKeyPressed(null);
        fieldAddProductQuantity.setOnKeyPressed(null);
        fieldAddProductDescription.setOnKeyPressed(null);
        fieldAddProductCategoryId.setOnAction(null);
        
        // Remove the image listener
        productImageView.imageProperty().removeListener(change -> {});
    }

    @FXML
    private void btnAddProductOnAction() {
        Categories category = fieldAddProductCategoryId.getSelectionModel().getSelectedItem();
        final int cat_id = category != null ? category.getId() : 0;

        // Trim input values to remove leading/trailing whitespace
        final String productName = fieldAddProductName.getText().trim();
        final String productDescription = fieldAddProductDescription.getText().trim();
        final String productPrice = fieldAddProductPrice.getText().trim();
        final String productQuantity = fieldAddProductQuantity.getText().trim();

        if (areProductInputsValid(productName, productDescription, productPrice, productQuantity, cat_id)) {
            final double price = Double.parseDouble(productPrice);
            final int quantity = Integer.parseInt(productQuantity);
            final String imagePath = saveImageFile();
            
            if (imagePath == null) {
                displayAlert("Please select an image for the product.", "warning");
                return;
            }

            final boolean isEnabled = true;

            Task<Boolean> addProductTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return Datasource.getInstance().insertNewProduct(
                            productName, productDescription, price,
                            quantity, cat_id, imagePath, isEnabled);
                }
            };

            addProductTask.setOnSucceeded(e -> {
                if (addProductTask.valueProperty().get()) {
                    displayAlert("Product added successfully!", "success");
                    clearForm();
                    if (onProductAdded != null) {
                        onProductAdded.run();
                    }
                } else {
                    displayAlert("Failed to add product. Please try again.", "error");
                }
            });

            new Thread(addProductTask).start();
        }
    }

    private void clearForm() {
        fieldAddProductName.clear();
        fieldAddProductDescription.clear();
        fieldAddProductPrice.clear();
        fieldAddProductQuantity.clear();
        fieldAddProductCategoryId.getSelectionModel().clearSelection();
        productImageView.setImage(DEFAULT_IMAGE);
        selectedImageFile = null;
    }
}