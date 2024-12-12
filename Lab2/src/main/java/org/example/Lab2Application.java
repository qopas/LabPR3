package org.example;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Lab2Application {

    public static void main(String[] args) {
        String serverId = System.getenv("SERVER_ID");  // Set this in your environment or config
        if (serverId == null) {
            System.err.println("Please set SERVER_ID environment variable!");
            return;
        }

        // Start the ElectionHandler for leader election
        new Thread(new ElectionHandler(serverId)).start();

        // Run the Spring Boot application
        SpringApplication.run(Lab2Application.class, args);

    }
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createWebSocketConnector());

        return tomcat;
    }

    private Connector createWebSocketConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(8081);
        return connector;
    }
}