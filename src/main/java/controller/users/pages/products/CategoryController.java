package controller.users.pages.products;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Categories;
import model.Datasource;

public class CategoryController {
    @FXML
    public TextField fieldCategoryName;
    @FXML
    public TextField fieldCategoryDescription;
    @FXML
    public TableView<Categories> categoriesTable; // Specify the type for type safety
    @FXML
    public TableColumn<Categories, Integer> categoryIdColumn; // Specify the type for type safety
    @FXML
    public TableColumn<Categories, String> categoryNameColumn; // Specify the type for type safety
    @FXML
    public TableColumn<Categories, String> descriptionColumn; // Specify the type for type safety

    private final ObservableList<Categories> categoryList; // Store the categories in an observable list
    private final Datasource datasource; // Reference to the Datasource

    public CategoryController() {
        datasource = Datasource.getInstance(); // Get the instance of Datasource
        categoryList = FXCollections.observableArrayList(); // Create the observable list
        // Do not load categories here
    }

    @FXML
    private void initialize() {
        loadCategories(); // Load categories once the UI components are initialized
    }

    private void loadCategories() {
        categoryList.setAll(datasource.getAllCategories()); // Fetch categories from the datasource
        categoriesTable.setItems(categoryList); // Set the items for the TableView
    }

    public void btnAddCategoryOnClick(ActionEvent actionEvent) {
        String name = fieldCategoryName.getText();
        String description = fieldCategoryDescription.getText();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        Categories newCategory = new Categories(); // Create a new Categories object
        newCategory.setName(name);
        newCategory.setDescription(description);

        // Add the new category to the datasource
        if (datasource.addCategory(newCategory)) {
            categoryList.add(newCategory); // Add to the observable list
            clearFields(); // Clear input fields
        } else {
            showAlert("Error", "Could not add the category. Please try again.");
        }
    }

    public void btnEditCategoryOnClick(ActionEvent actionEvent) {
        Categories selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            showAlert("Error", "Please select a category to edit.");
            return;
        }

        String name = fieldCategoryName.getText();
        String description = fieldCategoryDescription.getText();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        selectedCategory.setName(name); // Update the selected category
        selectedCategory.setDescription(description);

        // Update via datasource
        if (datasource.updateCategory(selectedCategory)) {
            categoriesTable.refresh(); // Refresh the table to reflect changes
            clearFields(); // Clear input fields
        } else {
            showAlert("Error", "Could not update the category. Please try again.");
        }
    }

    public void btnDeleteCategoryOnClick(ActionEvent actionEvent) {
        Categories selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            showAlert("Error", "Please select a category to delete.");
            return;
        }

        // Delete via datasource
        if (datasource.deleteCategory(selectedCategory.getId())) {
            categoryList.remove(selectedCategory); // Remove from the observable list
            clearFields(); // Clear input fields
        } else {
            showAlert("Error", "Could not delete the category. Please try again.");
        }
    }

    private void clearFields() {
        fieldCategoryName.clear();
        fieldCategoryDescription.clear();
        categoriesTable.getSelectionModel().clearSelection(); // Clear selection in the table
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
