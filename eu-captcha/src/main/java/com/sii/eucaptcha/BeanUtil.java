package com.sii.eucaptcha;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/*
 * BeanUtil is used for Autowiring Spring Beans Into Classes Not Managed by Spring
 */

@Service
public class BeanUtil implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  @SuppressWarnings("java:S2696") // Static field set from instance method is intentional - standard Spring pattern
  public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
    context = applicationContext;
  }

  public static <T> T getBean(Class<T> beanClass) {
    return context.getBean(beanClass);
  }
}
