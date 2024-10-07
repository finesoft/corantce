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
package org.corant.modules.ddd.shared.repository;

import static java.util.Collections.singleton;
import static org.corant.context.Beans.resolve;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.corant.context.Beans;
import org.corant.context.qualifier.Qualifiers;
import org.corant.modules.ddd.Entity.EntityManagerProvider;
import org.corant.modules.ddd.annotation.Repositories;
import org.corant.modules.ddd.shared.unitwork.AbstractJPAUnitOfWorksManager;
import org.corant.modules.ddd.shared.unitwork.AbstractJTAJPAUnitOfWorksManager;
import org.corant.modules.ddd.shared.unitwork.UnitOfWorks;
import org.corant.modules.jpa.shared.JPAExtension;
import org.corant.modules.jpa.shared.PersistenceService.PersistenceContextLiteral;

/**
 * corant-modules-ddd-shared
 *
 * @author bingo 下午7:09:18
 */
public class JPARepositoryExtension implements Extension {

  final Map<String, Annotation[]> qualifiers = new HashMap<>();

  public Annotation[] resolveQualifiers(Class<?> cls) {
    return qualifiers
        .get(resolve(EntityManagerProvider.class).getPersistenceContext(cls).unitName());
  }

  public JPARepository resolveRepository(Class<?> cls) {
    return Beans.resolve(JPARepository.class, resolveQualifiers(cls));
  }

  protected synchronized void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery abd,
      final BeanManager beanManager) {
    Set<String> names =
        beanManager.getExtension(JPAExtension.class).getPersistenceUnitInfoMetaDatas().keySet()
            .stream().map(PersistenceUnit::unitName).collect(Collectors.toSet());
    qualifiers.clear();
    qualifiers.putAll(Qualifiers.resolveNameds(names));
    qualifiers.forEach((k, v) -> abd.<JPARepository>addBean().addQualifiers(v)
        .addTransitiveTypeClosure(DefaultJPARepository.class).beanClass(DefaultJPARepository.class)
        .scope(ApplicationScoped.class).stereotypes(singleton(Repositories.class))
        .produceWith(beans -> produce(beans, k)).disposeWith((repo, beans) -> {
        }));
  }

  protected synchronized void onBeforeShutdown(@Observes @Priority(0) BeforeShutdown bs) {
    qualifiers.clear();
  }

  protected JPARepository produce(Instance<Object> instances, String unitName) {
    AbstractJTAJPAUnitOfWorksManager uowm =
        instances.select(UnitOfWorks.class).get().currentDefaultUnitOfWorksManager();
    return new DefaultJPARepository(PersistenceContextLiteral.of(unitName), uowm);
  }

  /**
   * corant-modules-ddd-shared
   *
   * @author bingo 下午5:06:40
   */
  public static class DefaultJPARepository extends AbstractJPARepository {

    protected DefaultJPARepository() {}

    protected DefaultJPARepository(PersistenceContext pc, AbstractJPAUnitOfWorksManager uofm) {
      persistenceContext = pc;
      unitOfWorkManager = uofm;
    }
  }
}
