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
package org.corant.suites.ddd.repository;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Tuple;

public interface TupleObjectConverter {

  <T> T convert(Tuple tuple, Class<T> type);

  default <T> List<T> convert(List<Tuple> tuples, Class<T> type) {
    List<T> result = new ArrayList<>(tuples.size());
    for (Tuple tuple : tuples) {
      result.add(convert(tuple, type));
    }
    return result;
  }

}
