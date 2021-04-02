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
package org.corant.modules.jpa.hibernate.orm;

import static org.corant.shared.util.Conversions.toObject;
import java.sql.Timestamp;
import org.corant.shared.exception.CorantRuntimeException;
import org.hibernate.FlushMode;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * corant-modules-jpa-hibernate-orm
 *
 * @author bingo 上午11:09:24
 *
 */
public class HibernateOrmSessionTimeService implements HibernateSessionTimeService {

  @Override
  public boolean accept(Class<?> provider) {
    return provider.equals(org.hibernate.jpa.HibernatePersistenceProvider.class);
  }

  @Override
  public long resolve(boolean useEpochSeconds, SharedSessionContractImplementor session,
      Object object) {
    final String timeSql = session.getFactory().getServiceRegistry().getService(JdbcServices.class)
        .getDialect().getCurrentTimestampSelectString();
    final FlushMode hfm = session.getHibernateFlushMode();
    try {
      session.setHibernateFlushMode(FlushMode.MANUAL);
      final long epochMillis =
          toObject(session.createNativeQuery(timeSql).getSingleResult(), Timestamp.class).getTime();
      return useEpochSeconds ? epochMillis / 1000L + 1 : epochMillis;
    } catch (Exception e) {
      throw new CorantRuntimeException(e);
    } finally {
      session.setHibernateFlushMode(hfm);
    }
  }

}