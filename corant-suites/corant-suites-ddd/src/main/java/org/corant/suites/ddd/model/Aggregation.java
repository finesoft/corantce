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
package org.corant.suites.ddd.model;

import static org.corant.shared.util.ClassUtils.tryAsClass;
import java.beans.Transient;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import org.corant.suites.ddd.event.Event;
import org.corant.suites.ddd.message.Message;

/**
 * @author bingo 下午4:23:02
 * @since
 */
public interface Aggregation extends Entity {

  /**
   * If flush is true then the integration event queue will be clear
   */
  List<Message> extractMessages(boolean flush);

  /**
   * the lifeCycle of being
   */
  @Transient
  @javax.persistence.Transient
  Lifecycle getLifecycle();

  /**
   * the evolution version number, use as persistence version
   */
  Long getVn();

  /**
   * In this case, it means whether it is persisted or not
   */
  @Transient
  @javax.persistence.Transient
  default boolean isEnabled() {
    return getLifecycle() != null && getLifecycle().getSign() > 0;
  }

  /**
   * The aggregation isn't persisted, or is destroyed, but still live in memory until the GC
   * recycle.
   */
  @Transient
  @javax.persistence.Transient
  default Boolean isPhantom() {
    return getId() == null || !isEnabled();
  }

  /**
   * Raise events.
   */
  void raise(Event event, Annotation... qualifiers);

  /**
   * Raise messages
   */
  void raise(Message... messages);

  /**
   * Raise asynchronous events
   */
  void raiseAsync(Event event, Annotation... qualifiers);

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:04:45
   *
   */
  public static abstract class AggregationHandlerAdapter<P, T> extends EnablingHandlerAdapter<P, T>
      implements DestroyHandler<P, T> {

    @Override
    public void preDestroy(P param, T destroyable) {

    }

  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:04:52
   *
   */
  interface AggregationIdentifier extends EntityIdentifier {

    @Override
    Serializable getId();

    @Override
    String getType();

    default Class<?> getTypeCls() {
      return tryAsClass(getType());
    }
  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:04:55
   *
   */
  interface AggregationReference<T extends Aggregation> extends EntityReference<T> {

    Long getVn();

  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:04:58
   *
   */
  @FunctionalInterface
  interface Destroyable<P, T> {
    void destroy(P param, DestroyHandler<P, T> handler);
  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:05:02
   *
   */
  @FunctionalInterface
  interface DestroyHandler<P, T> {
    void preDestroy(P param, T destroyable);
  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:06:04
   *
   */
  public static abstract class DestroyHandlerAdapter<P, T> implements DestroyHandler<P, T> {
    @Override
    public void preDestroy(P param, T destroyable) {

    }
  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:05:07
   *
   */
  @FunctionalInterface
  interface Enabling<P, T> {
    T enable(P param, EnablingHandler<P, T> handler);
  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:05:13
   *
   */
  @FunctionalInterface
  interface EnablingHandler<P, T> {
    void preEnable(P param, T enabling);
  }

  /**
   * corant-suites-ddd
   *
   * @author bingo 下午9:05:19
   *
   */
  public static abstract class EnablingHandlerAdapter<P, T> implements EnablingHandler<P, T> {
    @Override
    public void preEnable(P param, T destroyable) {

    }
  }

  /**
   *
   * corant-suites-ddd
   *
   * @author bingo 下午9:05:24
   *
   */
  public enum Lifecycle {
    INITIAL(0), ENABLED(1), REENABLED(2), DESTROYED(-1);
    int sign;

    private Lifecycle(int sign) {
      this.sign = sign;
    }

    public int getSign() {
      return sign;
    }
  }

}