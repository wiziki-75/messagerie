package com.example.messagerie.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Messagerie API",
                version = "v1",
                description = "API REST pour la messagerie (utilisateurs, demandes d'ami, conversations, messages, fichiers, notifications)",
                contact = @Contact(name = "Messagerie"),
                license = @License(name = "Unlicense")
        )
)
public class OpenApiConfig {
}
