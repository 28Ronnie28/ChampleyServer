package models;

import java.io.Serializable;

public class Product implements Serializable {

    private int productID;
    private String description;
    private String category;
    private String supplier;
    private int supplierID;
    private double price;

    public Product(int productID, String description, String category, String supplier, int supplierID, double price) {
        this.productID = productID;
        this.description = description;
        this.category = category;
        this.supplier = supplier;
        this.supplierID = supplierID;
        this.price = price;
    }

    public int getProductID() {
        return productID;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getSupplier() {
        return supplier;
    }

    public int getSupplierID() {
        return supplierID;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString(){
        return description + " - " + category + " - " + supplier + " - R " + price;
    }
}
