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
package org.corant.modules.jpa.hibernate.ogm;

import static org.corant.context.Instances.resolve;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import org.corant.modules.jpa.shared.JPAExtension;
import org.corant.modules.jpa.shared.PersistenceService.PersistenceUnitLiteral;
import org.corant.modules.jpa.shared.metadata.PersistenceUnitInfoMetaData;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.hibernate.ogm.cfg.OgmProperties;
import org.hibernate.ogm.jpa.HibernateOgmPersistence;

/**
 * corant-modules-jpa-hibernate-orm
 *
 * @author bingo 下午6:37:17
 *
 */
public class ExtendedHibernateOgmPersistenceProvider extends HibernateOgmPersistence {

  @Override
  public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName,
      @SuppressWarnings("rawtypes") Map properties) {
    EntityManagerFactory emf = super.createEntityManagerFactory(persistenceUnitName, properties);
    if (emf == null) {
      EntityManagerFactoryBuilder builder = resolveBuilder(persistenceUnitName, properties);
      if (builder != null) {
        return builder.build();
      }
    }
    return emf;
  }

  @SuppressWarnings("unchecked")
  protected EntityManagerFactoryBuilder resolveBuilder(String persistenceUnitName,
      @SuppressWarnings("rawtypes") Map map) {
    PersistenceUnitInfoMetaData pui = resolve(JPAExtension.class)
        .getPersistenceUnitInfoMetaData(PersistenceUnitLiteral.of(persistenceUnitName));
    if (pui != null
        && pui.getPersistenceProviderClassName().equals(HibernateOgmPersistence.class.getName())) {
      Map<?, ?> integration =
          map == null ? Collections.emptyMap() : Collections.unmodifiableMap(map);
      Map<Object, Object> protectiveCopy = new HashMap<Object, Object>(integration);
      protectiveCopy.put(AvailableSettings.DATASOURCE, "---PlaceHolderDSForOGM---");
      protectiveCopy.put(OgmProperties.ENABLED, true);
      protectiveCopy.put(org.hibernate.jpa.AvailableSettings.PROVIDER,
          HibernatePersistenceProvider.class.getName());
      final PersistenceUnitInfoMetaData thePui =
          pui.with(pui.getProperties(), pui.getPersistenceUnitTransactionType());
      return Bootstrap.getEntityManagerFactoryBuilder(new PersistenceUnitInfoDescriptor(thePui),
          protectiveCopy, (ClassLoader) null);
    }
    return null;
  }
}