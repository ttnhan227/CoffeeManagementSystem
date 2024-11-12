package controller.admin.pages.products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Categories;
import model.Datasource;

public class CategoryController {
    @FXML
    public TextField fieldCategoryName;
    @FXML
    public TextField fieldCategoryDescription;
    @FXML
    public TableView<Categories> categoriesTable;
    @FXML
    public TableColumn<Categories, Integer> categoryIdColumn;
    @FXML
    public TableColumn<Categories, String> categoryNameColumn;
    @FXML
    public TableColumn<Categories, String> descriptionColumn;

    private final ObservableList<Categories> categoryList;
    private final Datasource datasource;

    public CategoryController() {
        datasource = Datasource.getInstance();
        categoryList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        // Set up table columns
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Add selection listener to populate fields when a category is selected
        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fieldCategoryName.setText(newSelection.getName());
                fieldCategoryDescription.setText(newSelection.getDescription());
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        categoryList.setAll(datasource.getAllCategories());
        categoriesTable.setItems(categoryList);
    }

    @FXML
    public void btnAddCategoryOnClick(ActionEvent actionEvent) {
        if (!validateInputs()) return;

        Categories newCategory = new Categories();
        newCategory.setName(fieldCategoryName.getText());
        newCategory.setDescription(fieldCategoryDescription.getText());

        if (datasource.addCategory(newCategory)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully!");
            loadCategories();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not add the category. Please try again.");
        }
    }

    @FXML
    public void btnEditCategoryOnClick(ActionEvent actionEvent) {
        Categories selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a category to edit.");
            return;
        }

        if (!validateInputs()) return;

        selectedCategory.setName(fieldCategoryName.getText());
        selectedCategory.setDescription(fieldCategoryDescription.getText());

        if (datasource.updateCategory(selectedCategory)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
            loadCategories();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update the category. Please try again.");
        }
    }

    @FXML
    public void btnDeleteCategoryOnClick(ActionEvent actionEvent) {
        Categories selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a category to delete.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Category");
        confirmDialog.setContentText("Are you sure you want to delete this category?");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            if (datasource.deleteCategory(selectedCategory.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully!");
                loadCategories();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete the category. It may be in use.");
            }
        }
    }

    private boolean validateInputs() {
        String name = fieldCategoryName.getText();
        String description = fieldCategoryDescription.getText();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all fields.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        fieldCategoryName.clear();
        fieldCategoryDescription.clear();
        categoriesTable.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
