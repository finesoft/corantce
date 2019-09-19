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
package org.corant.suites.query.sql;

import java.util.ArrayList;
import java.util.List;
import org.corant.suites.query.shared.QueryParameter;
import org.corant.suites.query.shared.QueryParameterResolver;
import org.corant.suites.query.shared.QueryResultResolver;
import org.corant.suites.query.shared.dynamic.AbstractDynamicQuerierBuilder;
import org.corant.suites.query.shared.dynamic.javascript.NashornScriptEngines;
import org.corant.suites.query.shared.dynamic.javascript.NashornScriptEngines.ScriptFunction;
import org.corant.suites.query.shared.mapping.Query;

/**
 * corant-suites-query
 *
 * @author bingo 下午7:46:22
 *
 */
public class SqlNamedQuerierJsBuilder
    extends AbstractDynamicQuerierBuilder<Object[], String, DefaultSqlNamedQuerier> {

  final ScriptFunction execution;

  /**
   * @param query
   * @param parameterResolver
   * @param resultResolver
   */
  public SqlNamedQuerierJsBuilder(Query query, QueryParameterResolver parameterResolver,
      QueryResultResolver resultResolver) {
    super(query, parameterResolver, resultResolver);
    execution = NashornScriptEngines.compileFunction(query.getScript(), "p", "up");
  }

  /**
   * Generate SQL script with placeholder, and converted the parameter to appropriate type.
   */
  @Override
  public DefaultSqlNamedQuerier build(Object param) {
    QueryParameter queryParameter = resolveParameter(param);// convert parameter
    List<Object> useParam = new ArrayList<>();
    Object script = execution.apply(new Object[] {queryParameter, useParam});
    return new DefaultSqlNamedQuerier(getQuery(), queryParameter, getParameterResolver(),
        getResultResolver(), useParam.toArray(new Object[useParam.size()]), script.toString());
  }

}