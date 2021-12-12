import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

class FileDialog {
    public static File save(JFrame parent, String title, String fileTypeDescription, String fileExtension) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.toPath().endsWith("." + fileExtension);
            }

            @Override
            public String getDescription() {
                return fileTypeDescription;
            }
        });

        int uc = fc.showSaveDialog(parent);

        // Valid file selection
        if (uc == JFileChooser.APPROVE_OPTION) {
            System.out.println(fc.getSelectedFile());
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }

    public static File open(JFrame parent, String title, String fileTypeDescription, String fileExtension) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.toPath().endsWith("." + fileExtension);
            }

            @Override
            public String getDescription() {
                return fileTypeDescription;
            }
        });

        int uc = fc.showOpenDialog(parent);

        // Valid file selection
        if (uc == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }
}