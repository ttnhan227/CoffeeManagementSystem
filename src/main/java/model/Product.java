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
    private int nr_sales;
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

    public int getNr_sales() {
        return nr_sales;
    }

    public void setNr_sales(int nr_sales) {
        this.nr_sales = nr_sales;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String imagePath) {
        this.image = imagePath;
        try {
            // Get the project path
            String projectPath = System.getProperty("user.dir");
            File imageFile = new File(projectPath + imagePath);

            System.out.println("Attempting to load image from: " + imageFile.getAbsolutePath());

            Image img;
            if (imageFile.exists()) {
                img = new Image(imageFile.toURI().toString());
                System.out.println("Image loaded successfully from file.");
            } else {
                // Fallback to loading from resources
                URL resourceUrl = getClass().getResource(imagePath);
                System.out.println("Loading image from resources: " + resourceUrl);
                if (resourceUrl != null) {
                    img = new Image(resourceUrl.toString());
                    System.out.println("Image loaded successfully from resources.");
                } else {
                    // If image cannot be found, load a placeholder
                    URL placeholderUrl = getClass().getResource("/view/resources/img/coffee_pictures/placeholder.png");
                    if (placeholderUrl != null) {
                        img = new Image(placeholderUrl.toString());
                        System.err.println("Using placeholder image.");
                    } else {
                        System.err.println("Neither image nor placeholder could be loaded: " + imagePath);
                        return;
                    }
                }
            }

            imageView = new ImageView(img);
            // Resize the image to a fixed size of 100x100 pixels
            imageView.setFitHeight(100);  // Set the desired height
            imageView.setFitWidth(100);    // Set the desired width
            imageView.setPreserveRatio(true);  // Preserve the aspect ratio

        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
            e.printStackTrace();
        }
    }



    public ImageView getImageView() {
        return imageView;
    }

}
