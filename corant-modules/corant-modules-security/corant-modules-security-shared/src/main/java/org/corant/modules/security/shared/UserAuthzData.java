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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * corant-modules-security-shared
 *
 * @author bingo 下午4:11:00
 *
 */
public class UserAuthzData extends SimpleAuthzData {

  protected Serializable userId;

  public UserAuthzData(Serializable userId, Collection<String> roles) {
    super(roles);
    this.userId = userId;
  }

  public UserAuthzData(Serializable userId, List<SimpleRole> roles) {
    super(roles);
    this.userId = userId;
  }

  public UserAuthzData(Serializable userId, String... roles) {
    super(roles);
    this.userId = userId;
  }

  protected UserAuthzData() {}

  public Serializable getUserId() {
    return userId;
  }

}