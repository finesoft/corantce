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
package org.corant.suites.dsa.cluster.distance;

import java.util.Map;
import org.apache.commons.math3.exception.DimensionMismatchException;

/**
 * corant-suites-dsa
 *
 * @author bingo 下午1:30:50
 *
 */
public class EuclideanDistance extends SquaredEuclideanDistance {

  private static final long serialVersionUID = 6581941216598243825L;

  @Override
  public double calculate(Map<Object, Double> f1, Map<Object, Double> f2) {
    double sum = super.calculate(f1, f2);
    return Math.sqrt(sum);
  }

  @Override
  public double compute(double[] a, double[] b) throws DimensionMismatchException {
    double sum = super.compute(a, b);
    return Math.sqrt(sum);
  }

}