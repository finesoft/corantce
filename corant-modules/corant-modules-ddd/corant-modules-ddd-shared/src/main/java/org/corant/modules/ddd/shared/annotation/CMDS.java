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
package org.corant.modules.ddd.shared.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * corant-modules-ddd-shared
 *
 * Mark the use of XA resources
 *
 * @author bingo 下午9:05:13
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Qualifier
public @interface CMDS {

  @Nonbinding
  Class<?> value();

  class CMDSLiteral extends AnnotationLiteral<CMDS> implements CMDS {

    private static final long serialVersionUID = -5552841006073177750L;

    private final Class<?> type;

    public CMDSLiteral(Class<?> type) {
      this.type = type;
    }

    public static CMDSLiteral of(Class<?> type) {
      return new CMDSLiteral(type);
    }

    @Override
    public Class<?> value() {
      return type;
    }
  }
}