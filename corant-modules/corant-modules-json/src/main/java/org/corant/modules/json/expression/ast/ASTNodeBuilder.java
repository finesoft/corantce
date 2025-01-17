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
package org.corant.modules.json.expression.ast;

import static org.corant.shared.util.Empties.isNotEmpty;
import org.corant.modules.json.expression.ast.ASTObjectNode.EntryNode;
import org.corant.shared.ubiquity.Sortable;

/**
 * corant-modules-json
 *
 * @author bingo 上午10:13:20
 */
public interface ASTNodeBuilder extends Sortable {

  ASTNodeBuilder DFLT = token -> ASTNodeType.decideType(token).buildNode(token);

  default ASTArrayNode arrayNodeOf(ASTNode<?>... elements) {
    ASTArrayNode node = new ASTArrayNode();
    if (isNotEmpty(elements)) {
      for (ASTNode<?> element : elements) {
        node.addChild(element);
      }
    }
    return node;
  }

  ASTNode<?> build(Object token);

  default EntryNode entryNodeOf(String key, ASTNode<?> valueNode) {
    return new EntryNode(key, valueNode);
  }

  default ASTObjectNode objectNode() {
    return new ASTObjectNode();
  }

  default ASTValueNode valueNodeOf(Object value) {
    return new ASTValueNode(value);
  }
}
