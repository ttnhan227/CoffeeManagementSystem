module coffeeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j; // Add this line

    // Export the packages you want other modules to access (if any)
    exports app;
    exports controller;
    exports model;

    // Open packages for JavaFX's reflection, e.g., for FXML loaders
    opens app to javafx.fxml;
    opens controller to javafx.fxml;
    opens controller.admin to javafx.fxml; // Add this line
    opens controller.admin.pages to javafx.fxml;
    opens controller.admin.pages.products to javafx.fxml;
    opens controller.user to javafx.fxml; // Add this line
    opens controller.user.pages to javafx.fxml;




}
