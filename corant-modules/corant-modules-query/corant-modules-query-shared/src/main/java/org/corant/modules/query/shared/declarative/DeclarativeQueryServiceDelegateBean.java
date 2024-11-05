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

import static java.lang.String.format;
import static org.corant.shared.util.Assertions.shouldNotNull;
import static org.corant.shared.util.Classes.getUserClass;
import static org.corant.shared.util.Configurations.getAssembledConfigValue;
import static org.corant.shared.util.Configurations.getConfigValue;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.Empties.isNotEmpty;
import static org.corant.shared.util.Strings.defaultBlank;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.corant.config.cdi.CurrentInjectionPoint;
import org.corant.context.AbstractBean;
import org.corant.context.Beans;
import org.corant.context.proxy.MethodInvoker;
import org.corant.context.proxy.ProxyBuilder;
import org.corant.context.qualifier.AutoCreated;
import org.corant.modules.query.QueryRuntimeException;
import org.corant.modules.query.QueryService;
import org.corant.modules.query.QueryService.Forwarding;
import org.corant.modules.query.QueryService.Paging;
import org.corant.modules.query.QueryService.QueryWay;
import org.corant.modules.query.mapping.Query.QueryType;
import org.corant.modules.query.shared.NamedQueryServiceManager;
import org.corant.modules.query.shared.QueryMappingService;
import org.corant.shared.normal.Names;
import org.corant.shared.util.Configurations;
import org.corant.shared.util.Objects;
import org.corant.shared.util.Types;

/**
 * corant-modules-query-shared
 * <p>
 * Unfinished yet
 *
 * @author bingo 下午2:03:58
 */
public class DeclarativeQueryServiceDelegateBean extends AbstractBean<Object> {

  static final boolean useDeclaredMethod =
      getConfigValue("corant.query.declarative.use-declared-method", Boolean.TYPE, false);
  static final boolean verifyDeclaredMethodReturnType = getConfigValue(
      "corant.query.declarative.verify-declared-method-return-type", Boolean.TYPE, false);

  static Logger logger = Logger.getLogger(DeclarativeQueryServiceDelegateBean.class.getName());

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
    QueryTypeQualifier queryTypeQualifier = ip == null ? null
        : ip.getQualifiers().stream().filter(QueryTypeQualifier.class::isInstance)
            .map(QueryTypeQualifier.class::cast).findAny().orElse(null);
    return ProxyBuilder.buildContextual(beanManager, proxyType,
        m -> getExecution(m, queryTypeQualifier));
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
      queryName = defaultBlank(getAssembledConfigValue(queryMethod.name()), queryName);
      queryWay = queryMethod.way();
    } else {
      queryWay = QueryWay.fromMethodName(method.getName());
    }
    final QueryService queryService = resolveQueryService(queryName, queryTypeQualifier);
    validateReturnType(queryName, method, queryWay);
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
    // TODO consider the bridge method "change" visibility of base class's methods ??
    if (!method.isDefault() && !method.isBridge() && !Modifier.isStatic(method.getModifiers())) {
      if (useDeclaredMethod) {
        if (Objects.indexOf(proxyType.getDeclaredMethods(), method) >= 0) {
          return createExecution(method, queryTypeQualifier);
        }
      } else {
        return createExecution(method, queryTypeQualifier);
      }
    }
    return null;
  }

  @SuppressWarnings("rawtypes")
  QueryService resolveQueryService(String queryName, QueryTypeQualifier queryTypeQualifier) {
    if (queryTypeQualifier == null) {
      return NamedQueryServiceManager.resolveQueryService(queryName);
    }
    QueryType type = queryTypeQualifier.type();
    String qualifier = Configurations.getAssembledConfigValue(queryTypeQualifier.qualifier());
    return shouldNotNull(NamedQueryServiceManager.tryResolveQueryService(type, qualifier),
        "Can't find any query service to execute declarative query %s %s %s.", proxyType, type,
        qualifier);
  }

  void validateReturnType(String queryName, Method method, QueryWay queryWay) {
    Class<?> returnClass = method.getReturnType();
    if (returnClass.equals(Void.TYPE) || returnClass.equals(Object.class)
        || Modifier.isStatic(method.getModifiers()) || method.isDefault()) {
      return;
    }
    Type returnType = method.getGenericReturnType();
    Type queryResultType =
        Beans.resolve(QueryMappingService.class).getQuery(queryName).getResultClass();
    if (queryResultType.equals(Map.class)) {
      queryResultType = Types.parameterize(Map.class, Object.class, Object.class);
    }
    if (queryWay == QueryWay.SELECT) {
      Type resultType = Types.parameterize(List.class, queryResultType);
      if (!Types.isAssignable(resultType, returnType)) {
        handleValidateReturnTypeIssues(proxyType, method, resultType);
      }
    } else if (queryWay == QueryWay.GET) {
      if (!Types.isAssignable(queryResultType, returnType)) {
        handleValidateReturnTypeIssues(proxyType, method, queryResultType);
      }
    } else if (queryWay == QueryWay.FORWARD) {
      Type resultType = Types.parameterize(Forwarding.class, queryResultType);
      if (!Types.isAssignable(resultType, returnType)) {
        handleValidateReturnTypeIssues(proxyType, method, resultType);
      }
    } else if (queryWay == QueryWay.PAGE) {
      Type resultType = Types.parameterize(Paging.class, queryResultType);
      if (!Types.isAssignable(resultType, returnType)) {
        handleValidateReturnTypeIssues(proxyType, method, resultType);
      }
    } else if ((queryWay == QueryWay.STREAM)) {
      Type resultType = Types.parameterize(Stream.class, queryResultType);
      if (!Types.isAssignable(resultType, returnType)) {
        handleValidateReturnTypeIssues(proxyType, method, resultType);
      }
    }
  }

  private void handleValidateReturnTypeIssues(Class<?> proxyType, Method method, Type resultType) {
    if (verifyDeclaredMethodReturnType) {
      throw new QueryRuntimeException(
          "Declarative query service [%s] method [%s] return type mismatch, the type should be [%s]",
          proxyType, method.getName(), Types.toString(resultType));
    } else {
      logger.warning(format(
          "Declarative query service [%s] method [%s] return type mismatch, the type should be [%s]",
          proxyType, method.getName(), Types.toString(resultType)));
    }
  }
}
