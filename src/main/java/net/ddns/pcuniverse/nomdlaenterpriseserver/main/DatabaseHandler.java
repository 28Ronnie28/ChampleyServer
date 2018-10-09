package net.ddns.pcuniverse.nomdlaenterpriseserver.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jxl.Sheet;
import jxl.Workbook;
import models.*;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHandler {

    private Connection con;

    DatabaseHandler() {
        connectDB();
    }

    //<editor-fold desc="Database Connection">
    private void connectDB() {
        try {
            Boolean createDatabase = false;
            if (!Server.DATABASE_FILE.exists()) {
                createDatabase = true;
            }
            con = DriverManager.getConnection("jdbc:sqlite:" + Server.DATABASE_FILE.getAbsolutePath());
            if (createDatabase) {
                Statement stmt = con.createStatement();
                stmt.execute("CREATE TABLE USER (" +
                        "Username TEXT PRIMARY KEY, " +
                        "FirstName TEXT, " +
                        "LastName TEXT, " +
                        "Password TEXT, " +
                        "Email TEXT, " +
                        "ContactNumber TEXT);");
                stmt.execute("CREATE TABLE PRODUCTS (" +
                        "ProductID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "SupplierID TEXT, " +
                        "CategoryID TEXT, " +
                        "Description TEXT, " +
                        "Price TEXT);");
                stmt.execute("CREATE TABLE SUPPLIERS (" +
                        "SupplierID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "SupplierName TEXT, " +
                        "ContactNumber TEXT, " +
                        "Email TEXT, " +
                        "Address TEXT);");
                stmt.execute("CREATE TABLE CATEGORIES (" +
                        "CategoryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "CategoryName TEXT);");
                log("Server> Created Database");
            }
            log("Server> Connected to database");
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> connectDB> " + ex);
            System.exit(0);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Authorisation">
    Boolean authoriseUser(String username, String password) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM USER WHERE Username = ? AND Password = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            return preparedStatement.executeQuery().next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> authoriseUser> " + username + "> " + ex);
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Getters">
    User getUser(String username) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM USER WHERE Username = ?;");
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            User user = new User(rs.getString("Username"), rs.getString("FirstName"), rs.getString("LastName"), rs.getString("Email"), rs.getString("ContactNumber"));
            log("Server> Successfully Created User: " + username);
            return user;
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> getUser> " + username + "> " + ex);
            return null;
        }
    }

    String getUserPassword(String username) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT Password FROM USER WHERE Username = ?;");
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                log("Server> Successfully Gotten Password For User: " + username);
                return rs.getString("Password");
            } else {
                log("Server> Failed To Get Password For User: " + username);
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> getUserPassword> " + ex);
            return null;
        }
    }

    List<Supplier> getSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM SUPPLIERS;");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                suppliers.add(new Supplier(rs.getInt("SupplierID"), rs.getString("SupplierName"), rs.getString("ContactNumber"), rs.getString("Email"), rs.getString("Address")));
            }
            log("Server> Successfully Created Suppliers");
            return suppliers;
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> getSuppliers> " + ex);
            return null;
        }
    }

    List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM PRODUCTS;");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                products.add(new Product(rs.getInt("ProductID"), rs.getString("Description"), getCategoryName(rs.getInt("CategoryID")), getSupplierName(rs.getInt("SupplierID")), rs.getInt("SupplierID"), rs.getDouble("Price")));
            }
            log("Server> Successfully Created Suppliers");
            return products;
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> getProducts> " + ex);
            return null;
        }
    }

    List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM CATEGORIES;");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("CategoryID") + " - " + rs.getString("CategoryName"));
            }
            log("Server> Successfully Gotten Categories");
            return categories;
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> getProducts> " + ex);
            return null;
        }
    }

    String getCategoryName(int id) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT CategoryName FROM CATEGORIES WHERE CategoryID = ?;");
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                log("Server> Successfully Gotten Category For ID: " + id);
                return rs.getString("CategoryName");
            } else {
                log("Server> Failed To Get Category For ID: " + id);
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> getCategoryName> " + ex);
            return null;
        }
    }

    String getSupplierName(int id) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT SupplierName FROM SUPPLIERS WHERE SupplierID = ?;");
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                log("Server> Successfully Gotten SupplierName For ID: " + id);
                return rs.getString("SupplierName");
            } else {
                log("Server> Failed To Get SupplierName For ID: " + id);
                return null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> getSupplierName> " + ex);
            return null;
        }
    }

    ObservableList<DataFile> getQuotations(){
        ObservableList<DataFile> quotations = FXCollections.observableArrayList();
        File classFilesDirectory = new File(Server.QUOTATIONS_FOLDER.getAbsolutePath());
        if (classFilesDirectory.exists()) {
            for (File file : classFilesDirectory.listFiles()) {
                quotations.add(new DataFile("Quotations", file.getName().substring(0, file.getName().lastIndexOf(".")), file.getName().substring(file.getName().lastIndexOf("."), file.getName().length()), (int) file.length()));
            }
        }
        log("Server> Successfully Gotten Documents");
        return quotations;
    }

    ObservableList<DataFile> getInvoices(){
        ObservableList<DataFile> invoices = FXCollections.observableArrayList();
        File classFilesDirectory = new File(Server.INVOICE_FOLDER.getAbsolutePath());
        if (classFilesDirectory.exists()) {
            for (File file : classFilesDirectory.listFiles()) {
                invoices.add(new DataFile("Invoices", file.getName().substring(0, file.getName().lastIndexOf(".")), file.getName().substring(file.getName().lastIndexOf("."), file.getName().length()), (int) file.length()));
            }
        }
        log("Server> Successfully Gotten Documents");
        return invoices;
    }

    ObservableList<DataFile> getDocuments(){
        ObservableList<DataFile> documents = FXCollections.observableArrayList();
        File classFilesDirectory = new File(Server.DOCUMENTS_FOLDER.getAbsolutePath());
        if (classFilesDirectory.exists()) {
            for (File file : classFilesDirectory.listFiles()) {
                documents.add(new DataFile("Documents", file.getName().substring(0, file.getName().lastIndexOf(".")), file.getName().substring(file.getName().lastIndexOf("."), file.getName().length()), (int) file.length()));
            }
        }
        log("Server> Successfully Gotten Documents");
        return documents;
    }

    /*private byte[] getContactImage(String contactID) {
        try {
            return Files.readAllBytes(new File(Server.CONTACT_IMAGES + "/" + contactID + "/profile.jpg").toPath());
        } catch (Exception ex) {
            System.out.println("Server> Can't find picture for contact, " + contactID);
        }
        return null;
    }*/

    /*private List<ClassFile> getFiles(int classID) {
        List<ClassFile> files = new ArrayList<>();
        File classFilesDirectory = new File(Server.FILES_FOLDER.getAbsolutePath() + "/" + classID);
        if (classFilesDirectory.exists()) {
            for (File file : classFilesDirectory.listFiles()) {
                files.add(new ClassFile(classID, file.getName(), (int) file.length()));
            }
        }
        log("Server> Successfully Gotten Files: ");
        return files;
    }*/
    //</editor-fold>

    //<editor-fold desc="Change Password">
    Boolean changeUserPassword(String studentNumber, String newPassword) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE Student SET Password = ? WHERE StudentNumber = ?;");
            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, studentNumber);
            log("Server> Successfully Changed Password For User: " + studentNumber);
            return preparedStatement.executeUpdate() != 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> changeStudentPassword> " + ex);
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Email Passwords">
    void emailUserPassword(String studentNumber) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT Password, Email FROM Student WHERE StudentNumber = ?;");
            preparedStatement.setString(1, studentNumber);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                log("Server> Successfully Emailed Password For User: " + studentNumber);
                String email = rs.getString("Email");
                String password = rs.getString("Password");
                new Thread(() -> Email.emailPassword(studentNumber, email, password)).start();
            } else {
                log("Server> Failed To Email Password For User: " + studentNumber);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> emailStudentPassword> " + ex);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Adders">
    void addProduct(Product product) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO PRODUCTS (SupplierID, CategoryID, Description, Price) VALUES (?,?,?,?);");
            preparedStatement.setInt(1, Integer.parseInt(product.getSupplier().substring(0, product.getSupplier().indexOf(" -"))));
            preparedStatement.setInt(2,  Integer.parseInt(product.getCategory().substring(0, product.getCategory().indexOf(" -"))));
            preparedStatement.setString(3, product.getDescription());
            preparedStatement.setDouble(4, product.getPrice());
            log("Admin> Successfully Added Product: " + product.getDescription());
            preparedStatement.execute();
            //notifyUpdatedStudent(s[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> addProduct> " + ex);
        }
    }

    void addSupplier(Supplier supplier) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO SUPPLIERS (SupplierName, ContactNumber, Email, Address) VALUES (?,?,?,?);");
            preparedStatement.setString(1, supplier.getName());
            preparedStatement.setString(2, supplier.getContactNumber());
            preparedStatement.setString(3, supplier.getEmail());
            preparedStatement.setString(4, supplier.getAddress());
            log("Server> Successfully Added Supplier: " + supplier.getName());
            preparedStatement.execute();
            //notifyUpdatedStudent(s[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> addSupplier> " + ex);
        }
    }

    void addCategory(String category) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO CATEGORIES (CategoryName) VALUES (?);");
            preparedStatement.setString(1, category);
            log("Server> Successfully Added Category: " + category);
            preparedStatement.execute();
            //notifyUpdatedStudent(s[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> addSupplier> " + ex);
        }
    }

    void addQuotation(Quotation quotation){

    }
    //</editor-fold>

    //<editor-fold desc="Notify">

    //</editor-fold>

    //<editor-fold desc="Updaters">
    void updateProduct(Product product) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE PRODUCTS SET SupplierID = ?, CategoryID = ?,  Description = ?, Price = ? WHERE ProductID = ?;");
            preparedStatement.setInt(1, Integer.parseInt(product.getSupplier().substring(0, product.getSupplier().indexOf(" -"))));
            preparedStatement.setInt(2,  Integer.parseInt(product.getCategory().substring(0, product.getCategory().indexOf(" -"))));
            preparedStatement.setString(3, product.getDescription());
            preparedStatement.setDouble(4, product.getPrice());
            preparedStatement.setInt(5, product.getProductID());
            log("Admin> Successfully Updated Product: " + product.getDescription());
            preparedStatement.execute();
            //notifyUpdatedStudent(s[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> updateProduct> " + ex);
        }
    }

    void updateSupplier(Supplier supplier) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE SUPPLIERS SET SupplierName = ?, ContactNumber = ?, Email = ?, Address ? WHERE SupplierID = ?;");
            preparedStatement.setString(1, supplier.getName());
            preparedStatement.setString(2, supplier.getContactNumber());
            preparedStatement.setString(3, supplier.getEmail());
            preparedStatement.setString(4, supplier.getAddress());
            preparedStatement.setInt(5, supplier.getId());
            log("Server> Successfully Updated Supplier: " + supplier.getName());
            preparedStatement.execute();
            //notifyUpdatedStudent(s[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> updateSupplier> " + ex);
        }
    }

    void updateQuotation(Quotation quotation) {

    }
    //</editor-fold>

    //<editor-fold desc="Remove">
    void removeProduct(int id) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM PRODUCTS WHERE ProductID = ?;");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            log("Server> Successfully Removed Product ID: " + id);
            //notifyUpdatedStudent(studentNumber);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> removeProduct> " + ex);
        }
    }

    void removeSupplier(int id) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM SUPPLIERS WHERE SupplierID = ?;");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            preparedStatement = con.prepareStatement("DELETE FROM PRODUCTS WHERE SupplierID = ?;");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            log("Server> Successfully Removed Supplier ID: " + id);
            //notifyUpdatedStudent(studentNumber);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> removeSupplier> " + ex);
        }
    }

    void removeCategory(int id) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM CATEGORIES WHERE CategoryID = ?;");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            preparedStatement = con.prepareStatement("DELETE FROM PRODUCTS WHERE CategoryID = ?;");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            log("Server> Successfully Removed Category ID: " + id);
            //notifyUpdatedStudent(studentNumber);
        } catch (SQLException ex) {
            ex.printStackTrace();
            log("Server> removeCategory> " + ex);
        }
    }
    //</editor-fold>

    /*private void saveLecturerImage(String lecturerNumber, byte[] imageBytes) {
        try {
            if (imageBytes != null) {
                File newFile = new File(Server.LECTURER_IMAGES + "/" + lecturerNumber + "/profile.jpg");
                newFile.getParentFile().mkdirs();
                Files.write(newFile.toPath(), imageBytes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log("Server> saveLecturerImage> " + ex);
        }
    }*/

    void deleteFile(String fileType, String fileName) {
        File fileToDelete = new File(Server.APPLICATION_FOLDER + "/" + fileType + "/" + fileName);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
            try {
                Thread.sleep(50);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            notifyUpdatedDocuments();
        }
    }

    void notifyUpdatedDocuments() {
        for (ConnectionHandler ch : Server.connectionsList) {
            if (ch instanceof UserConnectionHandler) {
                ((UserConnectionHandler) ch).updateDocuments.setValue(true);
                break;
            }
        }
    }

    public void createQuotation(Quotation quotation){
        File source = new File(Server.TEMPLATES_FOLDER + "/Quotation Template.xlsx");
        File target = new File(Server.QUOTATIONS_FOLDER + "/" + quotation.getQuotationNumber() + ".xlsx");
        target.mkdirs();
        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileInputStream inputStream = new FileInputStream(target);
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            HSSFSheet sheet = workbook.getSheetAt(0);

            //TODO client data

            int productStartCell = 10;//TODO cell values
            for (QuotationProduct qp: quotation.getProducts()) {
                Cell cell = null;
                cell = sheet.getRow(productStartCell).getCell(3);
                cell.setCellValue(qp.getProduct().getDescription());
                cell = sheet.getRow(productStartCell).getCell(4);
                cell.setCellValue("R " + qp.getProduct().getPrice());
                cell = sheet.getRow(productStartCell).getCell(5);
                cell.setCellValue(qp.getQuantity());
                cell = sheet.getRow(productStartCell).getCell(6);
                cell.setCellValue("R " + qp.getProduct().getPrice() + qp.getQuantity());
                productStartCell++;
            }

            //TODO price

            inputStream.close();
            FileOutputStream outputFile = new FileOutputStream(target);
            workbook.write(outputFile);
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void processQuotationToInvoice(String quoteNumber) {

    }

    void log(String logDetails) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            System.out.println(dateFormat.format(date) + " : " + logDetails);
            File logFile = Server.LOG_FILE.getAbsoluteFile();
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileWriter fw = new FileWriter(logFile.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(dateFormat.format(date) + " : " + logDetails);
            bw.newLine();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

