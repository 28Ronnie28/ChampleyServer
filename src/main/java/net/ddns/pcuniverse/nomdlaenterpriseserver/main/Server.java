package net.ddns.pcuniverse.nomdlaenterpriseserver.main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.EOFException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {

    static final File APPLICATION_FOLDER = new File(System.getProperty("user.home") + "/AppData/Local/PCUniverse/NomdlaEnterpriseServer");
    static final File TEMPLATES_FOLDER = new File(APPLICATION_FOLDER.getAbsolutePath() + "/Templates");
    static final File INVOICE_FOLDER = new File(APPLICATION_FOLDER.getAbsolutePath() + "/Invoices");
    static final File QUOTATIONS_FOLDER = new File(APPLICATION_FOLDER.getAbsolutePath() + "/Quotations");
    static final File DOCUMENTS_FOLDER = new File(APPLICATION_FOLDER.getAbsolutePath() + "/Documents");
    static final File DATABASE_FILE = new File(APPLICATION_FOLDER.getAbsolutePath() + "/NomdlaEnterpriseDB.db");
    static final File LOG_FILE = new File(APPLICATION_FOLDER.getAbsolutePath() + "/NomdlaEnterpriseLogFile.txt");
    static final int BUFFER_SIZE = 4194304;
    public static ObservableList<ConnectionHandler> connectionsList = FXCollections.observableArrayList();
    public static final int PORT = 1521;
    public static final int MAX_CONNECTIONS = 5;
    public DatabaseHandler dh = new DatabaseHandler();

    public Server() {
        if (!TEMPLATES_FOLDER.exists()) {
            TEMPLATES_FOLDER.mkdirs();
            dh.log("Server> Local Templates Files Folder Created");
        }
        if (!INVOICE_FOLDER.exists()) {
            INVOICE_FOLDER.mkdirs();
            dh.log("Server> Local Invoice Files Folder Created");
        }
        if (!QUOTATIONS_FOLDER.exists()) {
            QUOTATIONS_FOLDER.mkdirs();
            dh.log("Server> Local Quotations Files Folders Created");
        }
        if (!DOCUMENTS_FOLDER.exists()) {
            DOCUMENTS_FOLDER.mkdirs();
            dh.log("Server> Local Documents Files Folders Created");
        }
        new ClientListener().start();
    }

    public class ClientListener extends Thread {
        @Override
        public void run() {
            try {
                dh.log("Server> Trying to set up client on port " + PORT);
                /*System.setProperty("javax.net.ssl.keyStore", APPLICATION_FOLDER.getAbsolutePath() + "/campuslive.store");//TODO
                System.setProperty("javax.net.ssl.keyStorePassword", "campuslivepassword1");*/
                dh.log("Server> Set up client on port " + PORT);
                //ServerSocket ss = SSLServerSocketFactory.getDefault().createServerSocket(PORT);
                ServerSocket ss = new ServerSocket(PORT);
                while (true) {
                    while (connectionsList.size() <= MAX_CONNECTIONS) {
                        dh.log("Server> Waiting for new connection");
                        Socket s = ss.accept();
                        s.setKeepAlive(true);
                        dh.log("Server> Connection established on " + s.getInetAddress().getHostAddress() + ":" + s.getPort());
                        new LoginManager(s).start();
                    }
                }
            } catch (Exception ex) {
                dh.log("Server> ClientListener> " + ex);
                ex.printStackTrace();
            }
        }
    }

    public class LoginManager extends Thread {

        private Socket s;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;

        public LoginManager(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                objectInputStream = new ObjectInputStream(s.getInputStream());
                objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                StopClass:
                while (true) {
                    Object inputObject;
                    try {
                        while ((inputObject = objectInputStream.readObject()) == null) ;
                        if (inputObject instanceof String) {
                            String input = (String) inputObject;
                            if (input.startsWith("au:")) {
                                dh.log("Server> Authorising User : " + input.substring(3).split(":")[0]);
                                if (authoriseUser(input.substring(3).split(":")[0], input.substring(3).split(":")[1])) {
                                    dh.log("Server> Authorised User : " + input.substring(3).split(":")[0]);
                                    objectOutputStream.writeObject("au:y");
                                    objectOutputStream.flush();
                                    UserConnectionHandler studentConnectionHandler = new UserConnectionHandler(s, objectInputStream, objectOutputStream, input.substring(3).split(":")[0], connectionsList, dh);
                                    Thread t = new Thread(studentConnectionHandler);
                                    t.start();
                                    connectionsList.add(studentConnectionHandler);
                                    //dh.createQuotation("Test123");
                                    break StopClass;
                                } else {
                                    dh.log("Server> Authorising User : " + input.substring(3).split(":")[0] + " Failed");
                                    objectOutputStream.writeObject("au:n");
                                    objectOutputStream.flush();
                                }
                            } else if (input.startsWith("fp:")) {
                                dh.log("User > Requested Forgot Password");
                                dh.emailUserPassword(input.substring(3));
                            }
                        }
                    } catch (SocketException e) {
                        dh.log("Server> User Disconnected");
                        this.stop();
                        connectionsList.remove(this);//TODO
                    } catch (EOFException e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception ex) {
                dh.log("Server> LoginManager> " + ex);
                ex.printStackTrace();
            }
        }
    }

    private Boolean authoriseUser(String username, String password) {
        return dh.authoriseUser(username, password);
    }

    public static void main(String[] args) {
        new Server();
    }

}
