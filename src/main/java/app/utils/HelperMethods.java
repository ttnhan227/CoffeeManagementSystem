package app.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class HelperMethods {

    public static boolean validateFullName(String fullName) {
        return fullName.matches("^[A-Z][a-zA-Z]{1,}(?: [A-Z][a-zA-Z]*|-[A-Z][a-zA-Z]*)*$") && 
               fullName.length() >= 2 && 
               fullName.length() <= 50;
    }

    public static boolean validateUsername(String username) {
        return username.matches("^[A-Za-z][A-Za-z0-9_]{2,29}$");
    }

    public static boolean validateEmail(String emailStr) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return emailStr.matches(emailRegex) && 
               emailStr.length() >= 5 && 
               emailStr.length() <= 254;
    }

    public static boolean validatePassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,32}$");
    }


    public static boolean validateProductQuantity(String integer) {
        return integer.matches("-?(0|[1-9]\\d*)");
    }

    public static boolean validateProductPrice(String productPrice) {
        return productPrice.matches("^[0-9]+(|\\.)[0-9]+$");
    }

    public static void alertBox(String title, String message, String insertData) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(message);

        // Show only the OK button
        alert.getButtonTypes().setAll(ButtonType.OK);

        // Optional: apply custom stylesheet if needed
        alert.getDialogPane().getStylesheets().add(
                HelperMethods.class.getResource("/view/resources/css/dialog.css").toExternalForm());

        alert.showAndWait();
    }




}
