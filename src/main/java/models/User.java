package models;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;

    public User(String username, String firstName, String lastName, String email, String contactNumber) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    public String getUsername() {
        return username;
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getStudentInformation() {
        return "First Name: " + firstName + "\nLast Name: " + lastName + "\nUsername: " + username + "\nEmail: " + email + "\nContact Number: " + contactNumber;
    }
}
