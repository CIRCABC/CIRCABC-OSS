package com.sii.eucaptcha.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * AppInitializer class
 * @author mousab.aidoud
 * @version 1.0
 */
public class AppInitializer
  extends AbstractAnnotationConfigDispatcherServletInitializer {

  /**
   * override method
   * @return null
   */
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[] {};
  }

  /**
   * override method
   * @return new class[]{WebMvcConfigurer.class}
   */
  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] { WebConfig.class };
  }

  /**
   * override method
   * @return new String[]{"/"};
   */
  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }
}
