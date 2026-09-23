package com.sii.eucaptcha.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringFoxConfig {

  @Bean
  public OpenAPI api() {
    return new OpenAPI().info(apiInfo());
  }

  private Info apiInfo() {
    return new Info()
      .title("EU Captcha Rest API")
      .description("API for use of EU Captcha")
      .version("1.0")
      .contact(
        new Contact().name("Digit info").email("DIGIT-EU-CAPTCHA@ec.europa.eu")
      )
      .license(
        new License()
          .name("European Union Public License 1.2")
          .url("https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12")
      );
  }
}
