package models;

import java.io.Serializable;

public class Supplier implements Serializable {

    private int id;
    private String name;
    private String contactNumber;
    private String email;
    private String address;

    public Supplier(int id, String name, String contactNumber, String email, String address) {
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getContactDetails() {
        return "Name: " + name + "\nEmail: " + contactNumber + "\nContact Number: " + contactNumber + "\nAddress: " + address;
    }

    @Override
    public String toString(){
        return id + " - " + name;
    }
}
