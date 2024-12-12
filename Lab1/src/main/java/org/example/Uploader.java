package org.example;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class Uploader {
    public static final String FTP_SERVER = "localhost";
    public static final int FTP_PORT = 21;
    public static final String FTP_USER = "testuser";
    public static final String FTP_PASS = "testpass";

    // Method to save data to file
    static void saveProductToFile(String fileName, String jsonData) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonData);
            System.out.println("Saved product data to file: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to upload the file to FTP server
    public static void uploadFileToFTP(String localFilePath) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(FTP_USER, FTP_PASS);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            File localFile = new File(localFilePath);
            FileInputStream inputStream = new FileInputStream(localFile);

            boolean success = ftpClient.storeFile(localFile.getName(), inputStream);
            inputStream.close();

            if (success) {
                System.out.println("File uploaded successfully to FTP server.");
            } else {
                System.out.println("Failed to upload file to FTP server.");
            }

            ftpClient.logout();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
