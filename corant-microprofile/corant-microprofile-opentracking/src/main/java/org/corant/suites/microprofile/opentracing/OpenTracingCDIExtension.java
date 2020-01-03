package org.corant.suites.microprofile.opentracing;

import io.opentracing.contrib.interceptors.OpenTracingInterceptor;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * @auther sushuaihao 2020/1/2
 * @since
 */
public class OpenTracingCDIExtension implements Extension {

  public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
    String extensionName = OpenTracingCDIExtension.class.getName();
    for (Class<?> clazz : new Class<?>[] {OpenTracingInterceptor.class}) {
      bbd.addAnnotatedType(
          manager.createAnnotatedType(clazz), extensionName + "_" + clazz.getName());
    }
  }
}