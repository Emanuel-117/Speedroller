package com.rollerspeed.rollerspeed.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("https://localhost:8443");
        devServer.setDescription("URL del servidor de desarrollo");

        Contact contact = new Contact();
        contact.setEmail("info@rollerspeed.com");
        contact.setName("RollerSpeed");
        contact.setUrl("https://www.rollerspeed.com");

        License mitLicense = new License().name("MIT License")
                                        .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
            .title("API de Gestión RollerSpeed")
            .version("1.0")
            .contact(contact)
            .description("Esta API expone endpoints para gestionar la academia RollerSpeed.")
            .termsOfService("https://www.rollerspeed.com/terms")
            .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(devServer));
    }
}