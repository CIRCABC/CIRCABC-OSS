package com.sii.eucaptcha.configuration;

import com.sii.eucaptcha.configuration.properties.ApplicationConfigProperties;
import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * @author mousab.aidoud
 * @version 1.0
 * Web MVC configuration.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "com.sii.eucaptcha" })
public class WebConfig implements WebMvcConfigurer {

  @Resource
  private ApplicationConfigProperties props;

  /**
   *
   * @return the created resolver
   */
  @Bean
  protected InternalResourceViewResolver resolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setViewClass(JstlView.class);
    resolver.setPrefix(props.getPrefix());
    resolver.setSuffix(props.getSuffix());
    return resolver;
  }

  /**
   *
   * @return localResolver.
   */
  @Bean
  protected LocaleResolver localeResolver() {
    SessionLocaleResolver localeResolver = new SessionLocaleResolver();
    localeResolver.setDefaultLocale(props.getDefaultLocale());
    return localeResolver;
  }

  /**
   *
   * @return localChangeInterceptor
   */
  @Bean
  protected LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor localeChangeInterceptor =
      new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName(props.getParamName());
    return localeChangeInterceptor;
  }

  /**
   *
   * @return the message source created
   */
  @Bean
  protected MessageSource messageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setDefaultEncoding(props.getDefaultEncoding());
    source.setBasename(props.getBaseName());
    return source;
  }

  /**
   *
   * @param registry the registry to add the interceptor to change the language
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor());
  }

  /**
   *
   * @return validator
   */
  @Override
  public Validator getValidator() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.setValidationMessageSource(messageSource());
    return validator;
  }

  /**
   *
   * @param registry the registry to add the resource handlers
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
      .addResourceHandler(props.getCssHandler())
      .addResourceLocations(props.getCssLocations());
    registry
      .addResourceHandler(props.getJsHandler())
      .addResourceLocations(props.getJsLocations());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
      .addMapping("/**")
      .allowedOriginPatterns("*")
      .allowCredentials(true)
      .allowedMethods("OPTIONS", "GET", "POST")
      .exposedHeaders("x-jwtString", "Content-Type")
      .allowedHeaders(
        "*",
        "x-jwtString",
        "Content-Type",
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
      );
  }
}
