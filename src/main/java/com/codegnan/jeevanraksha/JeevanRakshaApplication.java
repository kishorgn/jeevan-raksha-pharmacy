package com.codegnan.jeevanraksha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Jeevan Raksha Pharmacy REST API application.
 *
 * <p>This Spring Boot application exposes a production-grade REST API
 * for managing all pharmacy operations including customers, suppliers,
 * medicines, orders, inventory, reports, and cross-entity search.</p>
 *
 * <p>Base URL  : http://localhost:8080</p>
 * <p>Swagger UI: http://localhost:8080/swagger-ui.html</p>
 * <p>API Docs  : http://localhost:8080/v3/api-docs</p>
 */
@SpringBootApplication
public class JeevanRakshaApplication {

    private static final Logger logger = LoggerFactory.getLogger(JeevanRakshaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JeevanRakshaApplication.class, args);
        logger.info("=========================================================");
        logger.info("  Jeevan Raksha Pharmacy API started successfully!");
        logger.info("  Swagger UI : http://localhost:8080/swagger-ui.html");
        logger.info("  API Docs   : http://localhost:8080/v3/api-docs");
        logger.info("=========================================================");
    }
}
