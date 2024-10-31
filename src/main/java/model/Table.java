package model;

public class Table {
    private int id;
    private int status;
    private int capacity;

    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
