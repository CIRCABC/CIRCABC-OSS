package com.sii.eucaptcha.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:controller.properties")
@ConfigurationProperties(prefix = "controller.captcha")
public class ControllerConfigProperties {}
