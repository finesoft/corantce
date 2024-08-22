/*
 * Copyright (c) 2013-2023, Bingo.Chen (finesoft@gmail.com).
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
package org.corant.modules.query.jaxrs;

import java.util.function.Function;
import jakarta.ws.rs.client.Client;
import org.corant.modules.query.mapping.Query;
import org.corant.shared.exception.NotSupportedException;

/**
 * corant-modules-query-jaxrs
 *
 * @author bingo 20:21:17
 */
@FunctionalInterface
public interface JaxrsNamedQueryClientResolver extends Function<Query, Client> {

  default void closeAndClear() {}

  default JaxrsNamedQueryClientConfig getClientConfig(Query query) {
    throw new NotSupportedException();
  }
}