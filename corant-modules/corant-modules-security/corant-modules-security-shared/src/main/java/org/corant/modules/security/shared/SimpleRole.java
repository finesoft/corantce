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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Predicate;
import org.corant.modules.security.Role;
import org.corant.modules.security.shared.util.StringPredicates;
import org.corant.shared.exception.NotSupportedException;

/**
 * corant-modules-security-shared
 *
 * @author bingo 下午4:33:46
 *
 */
public class SimpleRole implements Role {

  private static final long serialVersionUID = 1585708942349545935L;

  private final String name;
  private Predicate<String> predicate;

  public SimpleRole(String name) {
    this.name = name;
    predicate = StringPredicates.predicateOf(name);
  }

  public static SimpleRole of(String name) {
    return new SimpleRole(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SimpleRole other = (SimpleRole) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (name == null ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean implies(Role role) {
    if (!(role instanceof SimpleRole)) {
      return false;
    }
    return predicate.test(((SimpleRole) role).name);
  }

  @Override
  public String toString() {
    return "SimpleRole [name=" + name + "]";
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(Class<T> cls) {
    if (Role.class.isAssignableFrom(cls)) {
      return (T) this;
    }
    if (SimpleRole.class.isAssignableFrom(cls)) {
      return (T) this;
    }
    throw new NotSupportedException("Can't unwrap %s", cls);
  }

  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    predicate = StringPredicates.predicateOf(name);
  }

  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
  }
}