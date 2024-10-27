package model;

import java.sql.*;
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

                    // Handle image path
                    String imagePath = results.getString(7);
                    if (imagePath != null && !imagePath.trim().isEmpty()) {
                        // Ensure the path starts with "/" if it doesn't already
                        if (!imagePath.startsWith("/")) {
                            imagePath = "/" + imagePath;
                        }

                        // Debug information
                        System.out.println("Loading image for product " + product.getName() + ": " + imagePath);

                        // Check if file exists in resources
                        URL resourceUrl = getClass().getResource(imagePath);
                        if (resourceUrl != null) {
                            System.out.println("Found image in resources: " + resourceUrl);
                        } else {
                            // Check if file exists in project directory
                            String projectPath = System.getProperty("user.dir");
                            File imageFile = new File(projectPath + imagePath);
                            if (imageFile.exists()) {
                                System.out.println("Found image in project directory: " + imageFile.getAbsolutePath());
                            } else {
                                System.out.println("Image file not found: " + imagePath);
                            }
                        }

                        product.setImage(imagePath);
                    } else {
                        // Set a default placeholder image path if no image is specified
                        product.setImage("/view/resources/img/placeholder.png");
                    }

                    products.add(product);

                } catch (Exception e) {
                    System.err.println("Error processing product from database:");
                    e.printStackTrace();
                    // Continue processing other products even if one fails
                    continue;
                }
            }
            return products;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }
    public List<Product> getOneProduct(int product_id) {

        StringBuilder queryProducts = queryProducts();
        queryProducts.append(" WHERE " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID + " = ? LIMIT 1");
        try (PreparedStatement statement = conn.prepareStatement(String.valueOf(queryProducts))) {
            statement.setInt(1, product_id);
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
                product.setNr_sales(results.getInt(7));
                product.setCategory_id(results.getInt(8));
                product.setImage(results.getString(9));
                products.add(product);
            }
            return products;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
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
                product.setNr_sales(results.getInt(8)); // Note: nr_sales is now column 8
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
                TABLE_CATEGORIES + "." + COLUMN_CATEGORIES_NAME + ", " +
                TABLE_PRODUCTS   + "." + COLUMN_PRODUCTS_IMAGE + ", " +
                " (SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID + " = " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID + ") AS nr_sales" + ", " +
                TABLE_CATEGORIES + "." + COLUMN_CATEGORIES_ID +
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
                                    int quantity, int category_id, String imagePath) {
        String sql = "INSERT INTO " + TABLE_PRODUCTS + " ("
                + COLUMN_PRODUCTS_NAME + ", "
                + COLUMN_PRODUCTS_DESCRIPTION + ", "
                + COLUMN_PRODUCTS_PRICE + ", "
                + COLUMN_PRODUCTS_QUANTITY + ", "
                + COLUMN_PRODUCTS_CATEGORY_ID + ", "
                + COLUMN_PRODUCTS_IMAGE
                + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, price);
            statement.setInt(4, quantity);
            statement.setInt(5, category_id);
            statement.setString(6, imagePath);

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateOneProduct(int product_id, String name, String description, double price, int quantity, int category_id, String imagePath) {

        String sql = "UPDATE " + TABLE_PRODUCTS + " SET "
                + COLUMN_PRODUCTS_NAME + " = ?, "
                + COLUMN_PRODUCTS_DESCRIPTION + " = ?, "
                + COLUMN_PRODUCTS_PRICE + " = ?, "
                + COLUMN_PRODUCTS_QUANTITY + " = ?, "
                + COLUMN_PRODUCTS_CATEGORY_ID + " = ?, "
                + COLUMN_PRODUCTS_IMAGE + " = ? "
                + "WHERE " + COLUMN_PRODUCTS_ID + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setDouble(3, price);
            statement.setInt(4, quantity);
            statement.setInt(5, category_id);
            statement.setString(6, imagePath);
            statement.setInt(7, product_id);

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
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

    public List<Employee> getAllEmployees(int sortOrder) {

        StringBuilder queryEmployees = queryEmployees();

        if (sortOrder != ORDER_BY_NONE) {
            queryEmployees.append(" ORDER BY ");
            queryEmployees.append(COLUMN_USERS_FULLNAME);
            if (sortOrder == ORDER_BY_DESC) {
                queryEmployees.append(" DESC");
            } else {
                queryEmployees.append(" ASC");
            }
        }
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(queryEmployees.toString())) {

            List<Employee> employees = new ArrayList<>();
            while (results.next()) {
                Employee employee = new Employee();
                employee.setId(results.getInt(1));
                employee.setFullname(results.getString(2));
                employee.setEmail(results.getString(3));
                employee.setUsername(results.getString(4));
                employee.setOrders(results.getInt(5));
                employee.setStatus(results.getString(6));
                employees.add(employee);
            }
            return employees;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public List<Employee> getOneCustomer(int customer_id) {

        StringBuilder queryCustomers = queryEmployees();
        queryCustomers.append(" AND " + TABLE_USERS + "." + COLUMN_USERS_ID + " = ?");
        try (PreparedStatement statement = conn.prepareStatement(String.valueOf(queryCustomers))) {
            statement.setInt(1, customer_id);
            ResultSet results = statement.executeQuery();
            List<Employee> employees = new ArrayList<>();
            while (results.next()) {
                Employee employee = new Employee();
                employee.setId(results.getInt(1));
                employee.setFullname(results.getString(2));
                employee.setEmail(results.getString(3));
                employee.setUsername(results.getString(4));
                employee.setOrders(results.getInt(5));
                employee.setStatus(results.getString(6));
                employees.add(employee);
            }
            return employees;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public List<Employee> searchCustomers(String searchString, int sortOrder) {

        StringBuilder queryCustomers = queryEmployees();

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

            List<Employee> employees = new ArrayList<>();
            while (results.next()) {
                Employee employee = new Employee();
                employee.setId(results.getInt(1));
                employee.setFullname(results.getString(2));
                employee.setEmail(results.getString(3));
                employee.setUsername(results.getString(4));
                employee.setOrders(results.getInt(5));
                employee.setStatus(results.getString(6));
                employees.add(employee);
            }
            return employees;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    private StringBuilder queryEmployees() {
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

    public boolean deleteSingleCustomer(int customerId) {
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

    public List<Order> getAllOrders(int sortOrder) {

        StringBuilder queryOrders = new StringBuilder("SELECT " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_ID + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID + ", " +
                TABLE_USERS + "." + COLUMN_USERS_FULLNAME + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_DATE + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_STATUS + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_NAME + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_PRICE +
                " FROM " + TABLE_ORDERS
        );

        queryOrders.append("" +
                " LEFT JOIN " + TABLE_PRODUCTS +
                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID +
                " = " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID);
        queryOrders.append("" +
                " LEFT JOIN " + TABLE_USERS +
                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID +
                " = " + TABLE_USERS + "." + COLUMN_USERS_ID);

        if (sortOrder != ORDER_BY_NONE) {
            queryOrders.append(" ORDER BY ");
            queryOrders.append(COLUMN_USERS_FULLNAME);
            if (sortOrder == ORDER_BY_DESC) {
                queryOrders.append(" DESC");
            } else {
                queryOrders.append(" ASC");
            }
        }

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(queryOrders.toString())) {

            List<Order> orders = new ArrayList<>();
            while (results.next()) {
                Order order = new Order();
                order.setId(results.getInt(1));
                order.setProduct_id(results.getInt(2));
                order.setUser_id(results.getInt(3));
                order.setUser_full_name(results.getString(4));
                order.setOrder_date(results.getString(5));
                order.setOrder_status(results.getString(6));
                order.setProduct_name(results.getString(7));
                order.setOrder_price(results.getDouble(8));
                orders.add(order);
            }
            return orders;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public List<Order> getAllUserOrders(int sortOrder, int user_id) {

        StringBuilder queryOrders = new StringBuilder("SELECT " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_ID + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID + ", " +
                TABLE_USERS + "." + COLUMN_USERS_FULLNAME + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_DATE + ", " +
                TABLE_ORDERS + "." + COLUMN_ORDERS_ORDER_STATUS + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_NAME + ", " +
                TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_PRICE +
                " FROM " + TABLE_ORDERS
        );

        queryOrders.append("" +
                " LEFT JOIN " + TABLE_PRODUCTS +
                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_PRODUCT_ID +
                " = " + TABLE_PRODUCTS + "." + COLUMN_PRODUCTS_ID);
        queryOrders.append("" +
                " LEFT JOIN " + TABLE_USERS +
                " ON " + TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID +
                " = " + TABLE_USERS + "." + COLUMN_USERS_ID);
        queryOrders.append(" WHERE " + TABLE_ORDERS + "." + COLUMN_ORDERS_USER_ID + " = ").append(user_id);

        if (sortOrder != ORDER_BY_NONE) {
            queryOrders.append(" ORDER BY ");
            queryOrders.append(COLUMN_USERS_FULLNAME);
            if (sortOrder == ORDER_BY_DESC) {
                queryOrders.append(" DESC");
            } else {
                queryOrders.append(" ASC");
            }
        }

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(queryOrders.toString())) {

            List<Order> orders = new ArrayList<>();
            while (results.next()) {
                Order order = new Order();
                order.setId(results.getInt(1));
                order.setProduct_id(results.getInt(2));
                order.setUser_id(results.getInt(3));
                order.setUser_full_name(results.getString(4));
                order.setOrder_date(results.getString(5));
                order.setOrder_status(results.getString(6));
                order.setProduct_name(results.getString(7));
                order.setOrder_price(results.getDouble(8));
                orders.add(order);
            }
            return orders;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }


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
}















