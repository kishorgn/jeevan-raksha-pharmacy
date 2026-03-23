package com.codegnan.jeevanraksha.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI 3.0 / Swagger UI documentation for the application.
 *
 * <p>After the application starts, interactive API documentation is accessible at:
 * <ul>
 *   <li>Swagger UI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></li>
 *   <li>API JSON:   <a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a></li>
 * </ul>
 * </p>
 *
 * <p>Each controller is tagged so that the Swagger UI groups endpoints
 * by domain (Customers, Suppliers, Medicines, Orders, Inventory,
 * Reports, Search).</p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Builds the top-level OpenAPI metadata shown in the Swagger UI header.
     */
    @Bean
    public OpenAPI jeevanRakshaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jeevan Raksha Pharmacy API")
                        .description("""
                                Production-grade REST API for the Jeevan Raksha Pharmacy Management System.

                                **Capabilities:**
                                - Full CRUD for Customers, Suppliers, and Medicines
                                - Order placement with automatic stock validation and deduction
                                - Order cancellation with stock restoration
                                - Real-time inventory alerts and restocking
                                - Revenue reports, bestseller analytics, and inventory audits
                                - Cross-entity search across medicines and suppliers

                                **Standard Response Envelope:**
                                Every endpoint returns a JSON object with `status`, `message`, and `data` fields.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jeevan Raksha Pharmacy")
                                .email("admin@jeevanraksha.in"))
                        .license(new License()
                                .name("Proprietary")
                                .url("http://localhost:8080")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")))
                .externalDocs(new ExternalDocumentation()
                        .description("Pharmacy Database Schema")
                        .url("http://localhost:8080/v3/api-docs"));
    }
}
