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
    private void btnEditProductOnAction() {
        Categories category = fieldEditProductCategoryId.getSelectionModel().getSelectedItem();
        int cat_id = 0;
        if (category != null) {
            cat_id = category.getId();
        }

        assert category != null;
        if (areProductInputsValid(fieldEditProductName.getText(), fieldEditProductDescription.getText(), fieldEditProductPrice.getText(), fieldEditProductQuantity.getText(), cat_id)) {

            int productId = Integer.parseInt(fieldEditProductId.getText());
            String productName = fieldEditProductName.getText();
            String productDescription = fieldEditProductDescription.getText();
            double productPrice = Double.parseDouble(fieldEditProductPrice.getText());
            int productQuantity = Integer.parseInt(fieldEditProductQuantity.getText());
            int productCategoryId = category.getId();
            String imagePath = saveImageFile();

            Task<Boolean> editProductTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return Datasource.getInstance().updateOneProduct(
                            productId, productName, productDescription,
                            productPrice, productQuantity, productCategoryId, imagePath);
                }
            };

            editProductTask.setOnSucceeded(e -> {
                if (editProductTask.valueProperty().get()) {
                    viewProductResponse.setVisible(true);
                    System.out.println("Product edited!");

                    // Optionally clear the form or provide feedback
                    clearForm();
                }
            });

            new Thread(editProductTask).start();
        }
    }

    public void fillEditingProductFields(int product_id) {
        Task<ObservableList<Product>> fillProductTask = new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                return FXCollections.observableArrayList(
                        Datasource.getInstance().getOneProduct(product_id));
            }
        };

        fillProductTask.setOnSucceeded(e -> {
            Product product = fillProductTask.valueProperty().getValue().get(0);
            viewProductName.setText("Editing: " + product.getName());
            fieldEditProductId.setText(String.valueOf(product.getId()));
            fieldEditProductName.setText(product.getName());
            fieldEditProductPrice.setText(String.valueOf(product.getPrice()));
            fieldEditProductQuantity.setText(String.valueOf(product.getQuantity()));
            fieldEditProductDescription.setText(product.getDescription());

            Categories category = new Categories();
            category.setId(product.getCategory_id());
            category.setName(product.getCategory_name());
            fieldEditProductCategoryId.getSelectionModel().select(category);

            // Set product image if available
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                String imagePath = "src/main/resources" + product.getImage();
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    productImageView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    productImageView.setImage(DEFAULT_IMAGE); // Fallback image if the file doesn't exist
                }
            } else {
                productImageView.setImage(DEFAULT_IMAGE); // Default image if no image path is set
            }
        });

        new Thread(fillProductTask).start();
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
