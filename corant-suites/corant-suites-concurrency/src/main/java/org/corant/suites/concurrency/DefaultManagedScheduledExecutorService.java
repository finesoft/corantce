/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.suites.concurrency;

import java.util.concurrent.TimeUnit;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedScheduledExecutorServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedThreadFactoryImpl;

/**
 * corant-suites-concurrency
 *
 * @author bingo 上午10:23:58
 *
 */
public class DefaultManagedScheduledExecutorService extends ManagedScheduledExecutorServiceImpl {

  /**
   * @param name
   * @param managedThreadFactory
   * @param hungTaskThreshold
   * @param longRunningTasks
   * @param corePoolSize
   * @param keepAliveTime
   * @param keepAliveTimeUnit
   * @param threadLifeTime
   * @param contextService
   * @param rejectPolicy
   */
  public DefaultManagedScheduledExecutorService(String name,
      ManagedThreadFactoryImpl managedThreadFactory, long hungTaskThreshold,
      boolean longRunningTasks, int corePoolSize, long keepAliveTime, TimeUnit keepAliveTimeUnit,
      long threadLifeTime, ContextServiceImpl contextService, RejectPolicy rejectPolicy) {
    super(name, managedThreadFactory, hungTaskThreshold, longRunningTasks, corePoolSize,
        keepAliveTime, keepAliveTimeUnit, threadLifeTime, contextService, rejectPolicy);
    // TODO Auto-generated constructor stub
  }

}
