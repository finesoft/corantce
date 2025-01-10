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

import static org.corant.shared.util.Assertions.shouldBeTrue;
import static org.corant.shared.util.Empties.sizeOf;

import java.util.List;

import org.corant.modules.json.expression.Node;
import org.corant.modules.json.expression.ParseException;
import org.corant.shared.ubiquity.Sortable;

/**
 * corant-modules-json
 *
 * @author bingo 下午5:01:15
 */
public interface ASTNodeVisitor extends Sortable {

  ASTNodeVisitor DFLT = node -> {
    switch (node.getType()) {
      case CP_EQ:
      case CP_GT:
      case CP_GTE:
      case CP_LT:
      case CP_LTE:
      case CP_NE:
      case LG_XOR: {
        if (node.getChildren().size() == 1) {
          shouldBeTrue(
              node.getChildren().get(0) instanceof ASTArrayNode && ((ASTArrayNode)node.getChildren().get(0)).getChildren().size() == 2,
              () -> new ParseException("AST node [%s] must contain 2 children nodes",
                  node.getType().token()));
        } else {
          shouldBeTrue(node.getChildren().size() == 2,
              () -> new ParseException("AST node [%s] must contain 2 children nodes",
                  node.getType().token()));
        }
        break;
      }
      case CP_REGEX: {
        List<? extends Node<?>> nodes;
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode) {
          ASTArrayNode an = (ASTArrayNode) node.getChildren().get(0);
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        shouldBeTrue(
            sizeOf(nodes) == 2 && ((ASTNode<?>) nodes.get(1)).getType() == ASTNodeType.VALUE,
            () -> new ParseException(
                "AST node [%s] must contain 2 children nodes and the second must be a value node",
                node.getType().token()));
        break;
      }
      case CP_BTW: {
        List<? extends Node<?>> nodes;
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode) {
          ASTArrayNode an = (ASTArrayNode) node.getChildren().get(0);
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        shouldBeTrue(sizeOf(nodes) == 3,
            () -> new ParseException("AST node [%s] must contain 3 children nodes",
                node.getType().token()));
        break;
      }
      case CP_IN:
      case CP_NIN:
      case LG_AND:
      case LG_NOT:
      case LG_NOR:
      case LG_OR:
      case RETURN:
        shouldBeTrue(!node.getChildren().isEmpty(),
            () -> new ParseException("AST node [%s] must have children nodes",
                node.getType().token()));
        break;
      default:
        break;
    }
  };

  default void prepare(ASTNode<?> node) {}

  default boolean supports(ASTNodeType type) {
    return true;
  }

  void visit(ASTNode<?> node);

}
