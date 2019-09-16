package org.corant.suites.query.mongodb;
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

import java.util.EnumMap;
import java.util.Map;
import org.bson.conversions.Bson;
import org.corant.suites.query.shared.NamedQuerier;
import org.corant.suites.query.shared.dynamic.DynamicQuerier;

/**
 * corant-suites-query
 *
 * @author bingo 下午3:13:37
 *
 */
public interface MgInLineNamedQueryResolver<K, P> {

  MgQuerier resolve(K key, P param);

  public enum MgOperator {

    FILTER("filter"), PROJECTION("projection"), MIN("min"), MAX("max"), HINT("hint"), SORT("sort");

    private String ops;

    private MgOperator(String ops) {
      this.ops = ops;
    }

    public String getOps() {
      return ops;
    }
  }

  interface MgQuerier
      extends DynamicQuerier<Map<String, Object>, EnumMap<MgOperator, Bson>>, NamedQuerier {
    String getOriginalScript();
  }

}
