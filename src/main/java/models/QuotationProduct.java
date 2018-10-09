package models;

import java.io.Serializable;

public class QuotationProduct implements Serializable {

    private Product product;
    private int quantity;

    public QuotationProduct(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}