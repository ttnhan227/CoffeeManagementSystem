package controller.users.pages.products;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.Categories;
import model.Datasource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

    public void setOnProductAdded(Runnable callback) {
        this.onProductAdded = callback;
    }

    @FXML
    private void initialize() {
        fieldAddProductCategoryId.setItems(FXCollections.observableArrayList(
                Datasource.getInstance().getProductCategories(Datasource.ORDER_BY_ASC)));

        TextFormatter<Double> textFormatterDouble = formatDoubleField();
        TextFormatter<Integer> textFormatterInt = formatIntField();
        fieldAddProductPrice.setTextFormatter(textFormatterDouble);
        fieldAddProductQuantity.setTextFormatter(textFormatterInt);

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
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Image Required");
                alert.setHeaderText(null);
                alert.setContentText("Please select an image for the product.");
                alert.showAndWait();
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
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Product added successfully!");
                    successAlert.showAndWait();

                    clearForm();

                    if (onProductAdded != null) {
                        onProductAdded.run();
                    }
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to add product. Please try again.");
                    errorAlert.showAndWait();
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