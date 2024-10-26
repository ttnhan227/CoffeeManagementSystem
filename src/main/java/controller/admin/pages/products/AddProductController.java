package controller.admin.pages.products;

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
        int cat_id = 0;
        if (category != null) {
            cat_id = category.getId();
        }

        assert category != null;
        if (areProductInputsValid(fieldAddProductName.getText(), fieldAddProductDescription.getText(),
                fieldAddProductPrice.getText(), fieldAddProductQuantity.getText(), cat_id)) {

            String productName = fieldAddProductName.getText();
            String productDescription = fieldAddProductDescription.getText();
            double productPrice = Double.parseDouble(fieldAddProductPrice.getText());
            int productQuantity = Integer.parseInt(fieldAddProductQuantity.getText());
            int productCategoryId = category.getId();
            String imagePath = saveImageFile();

            Task<Boolean> addProductTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return Datasource.getInstance().insertNewProduct(
                            productName, productDescription, productPrice,
                            productQuantity, productCategoryId, imagePath);
                }
            };

            addProductTask.setOnSucceeded(e -> {
                if (addProductTask.valueProperty().get()) {
                    viewProductResponse.setVisible(true);
                    System.out.println("Product added!");

                    // Clear the form
                    clearForm();

                    // Refresh the products list
                    listProducts();
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