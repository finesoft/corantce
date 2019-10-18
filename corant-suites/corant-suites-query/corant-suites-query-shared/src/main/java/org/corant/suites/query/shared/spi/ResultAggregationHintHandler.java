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
package org.corant.suites.query.shared.spi;

import static org.corant.shared.util.CollectionUtils.linkedHashSetOf;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.Empties.isNotEmpty;
import static org.corant.shared.util.ObjectUtils.isEquals;
import static org.corant.shared.util.StringUtils.isNotBlank;
import static org.corant.shared.util.StringUtils.split;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.corant.kernel.api.ConversionService;
import org.corant.suites.query.shared.QueryService.ForwardList;
import org.corant.suites.query.shared.QueryService.PagedList;
import org.corant.suites.query.shared.mapping.QueryHint;
import org.corant.suites.query.shared.mapping.QueryHint.QueryHintParameter;

/**
 * corant-suites-query
 *
 * <p>
 * The result field conversion hints.
 * <li>The key is 'result-field-convert'</li>
 * <li>The value of the parameter that named 'field-name' is the field name that will be
 * convert.</li>
 * <li>The value of the parameter that named 'target-type' is the target class name that the field
 * value will be convert to.</li>
 * <li>The values of the parameter that named 'convert-hint-key' and 'convert-hint-value' are the
 * conversion service hints, use for intervene conversion process.</li>
 * </p>
 * <p>
 * Use case:
 *
 * <pre>
 * &lt;query name="QueryService.get" result-class="java.util.Map"&gt;
 *       &lt;script&gt;
 *           &lt;![CDATA[
 *               SELECT o.id,o.name,m.f1,m.f2 FROM ONE o LEFT JOIN MANY m ON o.id = m.oId
 *           ]]&gt;
 *       &lt;/script&gt;
 *       &lt;hint key="result-aggregation"&gt;
 *           &lt;parameter name="aggs-field-names" value="id,name" /&gt;
 *           &lt;parameter name="aggs-name" value="list" /&gt;
 *       &lt;/hint&gt;
 * &lt;/query&gt;
 * </pre>
 * </p>
 *
 * @see ConversionService
 * @see org.corant.shared.conversion.Conversions
 * @author bingo 下午12:02:08
 *
 */
@ApplicationScoped
public class ResultAggregationHintHandler implements ResultHintHandler {

  public static final String HINT_NAME = "result-aggregation";
  public static final String HNIT_PARA_AGGS_FIELD_NME = "aggs-field-names";
  public static final String HNIT_PARA_AGGS_NME = "aggs-name";

  static final Map<String, Consumer<List<Map<?, ?>>>> caches = new ConcurrentHashMap<>();
  static final Set<String> brokens = new CopyOnWriteArraySet<>();

  @Inject
  Logger logger;

  @Override
  public boolean canHandle(Class<?> resultClass, QueryHint hint) {
    return (resultClass == null || Map.class.isAssignableFrom(resultClass)) && hint != null
        && isEquals(hint.getKey(), HINT_NAME);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void handle(QueryHint qh, Object parameter, Object result) throws Exception {
    Consumer<List<Map<?, ?>>> handler = null;
    if (brokens.contains(qh.getId()) || (handler = resolveHint(qh)) == null) {
      return;
    }
    List<Map<?, ?>> list = null;
    if (result instanceof ForwardList) {
      list = ((ForwardList) result).getResults();
    } else if (result instanceof List) {
      list = (List) result;
    } else if (result instanceof PagedList) {
      list = ((PagedList) result).getResults();
    }
    if (!isEmpty(list)) {
      handler.accept(list);
    }
  }

  protected Consumer<List<Map<?, ?>>> resolveHint(QueryHint qh) {
    if (caches.containsKey(qh.getId())) {
      return caches.get(qh.getId());
    } else {
      List<QueryHintParameter> aggNames = qh.getParameters(HNIT_PARA_AGGS_NME);
      List<QueryHintParameter> aggFieldNames = qh.getParameters(HNIT_PARA_AGGS_FIELD_NME);
      try {
        Set<String> fieldNames;
        String aggName = null;
        if (isNotEmpty(aggFieldNames) && isNotEmpty(aggNames)
            && isNotBlank(aggName = aggNames.get(0).getValue()) && isNotEmpty(fieldNames =
                linkedHashSetOf(split(aggFieldNames.get(0).getValue(), ",", true, true)))) {
          final String useAggName = aggName;
          return caches.computeIfAbsent(qh.getId(), (k) -> (list) -> {
            Map<Map<Object, Object>, List<Map<Object, Object>>> temp =
                new LinkedHashMap<>(list.size());
            for (Map<?, ?> src : list) {
              Map<Object, Object> key = new LinkedHashMap<>();
              Map<Object, Object> val = new LinkedHashMap<>();
              for (Map.Entry<?, ?> e : src.entrySet()) {
                if (!fieldNames.contains(e.getKey())) {
                  val.put(e.getKey(), e.getValue());
                } else {
                  key.put(e.getKey(), e.getValue());
                }
              }
              temp.computeIfAbsent(key, vk -> new ArrayList<>()).add(val);
            }
            list.clear();
            for (Entry<Map<Object, Object>, List<Map<Object, Object>>> e : temp.entrySet()) {
              e.getKey().put(useAggName, e.getValue());
              list.add(e.getKey());
            }
            temp.clear();
          });
        }
      } catch (Exception e) {
        logger.log(Level.WARNING, e, () -> "The query hint has some error!");
      }
    }
    brokens.add(qh.getId());
    return null;
  }
}
