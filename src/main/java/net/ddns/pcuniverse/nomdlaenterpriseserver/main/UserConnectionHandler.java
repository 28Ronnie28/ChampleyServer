package net.ddns.pcuniverse.nomdlaenterpriseserver.main;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.*;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class UserConnectionHandler extends ConnectionHandler implements Runnable {

    private String username;
    private ObjectProperty<User> user = new SimpleObjectProperty<>();
    private ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<DataFile> quotations = FXCollections.observableArrayList();
    private ObservableList<DataFile> invoices = FXCollections.observableArrayList();
    private ObservableList<DataFile> documents = FXCollections.observableArrayList();
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private volatile ObservableList<Object> outputQueue = FXCollections.observableArrayList();
    volatile BooleanProperty updateUser = new SimpleBooleanProperty(false);
    volatile BooleanProperty updateSuppliers = new SimpleBooleanProperty(false);
    volatile BooleanProperty updateProducts = new SimpleBooleanProperty(false);
    volatile BooleanProperty updateQuotations = new SimpleBooleanProperty(false);
    volatile BooleanProperty updateInvoices = new SimpleBooleanProperty(false);
    volatile BooleanProperty updateDocuments = new SimpleBooleanProperty(false);
    volatile BooleanProperty updateCategories = new SimpleBooleanProperty(false);

    public UserConnectionHandler(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, String username, ObservableList<ConnectionHandler> connectionsList, DatabaseHandler dh) {
        super(socket, objectInputStream, objectOutputStream, connectionsList, dh);
        this.username = username;
    }

    public void run() {
        updateUser.addListener((obs, oldV, newV) -> {
            if (newV) {
                updateUser();
                updateUser.set(false);
            }
        });
        updateSuppliers.addListener((obs, oldV, newV) -> {
            if (newV) {
                updateSuppliers();
                updateSuppliers.set(false);
            }
        });
        updateProducts.addListener((obs, oldV, newV) -> {
            if (newV) {
                updateProducts();
                updateProducts.set(false);
            }
        });
        updateQuotations.addListener((obs, oldV, newV) -> {
            if (newV) {
                updateQuotations();
                updateQuotations.set(false);
            }
        });
        updateInvoices.addListener((obs, oldV, newV) -> {
            if (newV) {
                updateInvoices();
                updateInvoices.set(false);
            }
        });
        updateDocuments.addListener((obs, oldV, newV) -> {
            if (newV) {
                updateDocuments();
                updateDocuments.set(false);
            }
        });
        updateCategories.addListener((obs, oldV, newV) -> {
            if (newV) {
                updateCategories();
                updateCategories.set(false);
            }
        });
        user.addListener(e -> {
            outputQueue.add(0, user.get());
        });
        suppliers.addListener((InvalidationListener) e -> {
            outputQueue.add(0, Arrays.asList(suppliers.toArray()));
        });
        products.addListener((InvalidationListener) e -> {
            outputQueue.add(0, Arrays.asList(products.toArray()));
        });
        quotations.addListener((InvalidationListener) e -> {
            outputQueue.add(0, Arrays.asList(quotations.toArray()));
        });
        invoices.addListener((InvalidationListener) e -> {
            outputQueue.add(0, Arrays.asList(invoices.toArray()));
        });
        documents.addListener((InvalidationListener) e -> {
            outputQueue.add(0, Arrays.asList(documents.toArray()));
        });
        categories.addListener((InvalidationListener) e -> {
            outputQueue.add(0, Arrays.asList(categories.toArray()));
        });
        updateUser();
        updateSuppliers();
        updateProducts();
        updateQuotations();
        updateInvoices();
        updateDocuments();
        updateCategories();
        new InputProcessor().start();
        new OutputProcessor().start();
    }

    private class InputProcessor extends Thread {
        public void run() {
            while (running.get()) {
                Object input;
                if ((input = getReply()) != null) {
                    if (input instanceof String) {
                        String text = input.toString();
                        if (text.startsWith("lo:")) {

                        } else if (text.startsWith("cp:")) {
                            dh.log("User " + username + "> Requested Change Password");
                            changePassword(text.substring(3).split(":")[0], text.substring(3).split(":")[1]);
                        }  else if (text.startsWith("gf:")) {
                            dh.log("User " + username + "> Requested File: " + text.substring(3).split(":")[1] + " From FileType: " + text.substring(3).split(":")[0]);
                            getFile(text.substring(3).split(":")[0], text.substring(3).split(":")[1]);
                        } else if (text.startsWith("ac:")) {
                            dh.log("User " + username + "> Added Category: " + text.substring(3));
                            dh.addCategory(text.substring(3));
                            updateCategories.setValue(true);
                        } else if (text.startsWith("rc:")) {
                            dh.log("User " + username + "> Removed Category: " + text.substring(3));
                            dh.removeCategory(Integer.parseInt(text.substring(3)));
                            updateCategories.setValue(true);
                        } else if (text.startsWith("rp:")) {
                            dh.log("User " + username + "> Removed Product: " + text.substring(3));
                            dh.removeProduct(Integer.parseInt(text.substring(3)));
                            updateProducts.setValue(true);
                        } else if (text.startsWith("rs:")) {
                            dh.log("User " + username + "> Removed Supplier: " + text.substring(3));
                            dh.removeSupplier(Integer.parseInt(text.substring(3)));
                            updateSuppliers.setValue(true);
                        } else if (text.startsWith("rd:")) {
                            dh.log("User " + username + "> Removed Document: " + text.substring(3));
                            dh.deleteFile(text.substring(3).split(":")[0], text.substring(3).split(":")[1]);
                            updateDocuments.setValue(true);
                        } else if (text.startsWith("rq:")) {
                            dh.log("User " + username + "> Removed Quotation: " + text.substring(3));
                            dh.deleteFile(text.substring(3).split(":")[0], text.substring(3).split(":")[1]);
                        } else if (text.startsWith("ri:")) {
                            dh.log("User " + username + "> Removed Invoice: " + text.substring(3));
                            dh.deleteFile(text.substring(3).split(":")[0], text.substring(3).split(":")[1]);
                        } else if (text.startsWith("se:")) {
                            if((!text.substring(3).split(":")[3].matches(""))) {
                                dh.log("User " + username + "> Emailed " + text.substring(3).split(":")[4] + "(" + text.substring(3).split(":")[3] + " to: " + text.substring(3).split(":")[0]);
                                if (new Email().email(text.substring(3).split(":")[0], text.substring(3).split(":")[1], text.substring(3).split(":")[2], text.substring(3).split(":")[3], text.substring(3).split(":")[4])) {
                                    outputQueue.add("es:t");
                                } else {
                                    outputQueue.add("es:f");
                                }
                            } else {
                                dh.log("User " + username + "> Emailed: " + text.substring(3).split(":")[0]);
                                if (new Email().email(text.substring(3).split(":")[0], text.substring(3).split(":")[1], text.substring(3).split(":")[2], null, null)) {
                                    outputQueue.add("es:t");
                                } else {
                                    outputQueue.add("es:f");
                                }
                            }
                        } else if (text.startsWith("pq:")) {
                            dh.log("User " + username + "> Processed Quotation " + text.substring(3).split(":")[0] + " to an invoice.");
                            dh.processQuotationToInvoice(text.substring(3).split(":")[0]);
                            updateInvoices.setValue(true);
                        } else {
                            dh.log("User " + username + "> Requested Unknown Command: " + input);
                            System.out.println("Server> Unknown command: " + input);
                        }
                    } else if (input instanceof Quotation){
                        dh.createQuotation((Quotation) input);
                        updateQuotations.setValue(true);
                    } else if (input instanceof Product) {
                        if (((Product) input).getProductID() == -1) {
                            dh.addProduct((Product)input);
                        } else {
                            dh.updateProduct((Product)input);
                        }
                        updateProducts.setValue(true);
                    } else if (input instanceof Supplier) {
                        if (((Supplier) input).getId() == -1) {
                            dh.addSupplier((Supplier)input);
                        } else {
                            dh.updateSupplier((Supplier)input);
                        }
                        updateSuppliers.set(true);
                    } else if (input instanceof UploadFile){
                        try {
                            UploadFile uploadFile = (UploadFile) input;
                            File newFile = new File(Server.APPLICATION_FOLDER.getAbsolutePath() + "/" + uploadFile.getFileType() + "/" + uploadFile.getFileName());
                            newFile.getParentFile().mkdirs();
                            Files.write(newFile.toPath(), uploadFile.getFileData());
                            if (uploadFile.getFileType().matches("Documents")) {
                                updateDocuments.setValue(true);
                            } else if (uploadFile.getFileType().matches("Quotations")) {
                                updateQuotations.setValue(true);
                            } else if (uploadFile.getFileType().matches("Invoices")) {
                                updateInvoices.setValue(true);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (input instanceof Quotation) {
                        if (((Quotation) input).isNewQ()) {
                            dh.addQuotation((Quotation) input);
                        } else {
                            dh.updateQuotation((Quotation)input);
                        }
                        updateQuotations.setValue(true);
                    }
                }
            }
        }
    }

    private class OutputProcessor extends Thread {
        public void run() {
            while (running.get()) {
                try {
                    if (!outputQueue.isEmpty()) {
                        Object out = outputQueue.get(0);
                        if (out instanceof List && (((List) out).isEmpty() || ((List) out).get(0) == null)) {
                            outputQueue.remove(out);
                        } else {
                            sendData(out);
                            dh.log("User " + username + "> OutputProcessor> Sent: " + out + " (" + out.getClass().toString() + ")");
                            outputQueue.remove(out);
                        }
                    }
                    Thread.sleep(20);
                } catch (Exception ex) {
                    dh.log("Server> OutputProcessor> " + ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    public void sendData(Object data) {
        try {
            synchronized (objectOutputStream) {
                System.out.println(data);
                objectOutputStream.writeObject(data);
                objectOutputStream.flush();
                objectOutputStream.reset();
            }
        } catch (Exception ex) {
            terminateConnection();
            dh.log("Server> sendData> " + ex);
            ex.printStackTrace();
        }
    }

    private void changePassword(String prevPassword, String newPassword) {
        String sPassword = dh.getUserPassword(username);
        if (prevPassword.matches(sPassword) && dh.changeUserPassword(username, newPassword)) {
            outputQueue.add(0, "cp:y");
        } else {
            outputQueue.add(0, "cp:n");
        }
    }

    private void getFile(String fileType, String fileName) {
        File file = new File(Server.APPLICATION_FOLDER + "/" + fileType + "/" + fileName);
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            int size = 0;
            while (size < fileBytes.length) {
                System.out.println(Math.min(Server.BUFFER_SIZE, fileBytes.length - size));
                outputQueue.add(new FilePart(Arrays.copyOfRange(fileBytes, size, size + Math.min(Server.BUFFER_SIZE, fileBytes.length - size)), fileName));
                size += Math.min(Server.BUFFER_SIZE, fileBytes.length - size);
                dh.log("User " + username + "> Successfully Downloaded " + fileType + ": " + fileName);
            }
        } catch (Exception ex) {
            dh.log("Server> getFile> " + ex);
            ex.printStackTrace();
        }
    }

    public User getUser() {
        return user.getValue();
    }

    private void updateUser() {
        user.setValue(dh.getUser(username));
    }

    private void updateSuppliers() {
        suppliers.clear();
        suppliers.addAll(dh.getSuppliers());
    }

    private void updateProducts() {
        products.clear();
        products.addAll(dh.getProducts());
    }

    private void updateQuotations() {
        quotations.clear();
        quotations.addAll(dh.getQuotations());
    }

    private void updateInvoices() {
        invoices.clear();
        invoices.addAll(dh.getInvoices());
    }

    private void updateDocuments() {
        documents.clear();
        documents.addAll(dh.getDocuments());
    }

    private void updateCategories() {
        categories.clear();
        categories.addAll(dh.getCategories());
    }

    public String getUsername() {
        return username;
    }

}
