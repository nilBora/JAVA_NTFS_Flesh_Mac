package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller {

    final String NTFS_DRIVER = "ntfs-3g";

    @FXML
    TextArea iTextArea;

    @FXML
    TextField iDiskName;

    @FXML
    Label iLabelError;

    @FXML
    public void onCheck(ActionEvent event)
    {
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("diskutil list");
            int result = pr.waitFor();
            String res = readBuffer(pr);
            iTextArea.appendText(res);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onUnlock(ActionEvent event) throws Exception {
        try {
            String diskName = iDiskName.getText();
            if (diskName.isEmpty()) {
                throw new Exception("Disk Path is Empty");
            }

            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("which " + NTFS_DRIVER);

            int code = pr.waitFor();
            if (code != 0) {
                String msg = __("%s Not Found. Use brew install '%s'", NTFS_DRIVER, NTFS_DRIVER);
                throw new Exception(msg);
            }

            String pathDriver = readBuffer(pr);

            appendTextArea("\n--------Process uncheck------\n");
            appendTextArea("Path Driver: " + pathDriver, false);

            String cmdCreate = "sudo mkdir /Volumes/NTFS";

            String diskPath = "/dev/" + diskName;
            String cmdUmount = "sudo umount " + diskPath;

            pr = rt.exec(cmdUmount);

            int result = pr.waitFor();

            if (result != 0) {
                appendTextArea("Problem umount command", true);
            } else {
                appendTextArea("Disk umount", false);
            }

            String cmd = "sudo " + pathDriver + " " + diskPath + " /Volumes/NTFS -olocal -oallow_other";

            appendTextArea(cmd, false);

            pr = rt.exec(cmd);

            result = pr.waitFor();

            if (result != 0) {
                appendTextArea("Disk unlock", true);
                throw new Exception("Problem unlock flesh");
            }
            appendTextArea("Disk unlock", false);

            String res = readBuffer(pr);
            appendTextArea(res, false);

        } catch (Exception e) {
            iLabelError.setText(e.getMessage());
        }
    }

    private void appendTextArea(String text)
    {
        iTextArea.appendText(text);
    }

    private void appendTextArea(String text, Boolean isError)
    {
        String postfix = " - [OK]";
        if (isError) {
            postfix = " - [ERROR]";
        }
        iTextArea.appendText(text + postfix + "\n");
    }


    public String readBuffer(Process process) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = "";
        String result = "";
        while ((line = reader.readLine()) != null) {
            result += line + "\n";
        }
//        if (result.length() > 2) {
//            return result.substring(0, result.length() - 2);
//        }

        return result;
    }

    public String __(String format, Object... args)
    {
        return String.format(format, args);
    }
}
