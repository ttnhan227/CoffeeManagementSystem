package controller.admin.pages.products;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import model.Categories;
import model.Datasource;
public class AddProductController extends ProductsController {

    @FXML
    public ComboBox<Categories> fieldAddProductCategoryId;
    public TextField fieldAddProductName;
    public TextField fieldAddProductPrice;
    public TextField fieldAddProductQuantity;
    public TextArea fieldAddProductDescription;
    public Text viewProductResponse;


    @FXML
    private void initialize() {
        fieldAddProductCategoryId.setItems(FXCollections.observableArrayList(Datasource.getInstance().getProductCategories(Datasource.ORDER_BY_ASC)));

        TextFormatter<Double> textFormatterDouble = formatDoubleField();
        TextFormatter<Integer> textFormatterInt = formatIntField();
        fieldAddProductPrice.setTextFormatter(textFormatterDouble);
        fieldAddProductQuantity.setTextFormatter(textFormatterInt);
    }
    @FXML
    private void btnAddProductOnAction() {
        Categories category = fieldAddProductCategoryId.getSelectionModel().getSelectedItem();
        int cat_id = 0;
        if (category != null) {
            cat_id = category.getId();
        }

        assert category != null;
        if (areProductInputsValid(fieldAddProductName.getText(), fieldAddProductDescription.getText(), fieldAddProductPrice.getText(), fieldAddProductQuantity.getText(), cat_id)) {

            String productName = fieldAddProductName.getText();
            String productDescription = fieldAddProductDescription.getText();
            double productPrice = Double.parseDouble(fieldAddProductPrice.getText());
            int productQuantity = Integer.parseInt(fieldAddProductQuantity.getText());
            int productCategoryId = category.getId();

            Task<Boolean> addProductTask = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return Datasource.getInstance().insertNewProduct(productName, productDescription, productPrice, productQuantity, productCategoryId);
                }
            };

            addProductTask.setOnSucceeded(e -> {
                if (addProductTask.valueProperty().get()) {
                    viewProductResponse.setVisible(true);
                    System.out.println("Product added!");
                }
            });

            new Thread(addProductTask).start();
        }
    }
}
