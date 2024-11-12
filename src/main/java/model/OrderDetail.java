package model;

public class OrderDetail {
    private int id;
    private int orderID;
    private int productID;
    private int quantity;
    private Double total;
    private String productName;

    public int getId() {
        return id;
    }

    public int getOrderID() {
        return orderID;
    }

    public int getProductID() {
        return productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public Double getTotal() {
        return total;
    }

    public String getProductName() {
        return productName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
