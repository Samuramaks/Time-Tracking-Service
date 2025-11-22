package ru.samura.time_tracking_service.swagger;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.AllArgsConstructor;


/**
 * Данный класс предназначен для описания SwaggerUI:
 * Кто сделал, краткое описание всех endpointов
 * Доступен по адресу http://localhost:8080/swagger-ui/index.html
 */
@Configuration
@AllArgsConstructor
public class OpenAPIConfiguration {

    private Environment environment;

    @Bean
    public OpenAPI defineOpenAPI () {
        Server server = new Server();
        String serverUrl = environment.getProperty("api.server.url");
        server.setUrl(serverUrl);
        server.setDescription("Development");

        Contact myContact = new Contact();
        myContact.setName("Максим Демин");

        Info info = new Info()
                .title("Системное API для управления временем сотрудников")
                .version("1.0")
                .description("Это API предоставляет эндпоинты для управления временем сотрудников.")
                .contact(myContact);
        return new OpenAPI().info(info).servers(List.of(server));
    }
}
