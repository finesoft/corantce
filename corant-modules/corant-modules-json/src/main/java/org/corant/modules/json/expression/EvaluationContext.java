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
package org.corant.modules.json.expression;

import static org.corant.shared.util.Maps.getMapKeyPathValue;
import static org.corant.shared.util.Maps.mapOf;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.corant.modules.json.Jsons;
import org.corant.modules.json.expression.ast.ASTFunctionNode;
import org.corant.modules.json.expression.ast.ASTVariableNode;
import org.corant.shared.exception.NotSupportedException;
import org.corant.shared.ubiquity.Sortable;

/**
 * corant-modules-json
 *
 * @author bingo 下午2:23:09
 */
public interface EvaluationContext extends Sortable {

  Function<Object[], Object> resolveFunction(Node<?> node);

  Object resolveVariableValue(Node<?> node);

  /**
   * corant-modules-json
   *
   * @author bingo 16:58:46
   */
  class DefaultEvaluationContext implements EvaluationContext {

    protected final Map<String, Object> variables;
    protected final List<FunctionResolver> functionResolvers;

    public DefaultEvaluationContext(Map<String, Object> variables) {
      this.variables = new LinkedHashMap<>();
      if (variables != null) {
        this.variables.putAll(variables);
      }
      functionResolvers = SimpleParser.resolveFunction().collect(Collectors.toUnmodifiableList());
    }

    public DefaultEvaluationContext(Object... objects) {
      this(mapOf(objects));
    }

    @Override
    public Function<Object[], Object> resolveFunction(Node<?> node) {
      ASTFunctionNode funcNode = (ASTFunctionNode) node;
      return functionResolvers.stream().filter(fr -> fr.supports(funcNode.getName()))
          .min(Sortable::compare).orElseThrow(NotSupportedException::new)
          .resolve(funcNode.getName());
    }

    @Override
    public Object resolveVariableValue(Node<?> node) {
      ASTVariableNode varNode = (ASTVariableNode) node;
      return getMapKeyPathValue(variables, varNode.getNamespace(), false);// variables.get(varNode.getName());
    }

  }

  /**
   * corant-modules-json
   *
   * @author bingo 16:40:44
   */
  class SubEvaluationContext implements EvaluationContext {

    protected final EvaluationContext original;
    protected final Map<String, Object> bindings = new LinkedHashMap<>();

    public SubEvaluationContext(EvaluationContext original) {
      this.original = original;
    }

    public SubEvaluationContext bind(String name, Object value) {
      bindings.put(name, value);
      return this;
    }

    @Override
    public Function<Object[], Object> resolveFunction(Node<?> node) {
      return original.resolveFunction(node);
    }

    @Override
    public Object resolveVariableValue(Node<?> node) {
      if (node instanceof ASTVariableNode) {
    	ASTVariableNode varNode = (ASTVariableNode)node;
        String[] varNamespace = varNode.getNamespace();
        if (bindings.containsKey(varNamespace[0])) {
          Object boundValue = bindings.get(varNamespace[0]);
          if (varNamespace.length == 1) {
            return boundValue;
          } else if (boundValue instanceof Map<?, ?>) {
        	Map<?, ?> boundMap = (Map<?, ?>)boundValue;
            return getMapKeyPathValue(boundMap,
                Arrays.copyOfRange(varNamespace, 1, varNamespace.length), false);
          } else if (boundValue != null) {
            return getMapKeyPathValue(Jsons.convert(boundValue, Map.class),
                Arrays.copyOfRange(varNamespace, 1, varNamespace.length), false);
          } else {
            return null;
          }
        }
      }
      return original.resolveVariableValue(node);
    }

    public SubEvaluationContext unbind(String name) {
      bindings.remove(name);
      return this;
    }

    public SubEvaluationContext unbindAll() {
      bindings.clear();
      return this;
    }
  }
}
