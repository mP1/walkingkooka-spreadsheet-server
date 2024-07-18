/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.server.engine;

import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContext;
import walkingkooka.convert.provider.ConverterInfo;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.convert.provider.ConverterSelector;
import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.compare.SpreadsheetComparator;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponent;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonString;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;
import walkingkooka.tree.text.TextNode;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

final class BasicSpreadsheetEngineHateosResourceHandlerContext implements SpreadsheetEngineHateosResourceHandlerContext {

    static BasicSpreadsheetEngineHateosResourceHandlerContext with(final JsonNodeMarshallContext marshallContext,
                                                                   final JsonNodeUnmarshallContext unmarshallContext,
                                                                   final SpreadsheetEngineContext engineContext) {
        return new BasicSpreadsheetEngineHateosResourceHandlerContext(
                Objects.requireNonNull(marshallContext, "marshallContext"),
                Objects.requireNonNull(unmarshallContext, "unmarshallContext"),
                Objects.requireNonNull(engineContext, "engineContext")
        );
    }

    private BasicSpreadsheetEngineHateosResourceHandlerContext(final JsonNodeMarshallContext marshallContext,
                                                               final JsonNodeUnmarshallContext unmarshallContext,
                                                               final SpreadsheetEngineContext engineContext) {
        this.marshallContext = marshallContext;
        this.unmarshallContext = unmarshallContext;
        this.engineContext = engineContext;
    }

    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_JSON;
    }

    // JsonNodeMarshallContext..........................................................................................

    @Override
    public JsonNodeMarshallContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor context) {
        return this.marshallContext.setObjectPostProcessor(context);
    }

    @Override
    public JsonNode marshall(final Object value) {
        return this.marshallContext.marshall(value);
    }

    @Override
    public JsonNode marshallEnumSet(final Set<? extends Enum<?>> set) {
        return this.marshallContext.marshallEnumSet(set);
    }

    @Override
    public JsonNode marshallWithType(final Object value) {
        return this.marshallContext.marshallWithType(value);
    }

    @Override
    public JsonNode marshallCollection(final Collection<?> collection) {
        return this.marshallContext.marshallCollection(collection);
    }

    @Override
    public JsonNode marshallMap(final Map<?, ?> map) {
        return this.marshallContext.marshallMap(map);
    }

    @Override
    public JsonNode marshallWithTypeCollection(final Collection<?> collection) {
        return this.marshallContext.marshallWithTypeCollection(collection);
    }

    @Override
    public JsonNode marshallWithTypeMap(final Map<?, ?> map) {
        return this.marshallContext.marshallWithTypeMap(map);
    }

    private final JsonNodeMarshallContext marshallContext;

    // JsonNodeUnmarshallContext........................................................................................

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.unmarshallContext.expressionNumberKind();
    }

    @Override
    public MathContext mathContext() {
        return this.unmarshallContext.mathContext();
    }

    @Override
    public JsonNodeUnmarshallContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.unmarshallContext.setPreProcessor(processor);
    }

    @Override
    public <T> T unmarshall(final JsonNode json,
                            final Class<T> type) {
        return this.unmarshallContext.unmarshall(
                json,
                type
        );
    }

    @Override
    public <T extends Enum<T>> Set<T> unmarshallEnumSet(final JsonNode json,
                                                        final Class<T> type,
                                                        final Function<String, T> nameToEnum) {
        return this.unmarshallContext.unmarshallEnumSet(
                json,
                type,
                nameToEnum
        );
    }

    @Override
    public <T> List<T> unmarshallList(final JsonNode json,
                                      final Class<T> type) {
        return this.unmarshallContext.unmarshallList(
                json,
                type
        );
    }

    @Override
    public <T> Set<T> unmarshallSet(final JsonNode json,
                                    final Class<T> type) {
        return this.unmarshallContext.unmarshallSet(
                json,
                type
        );
    }

    @Override
    public <K, V> Map<K, V> unmarshallMap(final JsonNode json,
                                          final Class<K> keyType,
                                          final Class<V> valueType) {
        return this.unmarshallContext.unmarshallMap(
                json,
                keyType,
                valueType
        );
    }

    @Override
    public <T> T unmarshallWithType(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithType(json);
    }

    @Override
    public <T> List<T> unmarshallWithTypeList(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithTypeList(json);
    }

    @Override
    public <T> Set<T> unmarshallWithTypeSet(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithTypeSet(json);
    }

    @Override
    public <K, V> Map<K, V> unmarshallWithTypeMap(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithTypeMap(json);
    }

    @Override
    public Optional<Class<?>> registeredType(final JsonString string) {
        return this.unmarshallContext.registeredType(string);
    }

    @Override
    public Optional<JsonString> typeName(final Class<?> type) {
        return this.unmarshallContext.typeName(type);
    }

    private final JsonNodeUnmarshallContext unmarshallContext;

    // SpreadsheetEngineContext.........................................................................................

    @Override
    public SpreadsheetParserToken parseFormula(final TextCursor textCursor) {
        return this.engineContext.parseFormula(textCursor);
    }

    @Override
    public Optional<Expression> toExpression(final SpreadsheetParserToken spreadsheetParserToken) {
        return this.engineContext.toExpression(spreadsheetParserToken);
    }

    @Override
    public Object evaluate(final Expression expression,
                           final Optional<SpreadsheetCell> cell) {
        return this.engineContext.evaluate(
                expression,
                cell
        );
    }

    @Override
    public Optional<TextNode> formatValue(final Object value,
                                          final SpreadsheetFormatter spreadsheetFormatter) {
        return this.engineContext.formatValue(
                value,
                spreadsheetFormatter
        );
    }

    @Override
    public SpreadsheetCell formatValueAndStyle(final SpreadsheetCell cell,
                                               final Optional<SpreadsheetFormatter> formatter) {
        return this.engineContext.formatValueAndStyle(
                cell,
                formatter
        );
    }

    @Override
    public SpreadsheetStoreRepository storeRepository() {
        return this.engineContext.storeRepository();
    }

    @Override
    public LocalDateTime now() {
        return this.engineContext.now();
    }

    @Override
    public SpreadsheetMetadata spreadsheetMetadata() {
        return this.engineContext.spreadsheetMetadata();
    }

    @Override
    public SpreadsheetSelection resolveLabel(final SpreadsheetLabelName spreadsheetLabelName) {
        return this.engineContext.resolveLabel(spreadsheetLabelName);
    }

    @Override
    public boolean isPure(final FunctionExpressionName functionExpressionName) {
        return this.engineContext.isPure(functionExpressionName);
    }

    @Override
    public <C extends ConverterContext> Converter<C> converter(final ConverterSelector selector) {
        return this.engineContext.converter(selector);
    }

    @Override
    public <C extends ConverterContext> Converter<C> converter(final ConverterName converterName,
                                                               final List<?> values) {
        return this.engineContext.converter(
                converterName,
                values
        );
    }

    @Override
    public Set<ConverterInfo> converterInfos() {
        return this.engineContext.converterInfos();
    }

    @Override
    public SpreadsheetComparator<?> spreadsheetComparator(final SpreadsheetComparatorName spreadsheetComparatorName) {
        return this.engineContext.spreadsheetComparator(spreadsheetComparatorName);
    }

    @Override
    public Set<SpreadsheetComparatorInfo> spreadsheetComparatorInfos() {
        return this.engineContext.spreadsheetComparatorInfos();
    }

    @Override
    public Set<ExpressionFunctionInfo> expressionFunctionInfos() {
        return this.engineContext.expressionFunctionInfos();
    }

    @Override
    public ExpressionFunction<?, ExpressionEvaluationContext> expressionFunction(final FunctionExpressionName functionExpressionName) {
        return this.engineContext.expressionFunction(functionExpressionName);
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterName name,
                                                     final List<?> values) {
        return this.engineContext.spreadsheetFormatter(
                name,
                values
        );
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector spreadsheetFormatterSelector) {
        return this.engineContext.spreadsheetFormatter(spreadsheetFormatterSelector);
    }

    @Override
    public Optional<SpreadsheetFormatterSelectorTextComponent> spreadsheetFormatterNextTextComponent(final SpreadsheetFormatterSelector selector) {
        return this.engineContext.spreadsheetFormatterNextTextComponent(selector);
    }

    @Override
    public Set<SpreadsheetFormatterInfo> spreadsheetFormatterInfos() {
        return this.engineContext.spreadsheetFormatterInfos();
    }

    @Override
    public SpreadsheetParser spreadsheetParser(final SpreadsheetParserSelector spreadsheetParserSelector) {
        return this.engineContext.spreadsheetParser(spreadsheetParserSelector);
    }

    @Override
    public SpreadsheetParser spreadsheetParser(final SpreadsheetParserName name,
                                               final List<?> values) {
        return this.engineContext.spreadsheetParser(
                name,
                values
        );
    }

    @Override
    public Set<SpreadsheetParserInfo> spreadsheetParserInfos() {
        return this.engineContext.spreadsheetParserInfos();
    }

    private final SpreadsheetEngineContext engineContext;
}
