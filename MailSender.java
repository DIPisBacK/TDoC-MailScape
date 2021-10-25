package mailscape;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class MailSender {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(System.in);
        String from = Credentials.getCredentials()[0];
        String to = sc.nextLine();
        String sub = sc.nextLine();
        String body = sc.nextLine();
        FilePicker ob = new FilePicker();
        File[] file_paths = ob.getPath();
        boolean success = actuallySendMail(from, to, sub, body, file_paths);
        if(success) System.out.println("Successfuly sent mail");
    }

    public static boolean actuallySendMail(String from, String to, String sub, String body, File[] file_paths) throws FileNotFoundException {
        Properties props = new Properties();

        // enable authentication
        props.put("mail.smtp.auth", "true");

        // enable STARTTLS
        props.put("mail.smtp.starttls.enable", "true");

        // Setup mail server
        props.put("mail.smtp.host", "smtp.gmail.com");

        // TLS Port
        props.put("mail.smtp.port", "587");

        String[] credentials = Credentials.getCredentials();
        String username = credentials[0];
        String password = credentials[1];

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(sub);

            if(file_paths != null && file_paths.length != 0) {
                Multipart multipart = new MimeMultipart();

                // sending the text
                BodyPart messageBodyPath = new MimeBodyPart();
                messageBodyPath.setContent(body, "text/html");
                multipart.addBodyPart(messageBodyPath);

                // sending the attachment
                for(File path : file_paths) {
                    MimeBodyPart file = new MimeBodyPart();
                    String path_as_string = path.toString();
                    file.attachFile(path_as_string);
                    multipart.addBodyPart(file);
                }

                message.setContent(multipart, "text/html");
            } else {
                message.setContent(body, "text/html");
            }

            Transport.send(message);
            return true;
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
