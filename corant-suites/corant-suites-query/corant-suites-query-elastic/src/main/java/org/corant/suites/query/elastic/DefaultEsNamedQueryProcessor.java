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
package org.corant.suites.query.elastic;

import static org.corant.shared.util.ClassUtils.isPrimitiveOrWrapper;
import static org.corant.shared.util.MapUtils.getMapInteger;
import static org.corant.shared.util.ObjectUtils.max;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.corant.kernel.service.ConversionService;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.suites.query.shared.QueryRuntimeException;
import org.corant.suites.query.shared.QueryUtils;
import org.corant.suites.query.shared.dynamic.freemarker.FreemarkerDynamicQueryProcessor;
import org.corant.suites.query.shared.mapping.Query;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonpCharacterEscapes;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * corant-suites-query
 *
 * @author bingo 下午8:25:44
 *
 */
@SuppressWarnings("rawtypes")
public class DefaultEsNamedQueryProcessor
    extends FreemarkerDynamicQueryProcessor<DefaultEsNamedQuerier, Map<String, Object>> {

  public final static ObjectMapper OM = new ObjectMapper();

  /**
   * @param query
   * @param conversionService
   */
  public DefaultEsNamedQueryProcessor(Query query, ConversionService conversionService) {
    super(query, conversionService);
  }

  @Override
  protected Map<String, Object> convertParameter(Map<String, Object> param) {
    Map<String, Object> convertedParam = new HashMap<>();
    Map<String, Object> tempParam = new HashMap<>();
    if (param != null) {
      tempParam.putAll(param);
      getParamConvertSchema().forEach((pn, pc) -> {
        if (tempParam.containsKey(pn)) {
          Object cvtVal = conversionService.convert(param.get(pn), pc);
          tempParam.put(pn, cvtVal);
        }
      });
    }
    tempParam.forEach((k, v) -> {
      try {
        if (v != null && isPrimitiveOrWrapper(v.getClass())) {
          convertedParam.put(k, v);
        } else {
          String jsonVal = v == null ? null : OM.writeValueAsString(v);
          convertedParam.put(k, jsonVal);
        }
      } catch (JsonProcessingException e) {
        throw new CorantRuntimeException(e, "Can not convert parameter %s to json string", k);
      }
    });
    return convertedParam;
  }

  @Override
  protected DefaultEsNamedQuerier doProcess(String script, Map<String, Object> param) {
    try {
      // OM.readValue(sw.toString(), Object.class) FIXME Do some protection
      final Map esQuery = OM.readValue(script, Map.class);
      doSomthing(esQuery, param);
      return new DefaultEsNamedQuerier(getQueryName(),
          OM.writer(JsonpCharacterEscapes.instance()).writeValueAsString(esQuery), getResultClass(),
          getHints(), getFetchQueries());
    } catch (IOException | NullPointerException e) {
      throw new QueryRuntimeException(e, "Freemarker process stringTemplate is error!");
    }
  }

  @SuppressWarnings("unchecked")
  void doSomthing(Map esQuery, Map<String, Object> param) {
    Integer from = getMapInteger(param, QueryUtils.OFFSET_PARAM_NME);
    if (from != null) {
      esQuery.put("from", max(from, Integer.valueOf(0)));
    }
    Integer size = getMapInteger(param, QueryUtils.LIMIT_PARAM_NME);
    if (size != null) {
      esQuery.put("size", max(size, Integer.valueOf(0)));
    }
  }

}
