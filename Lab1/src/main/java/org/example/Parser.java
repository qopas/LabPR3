package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class Parser {
    private static final String QUEUE_NAME = "productQueue";
    public static final String FILE_PATH = "C:\\Users\\user\\IdeaProjects\\Lab3PR\\";
    public static void parseAndPublish(String url) {
        try (Connection connection = new ConnectionFactory().newConnection("localhost");
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            Document doc = Jsoup.connect(url).get();
            Element productGrid = doc.selectFirst("div.product-grid");



            if (productGrid != null) {
                Elements products = productGrid.select("> div");
                for (Element product : products) {
                    StringBuilder fileContent = new StringBuilder();
                    String productLink = Validator.validateLink(product.selectFirst("div.image a"), "href");
                    String phoneName = Validator.validateText(product.selectFirst("div.name a span"));
                    String phonePrice = Validator.validatePrice(product.selectFirst("div.price"));
                    Document productDoc = Jsoup.connect(productLink).get();
                    Element creditPriceElement = productDoc.selectFirst(".postreqcredit_btn strong");
                    String creditPrice = Validator.validateText(creditPriceElement);

                    // Create a JSON object
                    String message = String.format(
                            "{\"name\":\"%s\",\"price\":%s,\"productLink\":\"%s\",\"monthlyCreditPrice\":%s}",
                            phoneName, phonePrice, productLink, creditPrice
                    );

                    // Publish message to RabbitMQ
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                    System.out.println("Published message: " + message);

                    // Append data to the file content
                    fileContent.append(String.format(
                            "Name: %s, Price: %s, Link: %s, monthlyCreditPrice: %s%n",
                            phoneName, phonePrice, productLink, creditPrice
                    ));

                    Map<String, String> productMap = new HashMap<>();
                    productMap.put("name", phoneName);
                    productMap.put("price", phonePrice);
                    productMap.put("productLink", productLink);
                    productMap.put("monthlyCreditPrice", creditPrice);

                    // Convert Map to JSON using Gson
                    Gson gson = new Gson();
                    String productJson = gson.toJson(productMap);

                    // Create a unique file for each product and save it
                    String fileName = phoneName.replaceAll("[^a-zA-Z0-9]", "_") + ".json";
                    String filePath = FILE_PATH+fileName;
                    Uploader.saveProductToFile(fileName, productJson);
                    Uploader.uploadFileToFTP(filePath);
                }
            } else {
                System.out.println("No product grid found on the page.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
