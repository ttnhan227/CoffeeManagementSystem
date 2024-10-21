package app.db;

import java.sql.*;

public class ViewDB {

    // Database name and connection string
    public static final String DB_NAME = "store_manager.sqlite";
    public static final String CONNECTION_STRING = "jdbc:sqlite:D:/CODE/code/Java/StoreManagementSystem/src/app/db/" + DB_NAME;

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // 1. Establish a connection to the database
            conn = DriverManager.getConnection(CONNECTION_STRING);
            System.out.println("Connection to SQLite has been established.");

            // 2. Create a statement to execute SQL queries
            stmt = conn.createStatement();

            // 3. Execute a query to retrieve data from the products table
            String sql = "SELECT * FROM products"; // Modify this if your table name is different
            rs = stmt.executeQuery(sql);

            // 4. Process the ResultSet
            System.out.println("Product List:");
            while (rs.next()) {
                // Assuming the products table has columns: id, name, and price
                int id = rs.getInt("id"); // Change this if your column names are different
                String name = rs.getString("name");
                double price = rs.getDouble("price");

                // Print the product details
                System.out.printf("ID: %d, Name: %s, Price: %.2f%n", id, name, price);
            }
        } catch (SQLException e) {
            System.out.println("Error occurred: " + e.getMessage());
        } finally {
            // Close resources in reverse order
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
