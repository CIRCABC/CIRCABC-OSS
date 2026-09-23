package com.sii.eucaptcha.configuration.properties;

import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config.view")
public class ApplicationConfigProperties {

  private String prefix;
  private String suffix;
  private Locale defaultLocale;
  private String defaultEncoding;
  private String paramName;
  private String baseName;
  private String cssHandler;
  private String cssLocations;
  private String jsHandler;
  private String jsLocations;

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  public void setDefaultLocale(String defaultLocale) {
    this.defaultLocale = Locale.forLanguageTag(defaultLocale);
  }

  public String getDefaultEncoding() {
    return defaultEncoding;
  }

  public void setDefaultEncoding(String defaultEncoding) {
    this.defaultEncoding = defaultEncoding;
  }

  public String getParamName() {
    return paramName;
  }

  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  public String getBaseName() {
    return baseName;
  }

  public void setBaseName(String baseName) {
    this.baseName = baseName;
  }

  public String getCssHandler() {
    return cssHandler;
  }

  public void setCssHandler(String cssHandler) {
    this.cssHandler = cssHandler;
  }

  public String getCssLocations() {
    return cssLocations;
  }

  public void setCssLocations(String cssLocations) {
    this.cssLocations = cssLocations;
  }

  public String getJsHandler() {
    return jsHandler;
  }

  public void setJsHandler(String jsHandler) {
    this.jsHandler = jsHandler;
  }

  public String getJsLocations() {
    return jsLocations;
  }

  public void setJsLocations(String jsLocations) {
    this.jsLocations = jsLocations;
  }
}
