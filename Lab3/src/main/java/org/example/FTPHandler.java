package org.example;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
@Component
public class FTPHandler {
    @Value("${ftp.server}")
    private String ftpServer;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.user}")
    private String ftpUser;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.directory}")
    private String ftpDirectory;
    @Autowired
    private RestTemplate restTemplate;
    private static final String UPLOAD_URL = "http://localhost:8080/api/products/upload";
    @Scheduled(fixedRate = 30000)
    private void fetchAndProcessLatestFileFromFTP() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpServer, ftpPort);
            ftpClient.login(ftpUser, ftpPassword);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // List files in the FTP directory
            String[] files = ftpClient.listNames(ftpDirectory);
            if (files != null && files.length > 0) {
                Arrays.sort(files, Collections.reverseOrder());

                // Get the latest file (first in sorted array)
                String latestFile = files[0];
                downloadAndUploadFile(ftpClient, latestFile);

                // Delete the file from FTP server after processing
                boolean deleted = ftpClient.deleteFile(ftpDirectory + latestFile);
                if (deleted) {
                    System.out.println("File " + latestFile + " deleted from FTP server.");
                } else {
                    System.out.println("Failed to delete file: " + latestFile);
                }
            } else {
                System.out.println("No files found on the FTP server.");
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

    private void downloadAndUploadFile(FTPClient ftpClient, String fileName) {
        FileOutputStream outputStream = null;
        try {
            // Download file from FTP server
            String localFilePath = "./" + fileName;
            outputStream = new FileOutputStream(localFilePath);
            boolean success = ftpClient.retrieveFile(ftpDirectory + fileName, outputStream);

            if (success) {
                System.out.println("File " + fileName + " downloaded successfully.");
                sendFileToWebServer(localFilePath);
            } else {
                System.out.println("Failed to download file: " + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFileToWebServer(String filePath) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            File file = new File(filePath);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send POST request
            String response = restTemplate.postForObject(UPLOAD_URL, requestEntity, String.class);
            System.out.println("Response from server: " + response);

            // Optionally, delete the file after upload
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("File deleted after upload: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
