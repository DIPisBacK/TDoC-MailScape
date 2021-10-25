package mailscape;

import org.jetbrains.annotations.NotNull;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

public class MailRetriever {
    public static void main(String[] args) throws FileNotFoundException {
        String protocol = "imap";
        String host = "imap.gmail.com";
        String user_name = Credentials.getCredentials()[0];
        String password = Credentials.getCredentials()[1];
        String port = "993";

        getEmails(protocol, host, port, user_name, password, 3);
    }

    public static @NotNull
    Properties getServerProperties(String protocol, String host, String port) {
        Properties properties = new Properties();

        // server settings
        properties.put(String.format("mail.%s.host", protocol), host);
        properties.put(String.format("mail.%s.port", protocol), host);

        // SSL settings
        properties.setProperty(String.format("mail.%s.socketFactory.class", protocol), "javax.net.ssl" +
                ".SSLSocketFactory");
        properties.setProperty(String.format("mail.%s.socketFactory.fallback", protocol), "false");
        properties.setProperty(String.format("mail.%s.socketFactory.port", protocol), String.valueOf(port));
        properties.setProperty("mail.imaps.partialfetch", "false");

        return properties;
    }

    public static void getEmails(String protocol, String host, String port, String user_name, String password,
                                 int mail_count) {
        
        String[] fromList = new String[mail_count];
        String[] subjectList = new String[mail_count];
        String[] bodyList = new String[mail_count];
        Properties properties = getServerProperties(protocol, host, port);
        Session session = Session.getDefaultInstance(properties);
        try {
            //connects to the message store
            Store store = session.getStore(protocol);
            store.connect(user_name, password);

            //opens the inbox folder
            Folder folderInbox = store.getFolder("[Gmail]/All Mail");
            folderInbox.open(Folder.READ_WRITE);

            //fetches new messages from server
            Message[] messages = folderInbox.getMessages();
            int n = messages.length;


            for (int i = n - 1; i >= n - mail_count; i--) {
                Message msg = messages[i];

                InternetAddress sender = (InternetAddress) msg.getFrom()[0];
                String from = sender.getAddress();
                String subject = msg.getSubject();
                String bodypart = "";
                if (msg.isMimeType("text/plain")) {
                    bodypart = msg.getContent().toString();
                }

                if (msg.isMimeType("multipart/*")) {
                    try {
                        MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
                        bodypart = getTextFromMimeMultipart(mimeMultipart).toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                bodypart = bodypart.replaceAll("(?m)^[ \t]*\r?\n", "");

                fromList[n-1-i] = from;
                subjectList[n-1-i] = subject;
                bodyList[n-1-i] = bodypart;

                System.out.println();
                System.out.println(n - i + ")");
                System.out.println("FROM : " + from);
                System.out.println("SUBJECT : " + subject);
                System.out.println("BODY : " + bodypart);
                System.out.println();
            }

            System.out.print("Enter mail number to star: ");
            Scanner sc = new Scanner(System.in);
            int x = sc.nextInt();
            starMail(fromList[x-1],subjectList[x-1], bodyList[x-1]);

            // disconnect
            folderInbox.close(false);
            store.close();

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    public static @NotNull StringBuilder getTextFromMimeMultipart(@NotNull MimeMultipart mimeMultipart) throws MessagingException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        try {
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result.append(bodyPart.getContent());
                    break;
                } else if (bodyPart.isMimeType("text/html")) {
                    String html = (String) bodyPart.getContent();
                    result.append(org.jsoup.Jsoup.parse(html).text());
                } else if (bodyPart.getContent() instanceof MimeMultipart)
                    result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void starMail(String from, String subject, String body) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String[] credentials = Credentials.getCredentials();
            Connection con = DriverManager.getConnection(credentials[2], credentials[3], credentials[4]);
            Statement statement = con.createStatement();

            body = body.replace("'", "\\'");
            String sqlInsert =
                    "INSERT INTO star_mail (sender, subject, body) VALUES ( '" + from + "', '" + subject + "', '" + body +
                            "');";
            System.out.println(sqlInsert);
            int countInserted = statement.executeUpdate(sqlInsert);
            if (countInserted != 0) {
                System.out.println("Email is starred successfully");
            }
            
            con.close();
        } catch (ClassNotFoundException | FileNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
