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
import static org.corant.shared.util.Strings.isNotBlank;
import java.util.ArrayList;
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
      case CP_EQS:
      case CP_GT:
      case CP_GTE:
      case CP_LT:
      case CP_LTE:
      case CP_NE:
      case CP_NES:
      case LG_XOR: {
        if (node.getChildren().size() == 1) {
          shouldBeTrue(
              node.getChildren().get(0) instanceof ASTArrayNode an && an.getChildren().size() == 2,
              () -> new ParseException("AST node [%s] must contain 2 children nodes",
                  node.getType().token()));
        } else {
          shouldBeTrue(node.getChildren().size() == 2,
              () -> new ParseException("AST node [%s] must contain 2 children nodes",
                  node.getType().token()));
        }
        break;
      }
      case NON_NULL:
      case IS_NULL:
        shouldBeTrue(node.getChildren().size() == 1);
        break;
      case CP_REGEX: {
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
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
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        shouldBeTrue(sizeOf(nodes) == 3,
            () -> new ParseException("AST node [%s] must contain 3 children nodes",
                node.getType().token()));
        break;
      }
      case CONDITIONAL:
      case NVL: {
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        shouldBeTrue(sizeOf(nodes) == 2 || sizeOf(nodes) == 3,
            () -> new ParseException("AST node [%s] must contain 2 or 3 children nodes",
                node.getType().token()));
        break;
      }
      case CP_IN:
      case CP_NIN:
      case LG_AND:
      case LG_NOT:
      case LG_NOR:
      case LG_OR:
      case DISTINCT:
      case RETURN:
      case SUBROUTINE:
        shouldBeTrue(!node.getChildren().isEmpty());
        break;
      case FILTER: {
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        shouldBeTrue(
            sizeOf(nodes) == 3 && nodes.get(1) instanceof ASTDeclarationNode vn
                && vn.value() instanceof String vns && isNotBlank(vns),
            () -> new ParseException(
                "AST node [%s] must contain 3 children nodes and the second must be a declaration node",
                node.getType().token()));
        break;
      }
      case MAP: {
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        shouldBeTrue(
            sizeOf(nodes) == 3 && nodes.get(1) instanceof ASTDeclarationNode vn
                && vn.getVariableNames().length == 1,
            () -> new ParseException(
                "AST node [%s] must contain 3 children nodes and the second must be a declaration node containing 1 variable declaration",
                node.getType().token()));
        break;
      }
      case SORT:
      case MAX:
      case MIN: {
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        shouldBeTrue(
            sizeOf(nodes) == 3 && nodes.get(1) instanceof ASTDeclarationNode vn
                && vn.getVariableNames().length == 2,
            () -> new ParseException(
                "AST node [%s] nodes must contain 3 children nodes and the second must be a declaration node containing 2 variable declarations",
                node.getType().token()));
        break;
      }
      case REDUCE: {
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        if (sizeOf(nodes) == 3) {
          shouldBeTrue(
              nodes.get(1) instanceof ASTDeclarationNode vn && vn.getVariableNames().length == 2);
        } else if (sizeOf(nodes) == 4) {
          shouldBeTrue(
              nodes.get(2) instanceof ASTDeclarationNode vn && vn.getVariableNames().length == 2);
        }
        // else if (node.getChildren().size() == 6) {
        // shouldBeTrue(node.getChildren().get(2) instanceof ASTDeclarationNode avn
        // && avn.getVariableNames().length == 2
        // && node.getChildren().get(4) instanceof ASTDeclarationNode cvn
        // && cvn.getVariableNames().length == 2);
        // }
        else {
          throw new ParseException(
              "AST node [%s] must contain a target object and an identity object (optional) and a variable declaration and an accumulator expression!",
              node.getType().token());
        }
        break;
      }
      case COLLECT: {
        // if (node.getChildren().size() == 6) {
        // shouldBeTrue(node.getChildren().get(2) instanceof ASTDeclarationNode avn
        // && avn.getVariableNames().length == 2
        // && node.getChildren().get(4) instanceof ASTDeclarationNode cvn
        // && cvn.getVariableNames().length == 2);
        // } else
        List<? extends Node<?>> nodes = new ArrayList<>();
        if (node.getChildren().size() == 1
            && node.getChildren().get(0) instanceof ASTArrayNode an) {
          nodes = an.getChildren();
        } else {
          nodes = node.getChildren();
        }
        if (sizeOf(nodes) == 4) {
          shouldBeTrue(
              nodes.get(2) instanceof ASTDeclarationNode avn && avn.getVariableNames().length == 2);
        } else {
          throw new ParseException(
              "AST node [%s] must contain a target object and a variable declaration and an accumulator expression!",
              node.getType().token());
        }
        break;
      }
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
