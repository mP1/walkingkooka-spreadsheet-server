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
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProviderDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProviderDelegator;
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
import walkingkooka.tree.json.JsonNodeMarshallUnmarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.text.TextNode;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class BasicSpreadsheetEngineHateosResourceHandlerContext implements SpreadsheetEngineHateosResourceHandlerContext,
        SpreadsheetComparatorProviderDelegator,
        SpreadsheetFormatterContextDelegator,
        SpreadsheetFormatterProviderDelegator,
        SpreadsheetParserProviderDelegator,
        JsonNodeMarshallUnmarshallContextDelegator {

    static BasicSpreadsheetEngineHateosResourceHandlerContext with(final JsonNodeMarshallContext marshallContext,
                                                                   final JsonNodeUnmarshallContext unmarshallContext,
                                                                   final SpreadsheetEngineContext engineContext,
                                                                   final SpreadsheetFormatterContext formatterContext) {
        return new BasicSpreadsheetEngineHateosResourceHandlerContext(
                Objects.requireNonNull(marshallContext, "marshallContext"),
                Objects.requireNonNull(unmarshallContext, "unmarshallContext"),
                Objects.requireNonNull(engineContext, "engineContext"),
                Objects.requireNonNull(formatterContext, "formatterContext")
        );
    }

    private BasicSpreadsheetEngineHateosResourceHandlerContext(final JsonNodeMarshallContext marshallContext,
                                                               final JsonNodeUnmarshallContext unmarshallContext,
                                                               final SpreadsheetEngineContext engineContext,
                                                               final SpreadsheetFormatterContext formatterContext) {
        this.marshallContext = marshallContext;
        this.unmarshallContext = unmarshallContext;
        this.engineContext = engineContext;
        this.formatterContext = formatterContext;
    }

    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.formatterContext.expressionNumberKind();
    }

    @Override
    public MathContext mathContext() {
        return this.formatterContext.mathContext();
    }

    // JsonNodeMarshallContext..........................................................................................

    @Override
    public JsonNodeMarshallContext jsonNodeMarshallContext() {
        return this.marshallContext;
    }

    private final JsonNodeMarshallContext marshallContext;

    // JsonNodeUnmarshallContext........................................................................................

    @Override
    public JsonNodeUnmarshallContext jsonNodeUnmarshallContext() {
        return this.unmarshallContext;
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
    public SpreadsheetComparatorProvider spreadsheetComparatorProvider() {
        return this.engineContext;
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
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.engineContext;
    }

    // SpreadsheetParserProvider........................................................................................

    @Override
    public SpreadsheetParserProvider spreadsheetParserProvider() {
        return this.engineContext;
    }

    private final SpreadsheetEngineContext engineContext;

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.formatterContext;
    }

    private final SpreadsheetFormatterContext formatterContext;
}
