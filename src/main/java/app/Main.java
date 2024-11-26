package app;

import controller.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.Datasource;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root;
        
        // Check for existing session
        if (SessionManager.getInstance().hasActiveSession()) {
            // Load appropriate dashboard based on user role
            if (SessionManager.getInstance().getUserAdmin() == 0) {
                root = FXMLLoader.load(getClass().getResource("/view/users/main-dashboard.fxml"));
            } else {
                root = FXMLLoader.load(getClass().getResource("/view/admin/main-dashboard.fxml"));
            }
            primaryStage.setMaximized(true);
        } else {
            root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        }
        
        primaryStage.setTitle("Coffee Management System");
        primaryStage.getIcons().add(new Image(getClass().getResource("/view/resources/img/brand/pngtree-simple-coffee-shop-logo-png-image_13299684.png").toString()));
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (!Datasource.getInstance().open()) {
            System.out.println("FATAL ERROR: Couldn't connect to database");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Datasource.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
