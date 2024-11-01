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
package org.corant.kernel.spi;

import static org.corant.shared.util.Empties.isEmpty;
import java.util.stream.Stream;
import org.corant.Corant;
import org.corant.kernel.event.PostContainerReadyEvent;
import org.corant.kernel.event.PostCorantReadyEvent;
import org.corant.shared.ubiquity.Sortable;
import org.corant.shared.util.Objects;
import org.corant.shared.util.Services;

/**
 * corant-kernel
 *
 * <p>
 * This class provides a convenient mechanism for processing pre-conditions and post-conditions for
 * application startup, it is used to do some related operations before and after the container is
 * started. This class uses a relatively low-level {@link java.util.ServiceLoader} mechanism and
 * does not rely on other mechanisms.
 *
 * @author bingo 下午2:30:23
 */
public interface CorantBootHandler extends Sortable {

  /**
   * Return the sorted CorantBootHandler instance stream, using {@link java.util.ServiceLoader} and
   * {@link org.corant.shared.util.Services#selectRequired(Class,ClassLoader)} mechanism to load.
   *
   * @param classLoader the class loader use for load the CorantBootHandler
   * @param excludeClassNames the excluded handler class names
   *
   * @see Sortable#compare(Sortable, Sortable)
   * @see Services#selectRequired(Class, ClassLoader)
   */
  static Stream<CorantBootHandler> load(ClassLoader classLoader, String... excludeClassNames) {
    if (isEmpty(excludeClassNames)) {
      return Services.selectRequired(CorantBootHandler.class, classLoader);
    } else {
      return Services.selectRequired(CorantBootHandler.class, classLoader)
          .filter(h -> Objects.indexOf(excludeClassNames, h.getClass().getName()) == -1);
    }
  }

  /**
   * Called after the container was started, the CDI context is available.
   *
   * <p>
   * Note: This method was invoked after emitted the {@link PostContainerReadyEvent} and before emit
   * the {@link PostCorantReadyEvent}. If the application startup with
   * {@link Corant#DISABLE_AFTER_STARTED_HANDLER_CMD} argument, this method will not be invoked. If
   * this method throws an exception, it will not affect the operation of the container, but the
   * {@link PostCorantReadyEvent} event will not be emitted, and if there are multiple Handlers,
   * this method of the lower priority Handler will not be invoked.
   *
   * @param corant the Corant instance is started currently
   * @param args the application startup arguments, the implementer can perform corresponding
   *        operations based on the arguments.
   */
  default void handleAfterStarted(Corant corant, String... args) {}

  /**
   * Called after the container was stopped, so operations related to the CDI context should not be
   * used in the method.
   *
   * <p>
   * Note: If this method throws an exception, and if there are multiple Handlers, this method of
   * the lower priority Handler will not be invoked. In some cases if one use daemon thread to shut
   * down Corant this method may not be invoked.
   *
   * @param classLoader the class loader use for this application
   * @param args the application startup arguments, the implementer can perform corresponding
   *        operations based on the arguments.
   */
  default void handleAfterStopped(ClassLoader classLoader, String... args) {}

  /**
   * Called before the container is started, so operations related to the CDI context should not be
   * used in the method.
   *
   * <p>
   * Note: If the application startup with {@link Corant#DISABLE_BEFORE_START_HANDLER_CMD} argument,
   * this method will not be invoked. If this method throws an exception, the container may not
   * start up, and if there are multiple Handlers, this method of the lower priority Handler will
   * not be invoked.
   *
   * @param classLoader the class loader use for this application
   * @param args the application startup arguments, the implementer can perform corresponding
   *        operations based on the arguments.
   */
  void handleBeforeStart(ClassLoader classLoader, String... args);

}
