package mailscape;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

public class UI {
    private JTabbedPane Tabs;
    private JLabel Heading;
    private JTextField tFieldTo;
    private JTextField tFieldSubject;
    private JTextField tFieldBody;
    private JButton bAttachment;
    private JButton bSend;
    private JPanel MainWindow;
    private JList<String> listAttachments;
    private JLabel sendingMailState;

    // Global State Variables
    private File[] file_paths;
    private String to = "", subject = "", body = "";
    private String from = "";

    public UI() {
        bAttachment.addActionListener(e -> {
            FilePicker ob = new FilePicker();
            file_paths = ob.getPath();
            String [] path_strings = new String[file_paths.length];
            for(int i = 0; i<file_paths.length; i++) path_strings[i] = file_paths[i].toString();
            listAttachments.setVisible(true);
            listAttachments.setListData(path_strings);
        });
        bSend.addActionListener(e -> {
            boolean success = false;
            sendingMailState.setText("Sending mail...");
            try {
                from = Credentials.getCredentials()[0];
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            to = tFieldTo.getText();
            subject = tFieldSubject.getText();
            body = tFieldBody.getText();
            try {
                success = MailSender.actuallySendMail(from, to, subject, body, file_paths);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            if(success) {
                sendingMailState.setText("Mail sent!");
            } else {
                sendingMailState.setText("Couldn't send mail");
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("UI");
        frame.setContentPane(new UI().MainWindow);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        listAttachments = new JList<String>();
        listAttachments.setVisible(false);
    }
}
