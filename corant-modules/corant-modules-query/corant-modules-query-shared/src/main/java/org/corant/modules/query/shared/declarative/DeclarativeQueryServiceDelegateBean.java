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
package org.corant.modules.query.shared.declarative;

import static org.corant.shared.util.Assertions.shouldNotNull;
import static org.corant.shared.util.Classes.getUserClass;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.Empties.isNotEmpty;
import static org.corant.shared.util.Strings.defaultBlank;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.corant.config.Configs;
import org.corant.config.cdi.CurrentInjectionPoint;
import org.corant.context.AbstractBean;
import org.corant.context.proxy.MethodInvoker;
import org.corant.context.proxy.ProxyBuilder;
import org.corant.context.qualifier.AutoCreated;
import org.corant.modules.query.QueryService;
import org.corant.modules.query.QueryService.QueryWay;
import org.corant.modules.query.mapping.Query.QueryType;
import org.corant.modules.query.shared.NamedQueryServiceManager;
import org.corant.shared.normal.Names;
import org.corant.shared.util.Configurations;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

/**
 * corant-modules-query-shared
 *
 * Unfinish yet
 *
 * @author bingo 下午2:03:58
 */
public class DeclarativeQueryServiceDelegateBean extends AbstractBean<Object> {

  static final Map<Method, MethodInvoker> methodInvokers = new ConcurrentHashMap<>();

  final Class<?> proxyType;

  public DeclarativeQueryServiceDelegateBean(BeanManager beanManager, Class<?> proxyType) {
    super(beanManager);
    this.proxyType = shouldNotNull(getUserClass(proxyType));
    qualifiers.add(AutoCreated.INST);
    qualifiers.add(Default.Literal.INSTANCE);
    qualifiers.add(Any.Literal.INSTANCE);
    types.add(proxyType);
    scope = ApplicationScoped.class;
    stereotypes.add(DeclarativeQueryService.class);
  }

  @Override
  public Object create(CreationalContext<Object> creationalContext) {
    InjectionPoint ip = (InjectionPoint) beanManager
        .getInjectableReference(new CurrentInjectionPoint(), creationalContext);
    QueryTypeQualifier queryTypeQualifier =
        ip.getQualifiers().stream().filter(QueryTypeQualifier.class::isInstance)
            .map(QueryTypeQualifier.class::cast).findAny().orElse(null);
    return ProxyBuilder.buildContextual(beanManager, proxyType,
        m -> getExecution(m, queryTypeQualifier));
  }

  @Override
  public void destroy(Object instance, CreationalContext<Object> creationalContext) {
    methodInvokers.clear();
  }

  @Override
  public String getId() {
    return proxyType.getName();
  }

  @Override
  public String getName() {
    return proxyType.getName();
  }

  @SuppressWarnings({"rawtypes"})
  MethodInvoker createExecution(Method method, QueryTypeQualifier queryTypeQualifier) {
    final QueryMethod[] queryMethods = method.getAnnotationsByType(QueryMethod.class);
    final QueryMethod queryMethod = isNotEmpty(queryMethods) ? queryMethods[0] : null;
    String queryName =
        proxyType.getSimpleName().concat(Names.NAME_SPACE_SEPARATORS).concat(method.getName());
    QueryWay queryWay;
    if (queryMethod != null) {
      queryName = defaultBlank(Configs.assemblyStringConfigProperty(queryMethod.name()), queryName);
      queryWay = queryMethod.way();
    } else {
      queryWay = QueryWay.fromMethodName(method.getName());
    }
    final QueryService queryService = resolveQueryService(queryName, queryTypeQualifier);
    return createExecution(queryService, queryName, queryWay);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  MethodInvoker createExecution(QueryService queryService, String queryName, QueryWay queryWay) {
    if (queryWay == QueryWay.GET) {
      return (target, args) -> queryService.get(queryName, isEmpty(args) ? null : args[0]);
    } else if (queryWay == QueryWay.SELECT) {
      return (target, args) -> queryService.select(queryName, isEmpty(args) ? null : args[0]);
    } else if (queryWay == QueryWay.PAGE) {
      return (target, args) -> queryService.page(queryName, isEmpty(args) ? null : args[0]);
    } else if (queryWay == QueryWay.FORWARD) {
      return (target, args) -> queryService.forward(queryName, isEmpty(args) ? null : args[0]);
    } else {
      return (target, args) -> queryService.stream(queryName, isEmpty(args) ? null : args[0]);
    }
  }

  MethodInvoker getExecution(Method method, QueryTypeQualifier queryTypeQualifier) {
    if (queryTypeQualifier != null) {
      // don't cache
      return createExecution(method, queryTypeQualifier);
    }
    return methodInvokers.computeIfAbsent(method, m -> createExecution(m, null));
  }

  @SuppressWarnings("rawtypes")
  QueryService resolveQueryService(String queryName, QueryTypeQualifier queryTypeQualifier) {
    if (queryTypeQualifier == null) {
      return NamedQueryServiceManager.resolveQueryService(queryName);
    }
    QueryType type = queryTypeQualifier.type();
    String qualifier = Configurations.getAssembledConfigValue(queryTypeQualifier.qualifier());
    return shouldNotNull(NamedQueryServiceManager.resolveQueryService(type, qualifier),
        "Can't find any query service to execute declarative query %s %s %s.", proxyType, type,
        qualifier);
  }

}
