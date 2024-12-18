package model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import controller.UserSessionController;
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

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USERS_ID = "id";
    public static final String COLUMN_USERS_FULLNAME = "fullname";
    public static final String COLUMN_USERS_USERNAME = "username";
    public static final String COLUMN_USERS_EMAIL = "email";
    public static final String COLUMN_USERS_PASSWORD = "password";
    public static final String COLUMN_USERS_SALT = "salt";
    public static final String COLUMN_USERS_ADMIN = "admin";
    public static final String COLUMN_USERS_STATUS = "status";
    public static final String COLUMN_USERS_DOB = "date_of_birth";
    public static final String COLUMN_USERS_GENDER = "gender";
    public static final String COLUMN_USERS_PHONE = "phone_number";

    public static final int ORDER_BY_NONE = 1;
    public static final int ORDER_BY_ASC = 2;
    public static final int ORDER_BY_DESC = 3;
    public static final int ORDER_BY_CREATION_DATE_DESC = 1;
    private static final Datasource instance = new Datasource();
    private Connection conn;

    private Datasource() {
    }
    public static Datasource getInstance() {
        return instance;
    }

    public boolean open() {
        try {
            if (conn != null && !conn.isClosed()) {
                return true; // Connection already exists and is open
            }
            
            // Set up connection with busy timeout
            org.sqlite.SQLiteConfig config = new org.sqlite.SQLiteConfig();
            config.setBusyTimeout(5000); // 5 second timeout
            config.setJournalMode(org.sqlite.SQLiteConfig.JournalMode.WAL); // Write-Ahead Logging
            
            conn = DriverManager.getConnection(CONNECTION_STRING, config.toProperties());
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
            switch (sortOrder) {
                // Sort by ID in descending order
                case ORDER_BY_DESC:
                    queryProducts.append(COLUMN_PRODUCTS_NAME + " DESC");
                    break;
                case ORDER_BY_ASC:
                    queryProducts.append(COLUMN_PRODUCTS_NAME + " ASC");
                    break;
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
    public List<Product> searchProducts(String searchString, int sortOrder, boolean includeDisabled) {
        StringBuilder query = queryProducts();

        boolean hasSearchString = searchString != null && !searchString.trim().isEmpty();

        // Add WHERE clause only if there's a search string
        if (hasSearchString) {
            query.append(" WHERE (")
                    .append(TABLE_PRODUCTS).append(".").append(COLUMN_PRODUCTS_NAME).append(" LIKE ? OR ")
                    .append("category_name LIKE ?)");
        }

        // If no search string is provided, sort by id in descending order
        if (!hasSearchString) {
            query.append(" ORDER BY ").append(COLUMN_PRODUCTS_ID).append(" DESC");
        } else if (sortOrder != ORDER_BY_NONE) {
            // Apply sorting based on the sortOrder
            query.append(" ORDER BY ").append(COLUMN_PRODUCTS_NAME);
            query.append(sortOrder == ORDER_BY_DESC ? " DESC" : " ASC");
        }

        try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;

            // Set search parameters only if there’s a search string
            if (hasSearchString) {
                statement.setString(paramIndex++, "%" + searchString + "%"); // For product name
                statement.setString(paramIndex++, "%" + searchString + "%"); // For category name
            }

            ResultSet results = statement.executeQuery();
            List<Product> products = new ArrayList<>();

            while (results.next()) {
                Product product = new Product();
                product.setId(results.getInt(COLUMN_PRODUCTS_ID));
                product.setName(results.getString(COLUMN_PRODUCTS_NAME));
                product.setDescription(results.getString(COLUMN_PRODUCTS_DESCRIPTION));
                product.setPrice(results.getDouble(COLUMN_PRODUCTS_PRICE));
                product.setQuantity(results.getInt(COLUMN_PRODUCTS_QUANTITY));
                product.setCategory_name(results.getString("category_name"));  // Category alias from query
                product.setImage(results.getString(COLUMN_PRODUCTS_IMAGE));
                product.setDisabled(results.getInt(COLUMN_PRODUCTS_ACTIVE) == 1);  // Set based on active status
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
    private StringBuilder queryUsers() {
        return new StringBuilder("SELECT " +
                TABLE_USERS + "." + COLUMN_USERS_ID + ", " +
                TABLE_USERS + "." + COLUMN_USERS_FULLNAME + ", " +
                TABLE_USERS + "." + COLUMN_USERS_EMAIL + ", " +
                TABLE_USERS + "." + COLUMN_USERS_USERNAME + ", " +
                TABLE_USERS + "." + COLUMN_USERS_DOB + ", " +
                TABLE_USERS + "." + COLUMN_USERS_GENDER + ", " +
                TABLE_USERS + "." + COLUMN_USERS_PHONE + ", " +
                TABLE_USERS + "." + COLUMN_USERS_STATUS +
                " FROM " + TABLE_USERS +
                " WHERE " + TABLE_USERS + "." + COLUMN_USERS_ADMIN + " = 0"
        );
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
                try {
                    User user = new User();
                    user.setId(results.getInt(1));
                    user.setFullname(results.getString(2));
                    user.setEmail(results.getString(3));
                    user.setUsername(results.getString(4));

                    // Handle optional fields with null checks
                    Date dob = results.getDate(5);
                    if (!results.wasNull()) {
                        user.setDateOfBirth(dob);
                    }

                    String genderStr = results.getString(6);
                    if (genderStr != null && !genderStr.isEmpty()) {
                        try {
                            user.setGender(User.Gender.valueOf(genderStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid gender value in database: " + genderStr); // Debug print
                        }
                    }

                    user.setPhoneNumber(results.getString(7));
                    user.setStatus(results.getString(8)); // Now correctly mapped to column 8

                    users.add(user);
                    System.out.println("Loaded user: " + user.getFullname()); // Debug print
                } catch (SQLException e) {
                    System.out.println("Error processing row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return users;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list instead of null
        }
    }

    public List<User> getOneUser(int customer_id) {
        StringBuilder queryCustomers = queryUsers();
        queryCustomers.append(" AND " + TABLE_USERS + "." + COLUMN_USERS_ID + " = ?");
        
        System.out.println("Executing query: " + queryCustomers.toString()); // Debug print
        
        try (PreparedStatement statement = conn.prepareStatement(queryCustomers.toString())) {
            statement.setInt(1, customer_id);
            ResultSet results = statement.executeQuery();
            List<User> users = new ArrayList<>();

            while (results.next()) {
                try {
                User user = new User();
                user.setId(results.getInt(COLUMN_USERS_ID));
                user.setFullname(results.getString(COLUMN_USERS_FULLNAME));
                user.setEmail(results.getString(COLUMN_USERS_EMAIL));
                user.setUsername(results.getString(COLUMN_USERS_USERNAME));
                
                // Handle Date of Birth
                Date dob = results.getDate(COLUMN_USERS_DOB);
                if (!results.wasNull()) {
                    user.setDateOfBirth(dob);
                }
                
                // Handle Gender
                String genderStr = results.getString(COLUMN_USERS_GENDER);
                if (genderStr != null && !genderStr.isEmpty()) {
                    try {
                        user.setGender(User.Gender.valueOf(genderStr.toUpperCase()));
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Invalid gender value in database: " + genderStr);
                    }
                }
                
                user.setPhoneNumber(results.getString(COLUMN_USERS_PHONE));
                user.setStatus(results.getString(COLUMN_USERS_STATUS));
                    
                users.add(user);
                    System.out.println("Found user: " + user.getFullname()); // Debug print
                } catch (SQLException e) {
                    System.out.println("Error processing user row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            if (users.isEmpty()) {
                System.out.println("No users found for ID: " + customer_id); // Debug print
            }
            
            return users;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateOneUser(int customer_id, String fullName, String username, String email,
                                 String status, Date dateOfBirth, String phoneNumber, User.Gender gender,
                                 String hashedPassword, String salt) {
        StringBuilder sql = new StringBuilder("UPDATE " + TABLE_USERS + " SET "
                + COLUMN_USERS_FULLNAME + " = ?, "
                + COLUMN_USERS_USERNAME + " = ?, "
                + COLUMN_USERS_EMAIL + " = ?, "
                + COLUMN_USERS_STATUS + " = ?, "
                + COLUMN_USERS_DOB + " = ?, "
                + COLUMN_USERS_PHONE + " = ?, "
                + COLUMN_USERS_GENDER + " = ?");
        
        // Add password and salt fields to update only if a new password is provided
        if (hashedPassword != null) {
            sql.append(", " + COLUMN_USERS_PASSWORD + " = ?, "
                    + COLUMN_USERS_SALT + " = ?");
        }
        
        sql.append(" WHERE " + COLUMN_USERS_ID + " = ? AND " + COLUMN_USERS_ADMIN + " = 0");

        try (PreparedStatement statement = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            statement.setString(paramIndex++, fullName);
            statement.setString(paramIndex++, username);
            statement.setString(paramIndex++, email);
            statement.setString(paramIndex++, status);
            statement.setDate(paramIndex++, new java.sql.Date(dateOfBirth.getTime()));
            statement.setString(paramIndex++, phoneNumber);
            statement.setString(paramIndex++, gender.name());
            
            // Add password and salt parameters if provided
            if (hashedPassword != null) {
                statement.setString(paramIndex++, hashedPassword);
                statement.setString(paramIndex++, salt);
            }
            
            statement.setInt(paramIndex, customer_id);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
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
    public boolean deleteSingleUser(int customerId) {
        String sql = "DELETE FROM " + TABLE_USERS + " WHERE " + COLUMN_USERS_ID + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            int rows = statement.executeUpdate();
            System.out.println(rows + " " + TABLE_USERS + " record(s) deleted.");

            return rows > 0; // Return true if at least one record was deleted

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
        if (conn == null) {
            System.out.println("Connection is not established. Please open the connection first.");
            return false;
        }

        // Check if email already exists
        if (isEmailExists(email)) {
            System.out.println("Email already exists: " + email);
            return false;
        }

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
    public boolean insertNewUserForm(String fullName, String username, String email,
                                     String password, String salt, java.sql.Date dob,
                                     String gender, String phone, String status) {
        if (conn == null) {
            System.out.println("Connection is not established. Please open the connection first.");
            return false;
        }

        String sql = "INSERT INTO " + TABLE_USERS + " ("
                + COLUMN_USERS_FULLNAME + ", "
                + COLUMN_USERS_USERNAME + ", "
                + COLUMN_USERS_EMAIL + ", "
                + COLUMN_USERS_PASSWORD + ", "
                + COLUMN_USERS_SALT + ", "
                + COLUMN_USERS_DOB + ", "
                + COLUMN_USERS_GENDER + ", "
                + COLUMN_USERS_PHONE + ", "
                + COLUMN_USERS_ADMIN + ", "
                + COLUMN_USERS_STATUS +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, password);
            statement.setString(5, salt);
            statement.setDate(6, dob);
            statement.setString(7, gender);
            statement.setString(8, phone);
            statement.setString(9, status);

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
             ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM customer")
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

    public Product searchOneProductByName(String searchName) {
        String query = "SELECT * FROM products WHERE name = ?";
        Product product = new Product();
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, searchName);
            ResultSet results = stmt.executeQuery();
            
            if (results.next()) {
                product.setId(results.getInt("id"));
                product.setName(results.getString("name"));
                product.setDescription(results.getString("description"));
                product.setPrice(results.getDouble("price"));
                product.setQuantity(results.getInt("quantity"));
                product.setCategory_id(results.getInt("category_id"));
                product.setImage(results.getString("image"));
                product.setDisabled(results.getBoolean("active"));
                return product;
            } else {
                System.out.println("No product found with search name");
                return null;
            }
        } catch (SQLException e) {
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
            // Debug print
            System.out.println("Creating order with customer: " + (customer != null ? customer.getId() : "null"));
            
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
                
                // Handle customer and points
                if (customer != null) {
                    orderStmt.setInt(2, customer.getId());
                    
                    // Update points in a separate statement
                    String updatePointsSql = "UPDATE customer SET points = points + 50 WHERE id = ?";
                    try (PreparedStatement pointsStmt = conn.prepareStatement(updatePointsSql)) {
                        pointsStmt.setInt(1, customer.getId());
                        int pointsUpdated = pointsStmt.executeUpdate();
                        System.out.println("Points update affected rows: " + pointsUpdated); // Debug print
                    }
                } else {
                    orderStmt.setNull(2, java.sql.Types.INTEGER);
                }

                // Rest of the order creation code...
                if (coupon != null) {
                    orderStmt.setInt(3, coupon.getId());
                } else {
                    orderStmt.setNull(3, java.sql.Types.INTEGER);
                }

                if (table != null) {
                    orderStmt.setInt(4, table.getId());
                } else {
                    orderStmt.setNull(4, java.sql.Types.INTEGER);
                }

                orderStmt.setString(5, date);
                orderStmt.setDouble(6, total);
                orderStmt.setDouble(7, discount);
                orderStmt.setDouble(8, fin);
                
                int orderCreated = orderStmt.executeUpdate();
                System.out.println("Order creation affected rows: " + orderCreated); // Debug print

                // Get the generated order_id and create order details
                ResultSet rs = orderStmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    System.out.println("Created order with ID: " + orderId); // Debug print

                    String detailSql = "INSERT INTO orderDetail (orderID, productID, quantity, total) VALUES (?, ?, ?, ?)";
                    String updateProductSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
                    
                    try (PreparedStatement detailStmt = conn.prepareStatement(detailSql);
                         PreparedStatement updateProductStmt = conn.prepareStatement(updateProductSql)) {
                        
                        for (int i = 0; i < productList.size(); i++) {
                            // Order details
                            detailStmt.setInt(1, orderId);
                            detailStmt.setInt(2, productList.get(i).getId());
                            detailStmt.setInt(3, quantities.get(i));
                            
                            double productTotal = productList.get(i).getPrice() * quantities.get(i);
                            DecimalFormat format = new DecimalFormat("#.##");
                            String formattedTotal = format.format(productTotal);
                            detailStmt.setDouble(4, Double.parseDouble(formattedTotal));
                            detailStmt.addBatch();

                            // Update product quantities
                            updateProductStmt.setInt(1, quantities.get(i));
                            updateProductStmt.setInt(2, productList.get(i).getId());
                            updateProductStmt.addBatch();
                        }
                        
                        int[] detailResults = detailStmt.executeBatch();
                        int[] productResults = updateProductStmt.executeBatch();
                        
                        System.out.println("Order details created: " + detailResults.length); // Debug print
                        System.out.println("Products updated: " + productResults.length); // Debug print
                    }
                }

                conn.commit();
                System.out.println("Transaction committed successfully"); // Debug print
                
            } catch (SQLException e) {
                System.out.println("Error during order creation: " + e.getMessage());
                conn.rollback();
                throw e;
            }
        } finally {
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

    public List<Customer> getAllCustomers(int sortOrder) {
        StringBuilder query = new StringBuilder("SELECT * FROM customer");

        if (sortOrder != ORDER_BY_NONE) {
            query.append(" ORDER BY name");
            if (sortOrder == ORDER_BY_DESC) {
                query.append(" DESC");
            } else {
                query.append(" ASC");
            }
        }

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query.toString())) {

            List<Customer> customers = new ArrayList<>();
            while (results.next()) {
                Customer customer = new Customer();
                customer.setId(results.getInt("id"));
                customer.setName(results.getString("name"));
                customer.setAddress(results.getString("address"));
                customer.setContact_info(results.getString("contact"));
                customer.setPoints(results.getInt("points"));
                customers.add(customer);
            }
            return customers;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteSingleCustomer(int customerId) {
        String sql = "DELETE FROM customer WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, customerId);
            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
            return false;
        }
    }

    public List<Customer> searchCustomers(String searchString, int sortOrder) {
        StringBuilder query = new StringBuilder("SELECT * FROM customer WHERE name LIKE ? OR address LIKE ? OR contact LIKE ?");

        if (sortOrder != ORDER_BY_NONE) {
            query.append(" ORDER BY name");
            if (sortOrder == ORDER_BY_DESC) {
                query.append(" DESC");
            } else {
                query.append(" ASC");
            }
        }

        try (PreparedStatement statement = conn.prepareStatement(query.toString())) {
            String searchPattern = "%" + searchString + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            ResultSet results = statement.executeQuery();
            List<Customer> customers = new ArrayList<>();

            while (results.next()) {
                Customer customer = new Customer();
                customer.setId(results.getInt("id"));
                customer.setName(results.getString("name"));
                customer.setAddress(results.getString("address"));
                customer.setContact_info(results.getString("contact"));
                customer.setPoints(results.getInt("points"));
                customers.add(customer);
            }
            return customers;
        } catch (SQLException e) {
            System.out.println("Search failed: " + e.getMessage());
            return null;
        }
    }

    public boolean insertNewCustomer(String name, String address, String contact) {
        String sql = "INSERT INTO customer (name, address, contact, points) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, address);
            statement.setString(3, contact);
            statement.setInt(4, 0); // Initialize points to 0 for new customers

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCustomer(int customerId, String name, String address, String contact) {
        String sql = "UPDATE customer SET name = ?, address = ?, contact = ? WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, address);
            statement.setString(3, contact);
            statement.setInt(4, customerId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteOrder(int orderId) {
        // First delete related order details
        String deleteDetailsSQL = "DELETE FROM orderDetail WHERE orderID = ?";
        String deleteOrderSQL = "DELETE FROM [order] WHERE id = ?";

        try (PreparedStatement detailStmt = conn.prepareStatement(deleteDetailsSQL);
             PreparedStatement orderStmt = conn.prepareStatement(deleteOrderSQL)) {

            // Delete order details first
            detailStmt.setInt(1, orderId);
            detailStmt.executeUpdate();

            // Then delete the order
            orderStmt.setInt(1, orderId);
            int rows = orderStmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
            return false;
        }
    }

    public Customer getLastInsertedCustomer() {
        String query = "SELECT * FROM customer WHERE id = last_insert_rowid()";
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)) {
            if (results.next()) {
                Customer customer = new Customer();
                customer.setId(results.getInt("id"));
                customer.setName(results.getString("name"));
                customer.setAddress(results.getString("address"));
                customer.setContact_info(results.getString("contact"));
                customer.setPoints(results.getInt("points"));
                customer.setType(results.getInt("type"));
                return customer;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return null;
    }

    public List<OrderDetail> searchAllOrderDetailByYear(int year, int month){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String query = "SELECT * FROM [order]";
        List<OrderDetail> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            //stmt.setInt(1, id);
            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                String orderDateStr = results.getString("orderDate");

                // Parse order date to LocalDate and extract the year
                LocalDate orderDate = LocalDate.parse(orderDateStr, formatter);
                int orderYear = orderDate.getYear();
                int orderMonth = orderDate.getMonthValue();
                if(year == orderYear && month == orderMonth){
                    List<OrderDetail> tempList = searchAllOrderDetailByOrderID(results.getInt("id"));
                    list.addAll(tempList);
                    //System.out.println(tempList);
                }
//                OrderDetail orderDetail = new OrderDetail();
//                orderDetail.setId(results.getInt("id"));
//                orderDetail.setOrderID(results.getInt("orderID"));
//                orderDetail.setProductID(results.getInt("productID"));
//                orderDetail.setQuantity(results.getInt("quantity"));
//                orderDetail.setTotal(results.getDouble("total"));
//                list.add(orderDetail);
            }
            return list;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<OrderDetail> getTopThreeProducts(){
        List<OrderDetail> orderDetails = new ArrayList<>();
        String query =
                "SELECT od.productID, p.name, SUM(od.quantity) AS quantity, SUM(od.total) AS total " +
                        "FROM orderDetail od " +
                        "JOIN products p ON od.productID = p.id " +
                        "GROUP BY od.productID " +
                        "ORDER BY total DESC "; //+
                        //"LIMIT 3";  // Changed to show only top 3 products
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int productID = rs.getInt("productID");
                String productName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double total = rs.getDouble("total");
                OrderDetail detail = new OrderDetail();
                detail.setProductID(productID);
                detail.setQuantity(quantity);
                detail.setProductName(productName);
                detail.setTotal(total);
                orderDetails.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orderDetails;
    }

    public Integer countAllEmployees() {
        String query = "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COLUMN_USERS_ADMIN + " = 0";

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(query)) {
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

    public Integer countAllOrders() {
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM [order]")
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

    // Add this method to check if a product name already exists
    public boolean isProductNameExists(String name) {
        String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS + 
                      " WHERE LOWER(" + COLUMN_PRODUCTS_NAME + ") = LOWER(?)";
                  
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, name.trim());
            ResultSet results = statement.executeQuery();
            
            if (results.next()) {
                return results.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return false;
    }

    public boolean updateTableStatus(int tableId, int newStatus) {
        // First ensure we have a valid connection
        if (conn == null) {
            open();
        }
        
        String sql = "UPDATE \"table\" SET status = ? WHERE id = ?";
        
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            // Set timeout to avoid infinite waiting
            statement.setQueryTimeout(30);
            
            statement.setInt(1, newStatus);
            statement.setInt(2, tableId);
            
            int affectedRows = statement.executeUpdate();
            
            // For debugging
            System.out.println("Updating table " + tableId + " to status " + newStatus);
            System.out.println("Affected rows: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            // If we get a BUSY error, try to reconnect and retry once
            if (e.getMessage().contains("SQLITE_BUSY")) {
                try {
                    // Close and reopen connection
                    close();
                    open();
                    
                    // Retry the update
                    try (PreparedStatement retryStatement = conn.prepareStatement(sql)) {
                        retryStatement.setQueryTimeout(30);
                        retryStatement.setInt(1, newStatus);
                        retryStatement.setInt(2, tableId);
                        
                        int affectedRows = retryStatement.executeUpdate();
                        return affectedRows > 0;
                    }
                } catch (SQLException retryEx) {
                    System.out.println("Retry failed: " + retryEx.getMessage());
                    retryEx.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("Update table status failed: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean updateCustomerPoints(int customerId, int pointsToAdd) {
        String sql = "UPDATE customer SET points = points + ? WHERE id = ?";
        
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, pointsToAdd);
            statement.setInt(2, customerId);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Update points failed: " + e.getMessage());
            return false;
        }
    }

    public int getCustomerPoints(int customerId) {
        String sql = "SELECT points FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("points");
            }
        } catch (SQLException e) {
            System.out.println("Error getting customer points: " + e.getMessage());
        }
        return -1;
    }

    // Add this new method to check if an email already exists
    public boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM " + TABLE_USERS + 
                      " WHERE LOWER(" + COLUMN_USERS_EMAIL + ") = LOWER(?)";
              
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, email.trim());
            ResultSet results = statement.executeQuery();
            
            if (results.next()) {
                return results.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
        return false;
    }
}















