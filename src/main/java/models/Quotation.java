package models;

import javafx.collections.ObservableList;

import java.io.Serializable;

public class Quotation implements Serializable {

    private String quotationNumber;
    private String clientName;
    private String contactNumber;
    private String email;
    private ObservableList<QuotationProduct> products;
    private boolean newQ;

    public Quotation(String quotationNumber, String clientName, String contactNumber, String email, ObservableList<QuotationProduct> products, boolean newQ) {
        this.quotationNumber = quotationNumber;
        this.clientName = clientName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.products = products;
        this.newQ = newQ;
    }

    public String getQuotationNumber() {
        return quotationNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public ObservableList<QuotationProduct> getProducts() {
        return products;
    }

    public boolean isNewQ() {
        return newQ;
    }
}
