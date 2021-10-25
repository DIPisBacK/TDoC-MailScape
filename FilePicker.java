package mailscape;

import javax.swing.JFileChooser;
import java.io.File;

public class FilePicker {
    public File[] getPath() {
        File[] files = {};
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int res = chooser.showOpenDialog(null);

        if(res == JFileChooser.APPROVE_OPTION) {
            files = chooser.getSelectedFiles();
        }

        return files;
    }
}
