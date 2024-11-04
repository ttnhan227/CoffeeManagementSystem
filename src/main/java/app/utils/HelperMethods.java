package app.utils;

import javafx.scene.control.Alert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelperMethods {

    public static boolean validateFullName(String fullName) {

        Matcher matcher = Pattern.compile("^[A-Z][a-zA-Z]{3,}(?: [A-Z][a-zA-Z]*){0,2}$", Pattern.CASE_INSENSITIVE).matcher(fullName);
        return matcher.find();
    }

    public static boolean validateUsername(String username) {
//      ^                       # start of string
//      [a-zA-Z]                # lowercase or uppercase ASCII letters
//      \\w{4, 29}              # remaining items are word items, which includes the underscore,
//      until it reaches the end and that is represented with $
//      {4, 29}                 # 5-30 character constraint given, minus the predefined first character
        Matcher matcher = Pattern.compile("^[A-Za-z]\\w{4,29}$", Pattern.CASE_INSENSITIVE).matcher(username);
        return matcher.find();
    }

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(emailStr);
        return matcher.find();
    }

    public static boolean validatePassword(String password) {

        return password.matches("^.{6,16}$");
    }

    public static boolean validateProductQuantity(String integer) {
        return integer.matches("-?(0|[1-9]\\d*)");
    }

    public static boolean validateProductPrice(String productPrice) {
        return productPrice.matches("^[0-9]+(|\\.)[0-9]+$");
    }

    public static void alertBox(String infoMessage, String headerText, String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(String.valueOf(infoMessage));
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

}
