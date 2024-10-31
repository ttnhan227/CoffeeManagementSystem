package model;

public class Customer {
    private int id;
    private String name;
    private String address;
    private String contact_info;
    private int points;
    private int type;

    public int getId() {
        return id;
    }

    public int getPoints() {
        return points;
    }

    public int getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getContact_info() {
        return contact_info;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setContact_info(String contact_info) {
        this.contact_info = contact_info;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setType(int type) {
        this.type = type;
    }
}
