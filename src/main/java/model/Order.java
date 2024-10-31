package model;


public class Order {
    private int id;
    private Integer employeeID;
    private Integer customerID;
    private Integer couponID;
    private Integer tableID;
    private String order_date;
    private Double total;
    private int discount;
    private Double fin;

    public int getId() {
        return id;
    }

    public int getDiscount() {
        return discount;
    }

    public Double getFin() {
        return fin;
    }

    public Double getTotal() {
        return total;
    }

    public Integer getCouponID() {
        return couponID;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public Integer getEmployeeID() {
        return employeeID;
    }

    public Integer getTableID() {
        return tableID;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public void setCouponID(Integer couponID) {
        this.couponID = couponID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public void setEmployeeID(Integer employeeID) {
        this.employeeID = employeeID;
    }

    public void setFin(Double fin) {
        this.fin = fin;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }

    public void setTableID(Integer tableID) {
        this.tableID = tableID;
    }
}
