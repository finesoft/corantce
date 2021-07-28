/*
 * Copyright (c) 2013-2021, Bingo.Chen (finesoft@gmail.com).
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
package org.corant.modules.security.shared;

import static org.corant.shared.util.Lists.listOf;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

public class SimpleRoles implements Iterable<SimpleRole> {

  final Collection<SimpleRole> roles;

  public SimpleRoles(Collection<SimpleRole> roles) {
    this.roles = Collections.unmodifiableCollection(roles);
  }

  public static SimpleRoles of(SimpleRole... role) {
    return new SimpleRoles(listOf(role));
  }

  public static SimpleRoles of(String... name) {
    return new SimpleRoles(Arrays.stream(name).map(SimpleRole::new).collect(Collectors.toList()));
  }

  @Override
  public Iterator<SimpleRole> iterator() {
    return roles.iterator();
  }
}