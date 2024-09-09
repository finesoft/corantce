/*
 * Copyright (c) 2013-2023, Bingo.Chen (finesoft@gmail.com).
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
package org.corant.modules.query.sql;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static org.corant.context.Beans.resolve;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.Empties.isNotEmpty;
import static org.corant.shared.util.Maps.getMapMap;
import static org.corant.shared.util.Strings.EMPTY;
import static org.corant.shared.util.Strings.isBlank;
import static org.corant.shared.util.Strings.isNotBlank;
import static org.corant.shared.util.Strings.trim;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.corant.modules.datasource.shared.DBMS;
import org.corant.modules.datasource.shared.DataSourceService;
import org.corant.modules.json.ObjectMappers;
import org.corant.modules.query.mapping.FetchQuery;
import org.corant.modules.query.mapping.FetchQuery.FetchQueryParameterSource;
import org.corant.modules.query.mapping.Query;
import org.corant.modules.query.mapping.Query.QueryType;
import org.corant.modules.query.mapping.Script.ScriptType;
import org.corant.modules.query.shared.QueryMappingService;
import org.corant.modules.query.shared.dynamic.freemarker.FreemarkerExecutions;
import org.corant.modules.query.shared.dynamic.jsonexpression.JsonExpressionScriptProcessor;
import org.corant.modules.query.sql.cdi.SqlNamedQueryServiceManager;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.shared.ubiquity.Experimental;
import org.corant.shared.ubiquity.Immutable.ImmutableSetBuilder;
import org.corant.shared.ubiquity.Mutable.MutableString;
import org.corant.shared.ubiquity.Tuple.Pair;
import org.corant.shared.util.Functions;
import org.corant.shared.util.Strings;
import org.corant.shared.util.Systems;
import com.fasterxml.jackson.core.JsonProcessingException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.ValidationException;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.metadata.JdbcDatabaseMetaDataCapability;
import net.sf.jsqlparser.util.validation.metadata.NamesLookup;

/**
 * corant-modules-query-sql
 *
 * @author bingo 14:15:32
 */
public class SqlQueryDeveloperKits {

  public static final Set<String> DEFAULT_DIRECTIVE_PATTERNS =
      new ImmutableSetBuilder<>("<[#@].*?>\\n", "<[#@].*?>\\r", "<[#@].*?>", "</[#@].*?>\\n",
          "</[#@].*?>\\r", "</[#@].*?>").build();

  public static final Set<String> DEFAULT_EX_MACRO_DIRECTIVE_PATTERNS =
      new ImmutableSetBuilder<>("<#(?!macro).*?>\\n", "<#(?!macro).*?>\\r", "<#(?!macro).*?>",
          "</#(?!macro).*?>\\n", "</#(?!macro).*?>\\r", "</#(?!macro).*?>").build();

  public static final Set<String> DEFAULT_VARIABLE_PATTERNS =
      new ImmutableSetBuilder<>("\\$\\{.*?}").build();

  public static final Set<String> DEFAULT_TM_VARIABLE_PATTERNS =
      new ImmutableSetBuilder<>("\\$\\{TM.*?}", "\\$\\{CM.*?}").build();

  public static final String JSE_RESULT_PARAM_PATTERN = "@r\\.[.a-zA-Z0-9]*";

  public static final String JSE_FETCH_RESULT_PARAM_PATTERN = "@fr\\.[.a-zA-Z0-9]*";

  @Experimental
  public static FreemarkerQueryScriptValidator freemarkerQueryScriptValidator() {
    return new FreemarkerQueryScriptValidator();
  }

  @Experimental
  public static List<String> resolveJSEResultVariableNames(String express, boolean fetch) {
    if (isNotBlank(express)) {
      Pattern pattern =
          Pattern.compile(fetch ? JSE_FETCH_RESULT_PARAM_PATTERN : JSE_RESULT_PARAM_PATTERN);
      Matcher matcher = pattern.matcher(express);
      List<String> matches = new ArrayList<>();
      while (matcher.find()) {
        String name = matcher.group();
        if (fetch && name.length() > 4) {
          name = name.substring(4);
        } else if (name.length() > 3) {
          name = name.substring(3);
        }
        matches.add(name);
      }
      return unmodifiableList(matches);
    }
    return emptyList();
  }

  /**
   * corant-modules-query-sql
   *
   * @author bingo 18:48:09
   */
  public static class FreemarkerQueryScriptValidator {
    Map<String, String> specVarReplacements = new LinkedHashMap<>();
    Map<String, String> specVarPatternReplacements = new LinkedHashMap<>();
    Set<String> directivePatterns = DEFAULT_DIRECTIVE_PATTERNS;
    Set<String> variablePatterns = DEFAULT_VARIABLE_PATTERNS;
    String defaultVariableReplacement = "NULL";
    String defaultDirectiveReplacement = "";
    FeatureConfiguration featureConfiguration = new FeatureConfiguration();
    boolean includeMacro = false;
    boolean includeFetchQueryHandling;
    boolean printSkipDirectiveStacks = false;
    Set<String> skipQueryQualifier = new HashSet<>();
    Predicate<String> queryNameSkipper = Functions.emptyPredicate(false);
    Predicate<String> queryNameFilter = Functions.emptyPredicate(true);
    List<String> skipDirectiveStacks = new ArrayList<>();// TODO FIXME

    public FreemarkerQueryScriptValidator addSkipDirectiveStacks(String... strings) {
      for (String s : strings) {
        if (isNotBlank(s)) {
          skipDirectiveStacks.add(trim(s));
        }
      }
      return this;
    }

    public FreemarkerQueryScriptValidator defaultDirectiveReplacement(
        String defaultDirectiveReplacement) {
      this.defaultDirectiveReplacement = defaultDirectiveReplacement;
      return this;
    }

    public FreemarkerQueryScriptValidator defaultVariableReplacement(
        String defaultVariableReplacement) {
      this.defaultVariableReplacement = defaultVariableReplacement;
      return this;
    }

    public FreemarkerQueryScriptValidator featureConfiguration(
        FeatureConfiguration featureConfiguration) {
      if (featureConfiguration != null) {
        this.featureConfiguration = featureConfiguration;
      }
      return this;
    }

    @Experimental
    public FreemarkerQueryScriptValidator includeFetchQueryHandling(
        boolean includeFetchQueryHandling) {
      this.includeFetchQueryHandling = includeFetchQueryHandling;
      return this;
    }

    @Experimental
    public FreemarkerQueryScriptValidator includeMacro(boolean includeMacro) {
      this.includeMacro = includeMacro;
      if (this.includeMacro) {
        directivePatterns = DEFAULT_EX_MACRO_DIRECTIVE_PATTERNS;
        variablePatterns = DEFAULT_TM_VARIABLE_PATTERNS;
      } else {
        directivePatterns = DEFAULT_DIRECTIVE_PATTERNS;
        variablePatterns = DEFAULT_VARIABLE_PATTERNS;
      }
      return this;
    }

    public FreemarkerQueryScriptValidator printSkipDirectiveStacks(
        boolean printSkipDirectiveStacks) {
      this.printSkipDirectiveStacks = printSkipDirectiveStacks;
      return this;
    }

    public FreemarkerQueryScriptValidator putSpecVarPatternReplacement(String string,
        String replacement) {
      specVarPatternReplacements.put(string, replacement);
      return this;
    }

    public FreemarkerQueryScriptValidator putSpecVarReplacement(String string, String replacement) {
      specVarReplacements.put(string, replacement);
      return this;
    }

    public FreemarkerQueryScriptValidator queryNameFilter(Predicate<String> queryNameFilter) {
      if (this.queryNameFilter != null) {
        this.queryNameFilter = queryNameFilter;
      } else {
        this.queryNameFilter = Functions.emptyPredicate(true);
      }
      return this;
    }

    public FreemarkerQueryScriptValidator queryNameSkipper(Predicate<String> queryNameSkipper) {
      if (this.queryNameSkipper != null) {
        this.queryNameSkipper = queryNameSkipper;
      } else {
        this.queryNameSkipper = Functions.emptyPredicate(false);
      }
      return this;
    }

    public FreemarkerQueryScriptValidator removeSkipDirectiveStacksIf(Predicate<String> predicate) {
      skipDirectiveStacks.removeIf(predicate);
      return this;
    }

    public FreemarkerQueryScriptValidator removeSpecVarPatternReplacementIf(
        Predicate<String> predicate) {
      if (predicate != null) {
        List<String> removeKeys =
            specVarPatternReplacements.keySet().stream().filter(predicate).toList();
        if (isNotEmpty(removeKeys)) {
          removeKeys.forEach(specVarPatternReplacements::remove);
        }
      }
      return this;
    }

    public FreemarkerQueryScriptValidator removeSpecVarReplacementIf(Predicate<String> predicate) {
      if (predicate != null) {
        List<String> removeKeys = specVarReplacements.keySet().stream().filter(predicate).toList();
        if (isNotEmpty(removeKeys)) {
          removeKeys.forEach(specVarReplacements::remove);
        }
      }
      return this;
    }

    public void validate() {
      try {
        final QueryMappingService service = resolve(QueryMappingService.class);
        final SqlNamedQueryServiceManager sqlQueryService =
            resolve(SqlNamedQueryServiceManager.class);
        final DataSourceService dataSources = resolve(DataSourceService.class);
        Map<String, List<ValidationError>> errorMaps = new LinkedHashMap<>();
        Map<String, List<String>> queryFieldNames = new LinkedHashMap<>();
        boolean hasErrors = false;
        for (Query query : service.getQueries()) {
          if (query.getScript().getType() != ScriptType.FM || query.getType() != QueryType.SQL
              || skipQueryQualifier.contains(query.getQualifier())
              || queryNameSkipper.test(query.getVersionedName())
              || !queryNameFilter.test(query.getVersionedName())) {
            System.out.println("[SKIP]: " + query.getVersionedName());
            continue;
          }

          List<ValidationError> errors =
              errorMaps.computeIfAbsent(query.getVersionedName(), k1 -> new ArrayList<>());

          String script = null;
          try {
            script = getScript(query);
          } catch (Exception e) {
            errors.add(createValidationError("Parse script occurred error!", e));
          }
          if (isBlank(script)) {
            System.out.println("[INVALID]: " + query.getVersionedName() + " script extract error!");
            continue;
          }
          Pair<DBMS, String> dss = sqlQueryService.resolveDataSourceSchema(query.getQualifier());
          String dsName = dss.right();
          DatabaseType dsType = DatabaseType.ANSI_SQL;
          switch (dss.left()) {
            case ORACLE:
              dsType = DatabaseType.ORACLE;
              break;
            case POSTGRE:
              dsType = DatabaseType.POSTGRESQL;
              break;
            case H2:
              dsType = DatabaseType.H2;
              break;
            case MYSQL:
              dsType = DatabaseType.MYSQL;
              break;
            case SQLSERVER2005:
            case SQLSERVER2008:
            case SQLSERVER2012:
              dsType = DatabaseType.SQLSERVER;
              break;
            default:
              break;
          }
          try (Connection conn = dataSources.resolve(dsName).getConnection()) {
            Validation validation = new Validation(featureConfiguration,
                Arrays.asList(dsType,
                    new JdbcDatabaseMetaDataCapability(conn, NamesLookup.NO_TRANSFORMATION)),
                script);
            List<ValidationError> validationErrors = validation.validate();
            errors.addAll(validationErrors);
            List<String> fieldNames = resolveFiledNames(validation);
            if (isNotEmpty(fieldNames)) {
              queryFieldNames.put(query.getVersionedName(), fieldNames);
            }
            if (isEmpty(errors)) {
              System.out.println("[VALID]: " + query.getVersionedName());
            } else {
              hasErrors = true;
              System.out.println(
                  "[INVALID]: " + query.getVersionedName() + " [" + errors.size() + "] ERRORS");
            }
          }
        }

        // validate fetch query no-sql script field names
        if (includeFetchQueryHandling) {
          validateFetchQueries(queryFieldNames, errorMaps, service::getQuery);
        }
        System.out
            .println("Validation completed" + (hasErrors ? ", output error messages" : EMPTY));
        System.out.println("");
        if (hasErrors) {
          System.err.println("$".repeat(100));
          errorMaps.forEach((k, v) -> {
            if (isNotEmpty(v)) {
              v.forEach(e -> {
                if (isNotEmpty(e.getErrors())) {
                  System.err.println("[QUERY NAME]: " + k);
                  if (isNotBlank(e.getStatements())) {
                    System.err.println("[ERROR SQL]:\n" + e.getStatements());
                  }
                  System.err.println("[ERROR MESSAGE]:");
                  e.getErrors().stream().map(
                      (Function<? super ValidationException, ? extends String>) ValidationException::getMessage)
                      .forEach(System.err::println);
                  System.err.println("*".repeat(100));
                }
              });
            }
          });
        }
      } catch (Exception e) {
        throw new CorantRuntimeException(e);
      }
    }

    protected String cleanFreemarkerTargets(String text) {
      if (isNotBlank(text)) {
        MutableString result = new MutableString(text);
        if (specVarReplacements != null) {
          specVarReplacements.forEach((k, v) -> result.apply(r -> Strings.replace(r, k, v)));
        }
        if (specVarPatternReplacements != null) {
          specVarPatternReplacements.forEach((k, v) -> result.apply(r -> r.replaceAll(k, v)));
        }
        for (String vp : variablePatterns) {
          result.apply(r -> r.replaceAll(vp, defaultVariableReplacement));
        }
        for (String dp : directivePatterns) {
          result.apply(r -> r.replaceAll(dp, defaultDirectiveReplacement));
        }
        return result.get();
      }
      return text;
    }

    protected ValidationError createValidationError(String msg, Throwable t) {
      ValidationError ve = new ValidationError(msg);
      if (t instanceof ValidationException x) {
        ve.addError(x);
      } else if (t != null) {
        ve.addError(new ValidationException(t));
      }
      return ve;
    }

    protected String getScript(Query query) throws TemplateException, IOException {
      String script = query.getScript().getCode();
      String macro = query.getMacroScript();
      String text = script;
      if (includeMacro && isNotBlank(macro)) {
        text = skipDirectiveStacksNecessary(macro + Systems.getLineSeparator() + script);
        try (StringWriter sw = new StringWriter()) {
          text = cleanFreemarkerTargets(text);
          new Template(query.getVersionedName(), text, FreemarkerExecutions.FM_CFG)
              .createProcessingEnvironment(emptyMap(), sw).process();
          text = sw.toString();
          MutableString result = new MutableString(text);
          for (String vp : DEFAULT_VARIABLE_PATTERNS) {
            result.apply(r -> r.replaceAll(vp, defaultVariableReplacement));
          }
          for (String dp : DEFAULT_DIRECTIVE_PATTERNS) {
            result.apply(r -> r.replaceAll(dp, defaultDirectiveReplacement));
          }
          text = result.get();
        }
      } else {
        text = cleanFreemarkerTargets(skipDirectiveStacksNecessary(text));
      }
      return text;
    }

    protected List<String> resolveFiledNames(Validation validation) {
      if (validation.getParsedStatements() != null
          && isNotEmpty(validation.getParsedStatements().getStatements())) {
        for (Statement st : validation.getParsedStatements().getStatements()) {
          if ((st instanceof Select select) && (select.getSelectBody() instanceof PlainSelect ps)) {
            List<String> fieldNames = new ArrayList<>();
            for (SelectItem item : ps.getSelectItems()) {
              if (item instanceof SelectExpressionItem si) {
                if (si.getAlias() != null) {
                  fieldNames.add(trim(si.getAlias().getName()));
                } else {
                  // FIXME use . ??
                  String fn = si.getExpression().toString();
                  int dot = fn.indexOf('.');
                  if (dot != -1 && fn.length() > (dot + 1)) {
                    fieldNames.add(trim(fn.substring(dot + 1)));
                  } else {
                    fieldNames.add(trim(fn));
                  }
                }
              }
            }
            return unmodifiableList(fieldNames);
          }
        }
      }
      return emptyList();
    }

    protected List<Integer> resolveSkipDirectiveStacksLines(String sd, List<String> lines) {
      String usd = trim(sd);
      int s = usd.indexOf(' ');
      String start = usd.substring(0, s);
      String end = "</" + usd.substring(1, s) + ">";
      List<Integer> poses = new ArrayList<>();
      List<Integer> stackPoses = new ArrayList<>();
      int lineNo = 0;
      Stack<String> stack = new Stack<>();
      boolean inStack = false;
      for (String line : lines) {
        String tl = trim(line);
        if (inStack) {
          stackPoses.add(lineNo);
          if (tl.startsWith(start)) {
            stack.push(tl);
          } else if (tl.equals(end)) {
            stack.pop();
            if (stack.isEmpty()) {
              poses.addAll(stackPoses);
              inStack = false;
              stackPoses.clear();
            }
          }
        } else if (tl.equals(usd)) {
          stackPoses.add(lineNo);
          stack.push(tl);
          inStack = true;
          if (tl.endsWith("/>")) {
            stack.clear();
            poses.addAll(stackPoses);
            inStack = false;
            stackPoses.clear();
          }
        }
        lineNo++;
      }
      return poses;
    }

    protected String skipDirectiveStacksNecessary(String string) {
      if (isEmpty(skipDirectiveStacks)) {
        return string;
      }
      List<String> lines = string.lines().toList();
      Set<Integer> skipLns = new LinkedHashSet<>();
      for (String sd : skipDirectiveStacks) {
        skipLns.addAll(resolveSkipDirectiveStacksLines(sd, lines));
      }
      if (skipLns.isEmpty()) {
        return string;
      }
      StringBuilder sb = new StringBuilder();
      String lineSpr = Systems.getLineSeparator();
      StringBuilder re = new StringBuilder();
      int size = lines.size();
      for (int i = 0; i < size; i++) {
        if (!skipLns.contains(i)) {
          sb.append(lines.get(i)).append(lineSpr);
        } else if (printSkipDirectiveStacks) {
          re.append("|- ").append(lines.get(i)).append(lineSpr);
        }
      }
      if (printSkipDirectiveStacks && !re.isEmpty()) {
        System.out.println("[STACK-SKIP]:");
        System.out.println("-".repeat(100));
        System.out.println(re.substring(0, re.length() - lineSpr.length()));
        System.out.println("-".repeat(100));
      }
      return sb.toString();
    }

    protected void validateFetchQueries(Map<String, List<String>> queryFieldNames,
        Map<String, List<ValidationError>> errorMaps, Function<String, Query> mapping) {
      for (Entry<String, List<String>> entry : queryFieldNames.entrySet()) {
        Query query;
        if (isEmpty(entry.getValue())
            || isEmpty((query = mapping.apply(entry.getKey())).getFetchQueries())) {
          continue;
        }
        List<ValidationError> errors =
            errorMaps.computeIfAbsent(entry.getKey(), k1 -> new ArrayList<>());
        for (FetchQuery fq : query.getFetchQueries()) {
          validateFetchQuery(entry.getValue(), errors, fq,
              queryFieldNames.get(fq.getQueryReference().getVersionedName()));
        }
      }
    }

    protected void validateFetchQuery(List<String> fieldNames, List<ValidationError> errors,
        FetchQuery fq, List<String> fetchQueryFieldNames) {
      if (isEmpty(fieldNames) || fq.getQueryReference().getType() != QueryType.SQL) {
        return;
      }

      // check predicate script variables
      String fqn = fq.getQueryReference().getVersionedName();
      if (fq.getPredicateScript().isValid()
          && fq.getPredicateScript().getType() == ScriptType.JSE) {
        List<String> resultParamNames =
            resolveJSEResultVariableNames(fq.getPredicateScript().getCode(), false);
        if (isNotEmpty(resultParamNames)) {
          for (String rpn : resultParamNames) {
            if (!fieldNames.contains(trim(rpn))) {
              errors.add(createValidationError(null, new ValidationException("Fetch query [" + fqn
                  + "] predicate script variable: [@r." + rpn + "] not exists")));
            }
          }
        }
      }

      // check query parameter variables
      fq.getParameters().forEach(fp -> {
        if ((fp.getSource() == FetchQueryParameterSource.R)
            && !fieldNames.contains(fp.getSourceNamePath()[0])) {
          errors.add(createValidationError(null, new ValidationException("Fetch query [" + fqn
              + "] parameter: [" + fp.getSourceNamePath()[0] + "] not exists")));
        }
      });

      // check injection script variables
      if (fq.getInjectionScript().isValid()
          && fq.getInjectionScript().getType() == ScriptType.JSE) {
        String injectCode = fq.getInjectionScript().getCode();
        List<String> parentResultParamNames = resolveJSEResultVariableNames(injectCode, false);
        if (isNotEmpty(parentResultParamNames)) {
          for (String rpn : parentResultParamNames) {
            if (!fieldNames.contains(trim(rpn))) {
              errors.add(createValidationError(null, new ValidationException("Fetch query [" + fqn
                  + "] injection script variable: [@r." + rpn + "] not exists")));
            }
          }
        }
        if (isNotEmpty(fetchQueryFieldNames)) {
          List<String> fetchResultParamNames = resolveJSEResultVariableNames(injectCode, true);
          if (isNotEmpty(fetchResultParamNames)) {
            for (String frpn : fetchResultParamNames) {
              if (!fetchQueryFieldNames.contains(trim(frpn))) {
                errors.add(createValidationError(null, new ValidationException("Fetch query [" + fqn
                    + "] injection script variable: [@fr." + frpn + "] not exists")));
              }
            }
          }
          try {
            Map<Object, Object> map = ObjectMappers.mapReader().readValue(injectCode);
            Map<String, Object> projectionMap =
                getMapMap(map, JsonExpressionScriptProcessor.PROJECTION_KEY);
            if (isNotEmpty(projectionMap)) {
              for (String frpn : projectionMap.keySet()) {
                if (!fetchQueryFieldNames.contains(trim(frpn))) {
                  errors.add(createValidationError(null, new ValidationException("Fetch query ["
                      + fqn + "] injection script projection name: [" + frpn + "] not exists")));
                }
              }
            }
          } catch (JsonProcessingException e) {
            errors.add(createValidationError(null,
                new ValidationException("Fetch query injection script process error!")));
          }
        }
      }
    }
  }
}
