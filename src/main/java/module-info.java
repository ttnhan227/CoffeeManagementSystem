module app {
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
    opens controller.users to javafx.fxml; // Add this line
    opens controller.users.pages to javafx.fxml;
    opens controller.admin.pages.users to javafx.fxml;
    opens controller.users.pages.products to javafx.fxml;
    opens controller.admin.pages.orders to javafx.fxml;
    opens controller.users.pages.orders to javafx.fxml;
    opens model to javafx.fxml;
}


