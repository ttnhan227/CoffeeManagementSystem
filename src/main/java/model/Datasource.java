package model;

import controller.UserSessionController;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.net.URL;
public class Datasource extends Product {

    public static final String DB_NAME = "store_manager.sqlite";
    public static final String CONNECTION_STRING = "jdbc:sqlite:"
            + System.getProperty("user.dir")
            + "/src/main/java/app/db/"
            + DB_NAME;
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCTS_ID = "id";
    public static final String COLUMN_PRODUCTS_NAME = "name";
    public static final String COLUMN_PRODUCTS_DESCRIPTION = "description";
    public static final String COLUMN_PRODUCTS_PRICE = "price";
    public static final String COLUMN_PRODUCTS_QUANTITY = "quantity";
    public static final String COLUMN_PRODUCTS_CATEGORY_ID = "category_id";
    public static final String COLUMN_PRODUCTS_IMAGE = "image";
    public static final String COLUMN_PRODUCTS_ACTIVE = "active"; // New column for isDisabled



    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORIES_ID = "id";
    public static final String COLUMN_CATEGORIES_NAME = "name";
    public static final String COLUMN_CATEGORIES_DESCRIPTION = "description";

    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDERS_ID = "id";
    public static final String COLUMN_ORDERS_PRODUCT_ID = "product_id";
    public static final String COLUMN_ORDERS_USER_ID = "user_id";
    public static final String COLUMN_ORDERS_ORDER_DATE = "order_date";
    public static final String COLUMN_ORDERS_ORDER_STATUS = "order_status";

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERS_ID = "id";
    public static final String COLUMN_USERS_FULLNAME = "fullname";
    public static final String COLUMN_USERS_USERNAME = "username";
    public static final String COLUMN_USERS_EMAIL = "email";
    public static final String COLUMN_USERS_PASSWORD = "password";
    public static final String COLUMN_USERS_SALT = "salt";
    public static final String COLUMN_USERS_ADMIN = "admin";
    public static final String COLUMN_USERS_STATUS = "status";

    public static final int ORDER_BY_NONE = 1;
    public static final int ORDER_BY_ASC = 2;
    public static final int ORDER_BY_DESC = 3;
    private static final Datasource instance = new Datasource();
    private Connection conn;

    private Datasource() {
    }
    public static Datasource getInstance() {
        return instance;
    }

    public boolean open() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING);
            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }
    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }
    public List<Product> getAllProducts(int sortOrder) {
        StringBuilder queryProducts = queryProducts();

        if (sortOrder != ORDER_BY_NONE) {
            queryProducts.append(" ORDER BY ");
            queryProducts.append(COLUMN_PRODUCTS_NAME);
            if (sortOrder == ORDER_BY_DESC) {
                queryProducts.append(" DESC");
            } else {
                queryProducts.append(" ASC");
            }
        }

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(queryProducts.toString())) {

            List<Product> products = new ArrayList<>();
            while (results.next()) {
                try {
                    Product product = new Product();
                    product.setId(results.getInt(1));
                    product.setName(results.getString(2));
                    product.setDescription(results.getString(3));
                    product.setPrice(results.getDouble(4));
                    product.setQuantity(results.getInt(5));
                    product.setCategory_name(results.getString(6));
                    product.setImage(results.getString(7));
                    product.setCategory_id(results.getInt(8));
                    product.setDisabled(results.getBoolean(9)); // Set isDisabled field

                    products.add(product);

                } catch (Exception e) {
                    System.err.println("Error processing product from database:");
                    e.printStackTrace();
                    continue;
                }
            }
            return products;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }


    public Product getOneProduct(int product_id) {
        StringBuilder queryProducts = queryProducts();
        queryProducts.append(" WHERE " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID + " = ? LIMIT 1");

        try (PreparedStatement statement = conn.prepareStatement(String.valueOf(queryProducts))) {
            statement.setInt(1, product_id);
            ResultSet results = statement.executeQuery();

            if (results.next()) { // Check if there is a result
                Product product = new Product();
                product.setId(results.getInt("id"));  // Use column names for clarity
                product.setName(results.getString("name"));
                product.setDescription(results.getString("description"));
                product.setPrice(results.getDouble("price"));
                product.setQuantity(results.getInt("quantity"));
                product.setCategory_id(results.getInt("category_id"));
                product.setImage(results.getString("image"));  // Ensure this is the correct column
                return product;  // Return the single product
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return null; // Return null if no product is found
    }

    public List<Product> searchProducts(String searchString, int sortOrder) {
        StringBuilder queryProducts = queryProducts();
        queryProducts.append(" WHERE (" + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_NAME + " LIKE ? OR " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_DESCRIPTION + " LIKE ?)");

        if (sortOrder != ORDER_BY_NONE) {
            queryProducts.append(" ORDER BY ");
            queryProducts.append(COLUMN_PRODUCTS_NAME);
            if (sortOrder == ORDER_BY_DESC) {
                queryProducts.append(" DESC");
            } else {
                queryProducts.append(" ASC");
            }
        }

        try (PreparedStatement statement = conn.prepareStatement(queryProducts.toString())) {
            statement.setString(1, "%" + searchString + "%");
            statement.setString(2, "%" + searchString + "%");
            ResultSet results = statement.executeQuery();

            List<Product> products = new ArrayList<>();
            while (results.next()) {
                Product product = new Product();
                product.setId(results.getInt(1));
                product.setName(results.getString(2));
                product.setDescription(results.getString(3));
                product.setPrice(results.getDouble(4));
                product.setQuantity(results.getInt(5));
                product.setCategory_name(results.getString(6));
                // Add this line to set the image
                product.setImage(results.getString(7));  // Get image path from column 7
                products.add(product);
            }
            return products;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }
    private StringBuilder queryProducts() {
        return new StringBuilder("SELECT " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_NAME + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_DESCRIPTION + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_PRICE + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_QUANTITY + ", " +
                TABLE_CATEGORIES + "." + COLUMN_CATEGORIES_NAME + " AS category_name, " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_IMAGE + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_CATEGORY_ID + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ACTIVE + // Include isDisabled column
                " FROM " + TABLE_PRODUCTS +
                " LEFT JOIN " + TABLE_CATEGORIES +
                " ON " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_CATEGORY_ID +
                " = " + TABLE_CATEGORIES + "." + COLUMN_CATEGORIES_ID
        );
    }




    public boolean deleteSingleProduct(int productId) {
        String sql = "DELETE FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCTS_ID + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, productId);
            int rows = statement.executeUpdate();
            System.out.println(rows + " record(s) deleted.");
            return true;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

    public boolean insertNewProduct(String name, String description, double price,
                                    int quantity, int category_id, String imagePath, boolean isDisabled) {
        String sql = "INSERT INTO " + TABLE_PRODUCTS + " ("
                + COLUMN_PRODUCTS_NAME + ", "
                + COLUMN_PRODUCTS_DESCRIPTION + ", "
                + COLUMN_PRODUCTS_PRICE + ", "
                + COLUMN_PRODUCTS_QUANTITY + ", "
                + COLUMN_PRODUCTS_CATEGORY_ID + ", "
                + COLUMN_PRODUCTS_IMAGE + ", "
                + COLUMN_PRODUCTS_ACTIVE + // Include the isDisabled column in insert
                ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, price);
            statement.setInt(4, quantity);
            statement.setInt(5, category_id);
            statement.setString(6, imagePath);
            statement.setBoolean(7, isDisabled); // Set isDisabled value

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateOneProduct(int product_id, String name, String description,
                                    double price, int quantity, int category_id,
                                    String imagePath, boolean isDisabled) {

        String sql = "UPDATE " + TABLE_PRODUCTS + " SET "
                + COLUMN_PRODUCTS_NAME + " = ?, "
                + COLUMN_PRODUCTS_DESCRIPTION + " = ?, "
                + COLUMN_PRODUCTS_PRICE + " = ?, "
                + COLUMN_PRODUCTS_QUANTITY + " = ?, "
                + COLUMN_PRODUCTS_CATEGORY_ID + " = ?, "
                + COLUMN_PRODUCTS_IMAGE + " = ?, "
                + COLUMN_PRODUCTS_ACTIVE + " = ? " // Update isDisabled field
                + "WHERE " + COLUMN_PRODUCTS_ID + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, price);
            statement.setInt(4, quantity);
            statement.setInt(5, category_id);
            statement.setString(6, imagePath);
            statement.setBoolean(7, isDisabled); // Set isDisabled value
            statement.setInt(8, product_id);

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProductStatus(int productId, boolean isDisabled) {
        String updateQuery = "UPDATE " + TABLE_PRODUCTS + " SET " + COLUMN_PRODUCTS_ACTIVE + " = ? WHERE " + COLUMN_PRODUCTS_ID + " = ?";
        try (PreparedStatement statement = conn.prepareStatement(updateQuery)) {
            statement.setBoolean(1, isDisabled);
            statement.setInt(2, productId);
            int affectedRows = statement.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Status update failed: " + e.getMessage());
            return false;
        }
    }



    public void decreaseStock(int product_id) {

        String sql = "UPDATE " + TABLE_PRODUCTS + " SET " + COLUMN_PRODUCTS_QUANTITY + " = " + COLUMN_PRODUCTS_QUANTITY + " - 1 WHERE " + COLUMN_PRODUCTS_ID + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, product_id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }

    public List<Categories> getProductCategories(int sortOrder) {
        StringBuilder queryCategories = new StringBuilder("SELECT " +
                TABLE_CATEGORIES + "." + COLUMN_CATEGORIES_ID + ", " +
                TABLE_CATEGORIES + "." + COLUMN_CATEGORIES_NAME + ", " +
                TABLE_CATEGORIES + "." + COLUMN_CATEGORIES_DESCRIPTION +
                " FROM " + TABLE_CATEGORIES
        );

        if (sortOrder != ORDER_BY_NONE) {
            queryCategories.append(" ORDER BY ");
            queryCategories.append(COLUMN_CATEGORIES_ID);
            if (sortOrder == ORDER_BY_DESC) {
                queryCategories.append(" DESC");
            } else {
                queryCategories.append(" ASC");
            }
        }

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(queryCategories.toString())) {

            List<Categories> categories = new ArrayList<>();
            while (results.next()) {
                Categories category = new Categories();
                category.setId(results.getInt(1));
                category.setName(results.getString(2));
                categories.add(category);
            }
            return categories;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }



    public List<User> getAllUsers(int sortOrder) {

        StringBuilder queryCustomers = queryUsers();

        if (sortOrder != ORDER_BY_NONE) {
            queryCustomers.append(" ORDER BY ");
            queryCustomers.append(COLUMN_USERS_FULLNAME);
            if (sortOrder == ORDER_BY_DESC) {
                queryCustomers.append(" DESC");
            } else {
                queryCustomers.append(" ASC");
            }
        }
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(queryCustomers.toString())) {

            List<User> users = new ArrayList<>();
            while (results.next()) {
                User user = new User();
                user.setId(results.getInt(1));
                user.setFullname(results.getString(2));
                user.setEmail(results.getString(3));
                user.setUsername(results.getString(4));
                user.setOrders(results.getInt(5));
                user.setStatus(results.getString(6));
                users.add(user);
            }
            return users;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }
    public List<User> getOneUser(int customer_id) {

        StringBuilder queryCustomers = queryUsers();
        queryCustomers.append(" AND " + TABLE_USERS + "." + COLUMN_USERS_ID + " = ?");
        try (PreparedStatement statement = conn.prepareStatement(String.valueOf(queryCustomers))) {
            statement.setInt(1, customer_id);
            ResultSet results = statement.executeQuery();
            List<User> users = new ArrayList<>();
            while (results.next()) {
                User user = new User();
                user.setId(results.getInt(1));
                user.setFullname(results.getString(2));
                user.setEmail(results.getString(3));
                user.setUsername(results.getString(4));
                user.setOrders(results.getInt(5));
                user.setStatus(results.getString(6));
                users.add(user);
            }
            return users;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }
    public List<User> searchUsers(String searchString, int sortOrder) {

        StringBuilder queryCustomers = queryUsers();

        queryCustomers.append(" AND (" + TABLE_USERS + "." + COLUMN_USERS_FULLNAME + " LIKE ? OR " + TABLE_USERS + "." + COLUMN_USERS_USERNAME + " LIKE ?)");

        if (sortOrder != ORDER_BY_NONE) {
            queryCustomers.append(" ORDER BY ");
            queryCustomers.append(COLUMN_USERS_FULLNAME);
            if (sortOrder == ORDER_BY_DESC) {
                queryCustomers.append(" DESC");
            } else {
                queryCustomers.append(" ASC");
            }
        }
        try (PreparedStatement statement = conn.prepareStatement(queryCustomers.toString())) {
            statement.setString(1, "%" + searchString + "%");
            statement.setString(2, "%" + searchString + "%");
            ResultSet results = statement.executeQuery();

            List<User> users = new ArrayList<>();
            while (results.next()) {
                User user = new User();
                user.setId(results.getInt(1));
                user.setFullname(results.getString(2));
                user.setEmail(results.getString(3));
                user.setUsername(results.getString(4));
                user.setOrders(results.getInt(5));
                user.setStatus(results.getString(6));
                users.add(user);
            }
            return users;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public boolean updateOneUser(int customer_id, String fullName, String username, String email, String status) {
        String sql = "UPDATE " + TABLE_USERS + " SET "
                + COLUMN_USERS_FULLNAME + " = ?, "
                + COLUMN_USERS_USERNAME + " = ?, "
                + COLUMN_USERS_EMAIL + " = ?, "
                + COLUMN_USERS_STATUS + " = ? "
                + "WHERE " + COLUMN_USERS_ID + " = ? AND " + COLUMN_USERS_ADMIN + " = 0";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, status);  // Adjusted index for status
            statement.setInt(5, customer_id); // Adjusted index for customer_id

            System.out.println("Updating User: " + customer_id + ", " + fullName + ", " + email + ", " + username + ", " + status);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }




    private StringBuilder queryUsers() {
        return new StringBuilder("SELECT " +
                TABLE_USERS + "." + COLUMN_USERS_ID + ", " +
                TABLE_USERS + "." + COLUMN_USERS_FULLNAME + ", " +
                TABLE_USERS + "." + COLUMN_USERS_EMAIL + ", " +
                TABLE_USERS + "." + COLUMN_USERS_USERNAME + ", " +
                " (SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID + " = " + TABLE_USERS + "." + COLUMN_USERS_ID + ") AS orders" + ", " +
                TABLE_USERS + "." + COLUMN_USERS_STATUS +
                " FROM " + TABLE_USERS +
                " WHERE " + TABLE_USERS + "." + COLUMN_USERS_ADMIN + " = 0"
        );
    }

    public boolean deleteSingleUser(int customerId) {
        String sql = "DELETE FROM " + TABLE_USERS + " WHERE " + COLUMN_USERS_ID + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            int rows = statement.executeUpdate();
            System.out.println(rows + " " + TABLE_USERS + " record(s) deleted.");


            String sql2 = "DELETE FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDERS_USER_ID + " = ?";

            try (PreparedStatement statement2 = conn.prepareStatement(sql2)) {
                statement2.setInt(1, customerId);
                int rows2 = statement2.executeUpdate();
                System.out.println(rows2 + " " + TABLE_ORDERS + " record(s) deleted.");
                return true;
            } catch (SQLException e) {
                System.out.println("Query failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

    public User getUserByEmail(String email) throws SQLException {

        PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERS_EMAIL + " = ?");
        preparedStatement.setString(1, email);
        ResultSet results = preparedStatement.executeQuery();

        User user = new User();
        if (results.next()) {

            user.setId(results.getInt("id"));
            user.setFullname(results.getString("fullname"));
            user.setUsername(results.getString("username"));
            user.setEmail(results.getString("email"));
            user.setPassword(results.getString("password"));
            user.setSalt(results.getString("salt"));
            user.setAdmin(results.getInt("admin"));
            user.setStatus(results.getString("status"));

        }

        return user;
    }

    public User getUserByUsername(String username) throws SQLException {

        PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERS_USERNAME + " = ?");
        preparedStatement.setString(1, username);
        ResultSet results = preparedStatement.executeQuery();

        User user = new User();
        if (results.next()) {

            user.setId(results.getInt("id"));
            user.setFullname(results.getString("fullname"));
            user.setUsername(results.getString("username"));
            user.setEmail(results.getString("email"));
            user.setPassword(results.getString("password"));
            user.setSalt(results.getString("salt"));
            user.setAdmin(results.getInt("admin"));
            user.setStatus(results.getString("status"));

        }

        return user;
    }

    public boolean insertNewUser(String fullName, String username, String email, String password, String salt) {

        String sql = "INSERT INTO " + TABLE_USERS + " ("
                + COLUMN_USERS_FULLNAME + ", "
                + COLUMN_USERS_USERNAME + ", "
                + COLUMN_USERS_EMAIL + ", "
                + COLUMN_USERS_PASSWORD + ", "
                + COLUMN_USERS_SALT + ", "
                + COLUMN_USERS_ADMIN + ", "
                + COLUMN_USERS_STATUS +
                ") VALUES (?, ?, ?, ?, ?, 0, 'enabled')";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, password);
            statement.setString(5, salt);

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

//    public List<Order> getAllOrders(int sortOrder) {
//
//        StringBuilder queryOrders = new StringBuilder("SELECT " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_ID + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID + ", " +
//                TABLE_USERS + "." + COLUMN_USERS_FULLNAME + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_DATE + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_STATUS + ", " +
//                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_NAME + ", " +
//                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_PRICE +
//                " FROM " + TABLE_ORDERS
//        );
//
//        queryOrders.append("" +
//                " LEFT JOIN " + TABLE_PRODUCTS +
//                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID +
//                " = " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID);
//        queryOrders.append("" +
//                " LEFT JOIN " + TABLE_USERS +
//                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID +
//                " = " + TABLE_USERS + "." + COLUMN_USERS_ID);
//
//        if (sortOrder != ORDER_BY_NONE) {
//            queryOrders.append(" ORDER BY ");
//            queryOrders.append(COLUMN_USERS_FULLNAME);
//            if (sortOrder == ORDER_BY_DESC) {
//                queryOrders.append(" DESC");
//            } else {
//                queryOrders.append(" ASC");
//            }
//        }
//
//        try (Statement statement = conn.createStatement();
//             ResultSet results = statement.executeQuery(queryOrders.toString())) {
//
//            List<Order> orders = new ArrayList<>();
//            while (results.next()) {
//                Order order = new Order();
//                order.setId(results.getInt(1));
//                order.setProduct_id(results.getInt(2));
//                order.setUser_id(results.getInt(3));
//                order.setUser_full_name(results.getString(4));
//                order.setOrder_date(results.getString(5));
//                order.setOrder_status(results.getString(6));
//                order.setProduct_name(results.getString(7));
//                order.setOrder_price(results.getDouble(8));
//                orders.add(order);
//            }
//            return orders;
//
//        } catch (SQLException e) {
//            System.out.println("Query failed: " + e.getMessage());
//            return null;
//        }
//    }

//    public List<Order> getAllUserOrders(int sortOrder, int user_id) {
//
//        StringBuilder queryOrders = new StringBuilder("SELECT " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_ID + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID + ", " +
//                TABLE_USERS + "." + COLUMN_USERS_FULLNAME + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_DATE + ", " +
//                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_STATUS + ", " +
//                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_NAME + ", " +
//                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_PRICE +
//                " FROM " + TABLE_ORDERS
//        );
//
//        queryOrders.append("" +
//                " LEFT JOIN " + TABLE_PRODUCTS +
//                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID +
//                " = " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID);
//        queryOrders.append("" +
//                " LEFT JOIN " + TABLE_USERS +
//                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID +
//                " = " + TABLE_USERS + "." + COLUMN_USERS_ID);
//        queryOrders.append(" WHERE " + TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID + " = ").append(user_id);
//
//        if (sortOrder != ORDER_BY_NONE) {
//            queryOrders.append(" ORDER BY ");
//            queryOrders.append(COLUMN_USERS_FULLNAME);
//            if (sortOrder == ORDER_BY_DESC) {
//                queryOrders.append(" DESC");
//            } else {
//                queryOrders.append(" ASC");
//            }
//        }
//
//        try (Statement statement = conn.createStatement();
//             ResultSet results = statement.executeQuery(queryOrders.toString())) {
//
//            List<Order> orders = new ArrayList<>();
//            while (results.next()) {
//                Order order = new Order();
//                order.setId(results.getInt(1));
//                order.setProduct_id(results.getInt(2));
//                order.setUser_id(results.getInt(3));
//                order.setUser_full_name(results.getString(4));
//                order.setOrder_date(results.getString(5));
//                order.setOrder_status(results.getString(6));
//                order.setProduct_name(results.getString(7));
//                order.setOrder_price(results.getDouble(8));
//                orders.add(order);
//            }
//            return orders;
//
//        } catch (SQLException e) {
//            System.out.println("Query failed: " + e.getMessage());
//            return null;
//        }
//    }


    public boolean insertNewOrder(int product_id, int user_id, String order_date, String order_status) {

        String sql = "INSERT INTO " + TABLE_ORDERS + " ("
                + COLUMN_ORDERS_PRODUCT_ID + ", "
                + COLUMN_ORDERS_USER_ID + ", "
                + COLUMN_ORDERS_ORDER_DATE + ", "
                + COLUMN_ORDERS_ORDER_STATUS +
                ") VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, product_id);
            statement.setInt(2, user_id);
            statement.setString(3, order_date);
            statement.setString(4, order_status);

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

    public Integer countAllProducts() {
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS)) {
            if (results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return 0;
        }
    }

    public Integer countAllCustomers() {
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM " + TABLE_USERS +
                     " WHERE " + COLUMN_USERS_ADMIN + "= 0"
             )
        ) {
            if (results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return 0;
        }
    }

    public Integer countUserOrders(int user_id) {

        try (PreparedStatement statement = conn.prepareStatement(String.valueOf("SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDERS_USER_ID + "= ?"))) {
            statement.setInt(1, user_id);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return 0;
        }
    }

    public List<Integer> getAllTableID(){
        String query = "SELECT id FROM \"table\"";

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)){
            List<Integer> list = new ArrayList<>();
            while (results.next()) {
                list.add(results.getInt("id"));
            }
            return list;

        }catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public List<Coupon> getAllCoupon(){
        String query = "SELECT * FROM coupon";

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)){
            List<Coupon> list = new ArrayList<>();
            while (results.next()) {
                Coupon c = new Coupon();
                c.setId(results.getInt("couponID"));
                c.setDiscount(results.getInt("discount"));
                c.setExpiry(results.getString("expiry"));
                list.add(c);
//                System.out.println(c.getId());
//                System.out.println(c.getExpiry());
//                System.out.println(c.getDiscount());
            }
            return list;

        }catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public Product searchOneProductByName(String searchName){
        String query = "SELECT * FROM products WHERE name = '" + searchName + "'";
        Product product = new Product();
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)){
            if (results.next()) {
                product.setId(results.getInt("id"));
                product.setName(results.getString("name"));
                product.setDescription(results.getString("description"));
                product.setPrice(results.getDouble("price"));
                product.setQuantity(results.getInt("quantity"));
                product.setCategory_id(results.getInt("category_id"));
                product.setImage(results.getString("image"));
                return product;
            }
            else{
                System.out.println("No product found with search name");
                return null;
            }
            //return product;

        }catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }


    public Table getOneTable(int id){
        String query = "SELECT * FROM 'table' WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Table table = new Table();
                table.setId(resultSet.getInt("id"));
                table.setStatus(resultSet.getInt("status"));
                table.setCapacity(resultSet.getInt("capacity"));
                return table;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int searchCouponDiscountByID(int id){
        String query = "SELECT * FROM coupon WHERE couponID = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("discount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void createOrderWithDetails(String date,
                                       Customer customer,
                                       Table table, Coupon coupon,
                                       List<Product> productList,
                                       List<Integer> quantities,
                                       double total, double discount, double fin) throws SQLException
    {
        conn.setAutoCommit(false);

        try {
            String orderSql = "INSERT INTO [order] " +
                    "(employeeID, " +
                    "customerID, " +
                    "couponID, " +
                    "tableID, " +
                    "orderDate, " +
                    "total, " +
                    "discount, " +
                    "final) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, UserSessionController.getUserId());
                if (customer != null) {
                    orderStmt.setInt(2, customer.getId());
                }
                else{
                    orderStmt.setNull(2, java.sql.Types.INTEGER);
                }

                if (coupon != null) {
                    orderStmt.setInt(3, coupon.getId());
                }
                else{
                    orderStmt.setNull(3, java.sql.Types.INTEGER);
                }

                if (table != null) {
                    orderStmt.setInt(4, table.getId());
                }
                else{
                    orderStmt.setNull(4, java.sql.Types.INTEGER);
                }

                orderStmt.setString(5, date);
                orderStmt.setDouble(6, total);
                orderStmt.setDouble(7, discount);
                orderStmt.setDouble(8, fin);
                orderStmt.executeUpdate();
                // Get the generated order_id
                ResultSet rs = orderStmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);

                    //Insert into OrderDetail table for each product
                    String detailSql = "INSERT INTO orderDetail (orderID, productID, quantity, total) VALUES (?, ?, ?, ?)";
                    String updateProductSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
                    PreparedStatement updateProductStmt = conn.prepareStatement(updateProductSql);
                    try (PreparedStatement detailStmt = conn.prepareStatement(detailSql)) {
                        for (int i = 0; i < productList.size(); i++) {
                            detailStmt.setInt(1, orderId); // Set the foreign key from Order
                            detailStmt.setInt(2, productList.get(i).getId()); // Product ID
                            detailStmt.setInt(3, quantities.get(i)); // quantity

                            double productTotal = productList.get(i).getPrice() * quantities.get(i);
                            DecimalFormat format = new DecimalFormat("#.##");
                            String formattedTotal = format.format(productTotal);
                            detailStmt.setDouble(4, Double.parseDouble(formattedTotal));// product total

                            try {
                                updateProductStmt.setInt(1, quantities.get(i)); // Quantity to subtract
                                updateProductStmt.setInt(2, productList.get(i).getId()); // Product ID to update

                                updateProductStmt.addBatch(); // addbatch
                            }
                            catch (SQLException e){
                                System.out.println(e.getMessage());
                            }

                            detailStmt.addBatch();
                        }
                        detailStmt.executeBatch();
                        updateProductStmt.executeBatch();
                    }
                } else {
                    throw new SQLException("Order ID retrieval failed, no ID returned.");
                }
            }

            // Commit the transaction if everything is successful
            conn.commit();
        }catch (SQLException e) {
            // Rollback in case of an error
            conn.rollback();
            throw e;
        } finally {
            // Restore auto-commit
            conn.setAutoCommit(true);
        }
    }
    public String getCategoryName(int id){
        String query = "SELECT * FROM categories WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean deleteCategory(int id) {
        String query = "DELETE FROM " + TABLE_CATEGORIES + " WHERE " + COLUMN_CATEGORIES_ID + " = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, id);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0; // Return true if at least one row was deleted
        } catch (SQLException e) {
            System.out.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }
    public boolean updateCategory(Categories category) {
        String query = "UPDATE " + TABLE_CATEGORIES + " SET " +
                COLUMN_CATEGORIES_NAME + " = ?, " +
                COLUMN_CATEGORIES_DESCRIPTION + " = ? " +
                "WHERE " + COLUMN_CATEGORIES_ID + " = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.setInt(3, category.getId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0; // Return true if at least one row was updated
        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
            return false;
        }
    }
    public boolean addCategory(Categories category) {
        String query = "INSERT INTO " + TABLE_CATEGORIES + " (" +
                COLUMN_CATEGORIES_NAME + ", " +
                COLUMN_CATEGORIES_DESCRIPTION + ") VALUES (?, ?)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                return false; // No rows inserted
            }

            // Get the generated keys (category ID)
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1)); // Set the generated ID
                } else {
                    return false; // Failed to get ID
                }
            }
            return true; // Successfully added category
        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
            return false;
        }
    }
    public List<Categories> getAllCategories() {
        List<Categories> categories = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CATEGORIES;

        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Categories category = new Categories();
                category.setId(resultSet.getInt(COLUMN_CATEGORIES_ID));
                category.setName(resultSet.getString(COLUMN_CATEGORIES_NAME));
                category.setDescription(resultSet.getString(COLUMN_CATEGORIES_DESCRIPTION));
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public List<Order> getAllOrder(){
        String query = "SELECT * FROM [order]";

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)){
            List<Order> list = new ArrayList<>();
            while (results.next()) {
              Order o = new Order();
              o.setId(results.getInt("id"));
              o.setEmployeeID((Integer) results.getObject("employeeID"));
              o.setCustomerID((Integer) results.getObject("customerID"));
              o.setCouponID((Integer) results.getObject("couponID"));
              o.setTableID((Integer) results.getObject("tableID"));
              o.setOrder_date(results.getString("orderDate"));
              o.setTotal(results.getDouble("total"));
              o.setDiscount(results.getInt("discount"));
              o.setFin(results.getDouble("final"));
              list.add(o);
            }
            return list;

        }catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }

    }

    public Product searchOneProductById(Integer id){
        String query = "SELECT * FROM products WHERE id = ?";
        Product product = new Product();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                product.setId(results.getInt("id"));
                product.setName(results.getString("name"));
                product.setDescription(results.getString("description"));
                product.setPrice(results.getDouble("price"));
                product.setQuantity(results.getInt("quantity"));
                product.setCategory_id(results.getInt("category_id"));
                product.setImage(results.getString("image"));
                return product;

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Customer searchOneCustomerById(Integer id){
        String query = "SELECT * FROM customer WHERE id = ?";
        Customer customer = new Customer();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                customer.setId(results.getInt("id"));
                customer.setName(results.getString("name"));
                customer.setAddress(results.getString("address"));
                customer.setContact_info(results.getString("contact"));
                customer.setPoints(results.getInt("points"));
                customer.setType(results.getInt("type"));

                return customer;

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public User searchOneEmployeeById(Integer id){
        String query = "SELECT * FROM users WHERE id = ?";
        User user = new User();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet results = stmt.executeQuery();

            if (results.next()) {
                user.setId(results.getInt("id"));
                user.setUsername(results.getString("username"));
                user.setFullname(results.getString("fullname"));

                return user;

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<OrderDetail> searchAllOrderDetailByOrderID(int id){
        String query = "SELECT * FROM orderDetail WHERE orderID = ?";
        List<OrderDetail> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setId(results.getInt("id"));
                orderDetail.setOrderID(results.getInt("orderID"));
                orderDetail.setProductID(results.getInt("productID"));
                orderDetail.setQuantity(results.getInt("quantity"));
                orderDetail.setTotal(results.getDouble("total"));
                list.add(orderDetail);
            }
            return list;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean isExistCouponId(int id) throws SQLException{
        String query = "SELECT COUNT(*) FROM coupon WHERE couponID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        }
    }

    public void saveToDatabase(int id, String expiryDate, int discount) throws SQLException{
        String sql = "INSERT INTO coupon (couponID, expiry, discount) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, expiryDate);
            stmt.setInt(3, discount);
            stmt.executeUpdate();
        }
    }
}















