/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.asosat.exp.interfaces;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.sql.DataSource;

/**
 * corant-asosat-ddd
 *
 * @author bingo 下午6:56:30
 *
 */
@ApplicationScoped
@WebListener
public class TestListener implements ServletContextListener {

  @Inject
  @Named("dmmsRwDs")
  DataSource ds;

  static void testInject(DataSource ds, String test) {

  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ServletContextListener.super.contextDestroyed(sce);
    testInject(ds, "contextDestroyed");
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContextListener.super.contextInitialized(sce);
    testInject(ds, "contextInitialized");
  }

  @ApplicationScoped
  @WebListener
  public static class OnLineListener implements HttpSessionListener {
    @Inject
    @Named("dmmsRwDs")
    DataSource ds;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
      HttpSessionListener.super.sessionCreated(se);
      testInject(ds, "sessionCreated");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
      HttpSessionListener.super.sessionDestroyed(se);
      testInject(ds, "sessionDestroyed");
    }
  }

  @ApplicationScoped
  @WebListener
  public static class RequestListenter
      implements ServletRequestListener, ServletRequestAttributeListener {

    @Inject
    @Named("dmmsRwDs")
    DataSource ds;

    @Override
    public void attributeAdded(ServletRequestAttributeEvent srae) {
      ServletRequestAttributeListener.super.attributeAdded(srae);
      testInject(ds, "attributeAdded");
    }

    @Override
    public void attributeRemoved(ServletRequestAttributeEvent srae) {
      ServletRequestAttributeListener.super.attributeRemoved(srae);
      testInject(ds, "attributeRemoved");
    }

    @Override
    public void attributeReplaced(ServletRequestAttributeEvent srae) {
      ServletRequestAttributeListener.super.attributeReplaced(srae);
      testInject(ds, "attributeReplaced");
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
      ServletRequestListener.super.requestDestroyed(sre);
      testInject(ds, "requestDestroyed");
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
      ServletRequestListener.super.requestInitialized(sre);
      testInject(ds, "requestInitialized");
    }

  }

  @ApplicationScoped
  @WebListener
  public static class ServletConnextAtrributeListener implements ServletContextAttributeListener {

    @Inject
    @Named("dmmsRwDs")
    DataSource ds;

    @Override
    public void attributeAdded(ServletContextAttributeEvent event) {
      ServletContextAttributeListener.super.attributeAdded(event);
      testInject(ds, "attributeAdded");
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent event) {
      ServletContextAttributeListener.super.attributeRemoved(event);
      testInject(ds, "attributeRemoved");
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent event) {
      ServletContextAttributeListener.super.attributeReplaced(event);
      testInject(ds, "attributeReplaced");
    }

  }
}
