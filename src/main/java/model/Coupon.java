package model;

public class Coupon {
    private int id;
    private String expiry;
    private int discount;

    public int getId() {
        return id;
    }

    public int getDiscount() {
        return discount;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
}
