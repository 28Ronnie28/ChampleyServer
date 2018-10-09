package net.ddns.pcuniverse.nomdlaenterpriseserver.main;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class Email {

    public static Boolean emailPassword(String username, String email, String password) {
        return email(email, "Champley - Forgot password", "Dear " + username + "\n\nYou requested to send your Champley password to your email.\nIf this wasn't you please contact us.\n\nPassword:\t" + password, null, null);
    }

    public static Boolean email(String email, String emailSubject, String emailMessage, String documentType, String attachment) {
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.user", "infochampley@gmail.com");
            props.put("mail.smtp.password", "Champley2018RonCar");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", true);
            Session session = Session.getInstance(props, null);
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", "infochampley", "Champley2018RonCar");
            MimeMessage message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(emailSubject);
            message.setText(emailMessage);
            if (attachment != null) {
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText("This is message body");
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                messageBodyPart = new MimeBodyPart();
                String filename = Server.APPLICATION_FOLDER.getAbsolutePath() + "/" + documentType + "/" + attachment;
                DataSource source = new FileDataSource(filename);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
                message.setContent(multipart);
                transport.sendMessage(message, message.getAllRecipients());
            } else {
                transport.sendMessage(message, message.getAllRecipients());
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
