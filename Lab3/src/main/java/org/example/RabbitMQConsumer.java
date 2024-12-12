package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class RabbitMQConsumer {


    @Autowired
    private RestTemplate restTemplate;
    private static final String PRODUCT_API_URL = "http://localhost:8081/api/products";

    @RabbitListener(queues = "productQueue")
    public void consumeMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Product product = objectMapper.readValue(message, Product.class);

            sendProductToHttpServer(product);

            System.out.println("Product sent to HTTP server: " + product);
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }

    private void sendProductToHttpServer(Product product) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Product> entity = new HttpEntity<>(product, headers);
            ResponseEntity<String> response = restTemplate.exchange(PRODUCT_API_URL, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Product sent successfully to HTTP server: " + response.getBody());
            } else {
                System.out.println("Failed to send product to HTTP server: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Failed to send product to HTTP server: " + e.getMessage());
        }
    }


}
