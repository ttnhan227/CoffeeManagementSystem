package model;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.net.URL;

public class Product {

    private int id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private int category_id;
    private String category_name;
    private String image;
    private ImageView imageView;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }



    public String getImage() {
        return image;
    }

    public void setImage(String imagePath) {
        this.image = imagePath;

        if (imagePath == null || imagePath.isEmpty()) {
            // Load default placeholder image
            URL placeholderUrl = getClass().getResource("/view/resources/img/coffee_pictures/placeholder.png");
            if (placeholderUrl != null) {
                imageView = new ImageView(new Image(placeholderUrl.toString()));
            }
            return;
        }

        try {
            Image img = null;

            // Try loading from resources first
            String resourcePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
            URL resourceUrl = getClass().getResource("/src/main/resources" + resourcePath);

            if (resourceUrl != null) {
                img = new Image(resourceUrl.toString());
            } else {
                // Try loading from file system
                String projectPath = System.getProperty("user.dir");
                File imageFile = new File(projectPath + "/src/main/resources" + resourcePath);

                if (imageFile.exists()) {
                    img = new Image(imageFile.toURI().toString());
                }
            }

            // If image is still null, load placeholder
            if (img == null) {
                URL placeholderUrl = getClass().getResource("/view/resources/img/coffee_pictures/placeholder.png");
                if (placeholderUrl != null) {
                    img = new Image(placeholderUrl.toString());
                }
            }

            // Create ImageView with the loaded image
            if (img != null) {
                imageView = new ImageView(img);
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                imageView.setPreserveRatio(true);
            }

        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            e.printStackTrace();
        }
    }




    public ImageView getImageView() {
        return imageView;
    }

}
