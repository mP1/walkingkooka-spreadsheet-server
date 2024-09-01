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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.convert.provider.ConverterProvider;
import walkingkooka.convert.provider.ConverterProviderDelegator;
import walkingkooka.net.header.MediaType;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProviderDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterProvider;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterProviderDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProviderDelegator;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProviderDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.text.TextNode;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A {@link SpreadsheetEngineHateosResourceHandlerContext} which delegates all methods to the given {@link SpreadsheetEngineHateosResourceHandlerContext},
 * except for the given {@link SpreadsheetMetadata} which will have the updated {@link walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName#VIEWPORT}.
 */
final class SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetEngineHateosResourceHandlerContext implements SpreadsheetEngineHateosResourceHandlerContext,
        ConverterProviderDelegator,
        SpreadsheetComparatorProviderDelegator,
        ExpressionFunctionProviderDelegator,
        SpreadsheetFormatterContextDelegator,
        SpreadsheetFormatterProviderDelegator,
        SpreadsheetImporterProviderDelegator,
        SpreadsheetParserProviderDelegator,
        ProviderContextDelegator,
        JsonNodeMarshallUnmarshallContextDelegator {

    static SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetEngineHateosResourceHandlerContext with(final SpreadsheetMetadata metadata,
                                                                                                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        return new SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetEngineHateosResourceHandlerContext(
                metadata,
                context
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetEngineHateosResourceHandlerContext(final SpreadsheetMetadata metadata,
                                                                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        this.metadata = metadata;
        this.context = context;
    }

    // SpreadsheetEngineHateosResourceHandlerContext....................................................................

    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_JSON;
    }

    // must be overridden because of clashes between various XXXDelegators

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.context.expressionNumberKind();
    }

    @Override
    public MathContext mathContext() {
        return this.context.mathContext();
    }

    // JsonNodeMarshallContext..........................................................................................

    @Override
    public JsonNodeMarshallContext jsonNodeMarshallContext() {
        return this.context;
    }

    // JsonNodeUnmarshallContext........................................................................................

    @Override
    public JsonNodeUnmarshallContext jsonNodeUnmarshallContext() {
        return this.context;
    }

    // SpreadsheetEngineContext.........................................................................................

    @Override
    public SpreadsheetParserToken parseFormula(final TextCursor textCursor) {
        return this.context.parseFormula(textCursor);
    }

    @Override
    public Optional<Expression> toExpression(final SpreadsheetParserToken spreadsheetParserToken) {
        return this.context.toExpression(spreadsheetParserToken);
    }

    @Override
    public Object evaluate(final Expression expression,
                           final Optional<SpreadsheetCell> cell) {
        return this.context.evaluate(
                expression,
                cell
        );
    }

    @Override
    public Optional<TextNode> formatValue(final Object value,
                                          final SpreadsheetFormatter spreadsheetFormatter) {
        return this.context.formatValue(
                value,
                spreadsheetFormatter
        );
    }

    @Override
    public SpreadsheetCell formatValueAndStyle(final SpreadsheetCell cell,
                                               final Optional<SpreadsheetFormatter> formatter) {
        return this.context.formatValueAndStyle(
                cell,
                formatter
        );
    }

    @Override
    public SpreadsheetStoreRepository storeRepository() {
        return this.context.storeRepository();
    }

    @Override
    public LocalDateTime now() {
        return this.context.now();
    }

    @Override
    public SpreadsheetMetadata spreadsheetMetadata() {
        return this.metadata;
    }

    private final SpreadsheetMetadata metadata;

    @Override
    public SpreadsheetSelection resolveLabel(final SpreadsheetLabelName spreadsheetLabelName) {
        return this.context.resolveLabel(spreadsheetLabelName);
    }

    @Override
    public boolean isPure(final ExpressionFunctionName name) {
        return this.context.isPure(name);
    }

    // ConverterProvider................................................................................................

    @Override
    public ConverterProvider converterProvider() {
        return this.context;
    }

    // SpreadsheetComparatorProvider....................................................................................

    @Override
    public SpreadsheetComparatorProvider spreadsheetComparatorProvider() {
        return this.context;
    }

    // ExpressionFunctionProvider.......................................................................................

    @Override
    public ExpressionFunctionProvider expressionFunctionProvider() {
        return this.context;
    }

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.context;
    }

    // SpreadsheetImporterProvider......................................................................................

    @Override
    public SpreadsheetImporterProvider spreadsheetImporterProvider() {
        return this.context;
    }

    // SpreadsheetParserProvider........................................................................................

    @Override
    public SpreadsheetParserProvider spreadsheetParserProvider() {
        return this.context;
    }

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.context;
    }

    // ProviderContext..................................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.context;
    }

    private final SpreadsheetEngineHateosResourceHandlerContext context;
}
