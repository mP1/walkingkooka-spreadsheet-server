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

import walkingkooka.convert.CanConvert;
import walkingkooka.convert.provider.ConverterProviderDelegator;
import walkingkooka.net.header.MediaType;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * A {@link SpreadsheetEngineHateosResourceHandlerContext} which delegates all methods to the given {@link SpreadsheetEngineHateosResourceHandlerContext},
 * except for the given {@link SpreadsheetMetadata} which will have the updated {@link walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName#VIEWPORT}.
 */
final class SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetEngineHateosResourceHandlerContext implements SpreadsheetEngineHateosResourceHandlerContext,
    ConverterProviderDelegator,
    SpreadsheetEngineContextDelegator,
    SpreadsheetFormatterContextDelegator {

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

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final SpreadsheetEngineHateosResourceHandlerContext before = this.context;
        final SpreadsheetEngineHateosResourceHandlerContext after = before.setPreProcessor(processor);
        return before.equals(after) ?
            this :
            new SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetEngineHateosResourceHandlerContext(
                this.metadata,
                after
            );
    }

    @Override
    public SpreadsheetMetadata spreadsheetMetadata() {
        return this.metadata;
    }

    private final SpreadsheetMetadata metadata;

    // SpreadsheetEngineHateosResourceHandlerContext.........................................................................

    @Override
    public CanConvert canConvert() {
        return this.context;
    }

    @Override
    public MediaType contentType() {
        return SpreadsheetServerMediaTypes.CONTENT_TYPE;
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

    @Override
    public LocalDateTime now() {
        return this.context.now();
    }

    @Override
    public Optional<SpreadsheetSelection> resolveLabel(final SpreadsheetLabelName label) {
        return this.context.resolveLabel(label);
    }

    // SpreadsheetEngineContext.........................................................................................

    @Override
    public Locale locale() {
        return this.context.locale();
    }

    @Override
    public SpreadsheetEngineContext spreadsheetEngineContext() {
        return this.context;
    }

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.context;
    }

    // SpreadsheetProvider..............................................................................................

    @Override
    public SpreadsheetProvider spreadsheetProvider() {
        return this.context;
    }

    // SpreadsheetEngineHateosResourceHandlerContext..........................................................................

    @Override
    public SpreadsheetProvider systemSpreadsheetProvider() {
        return this.context.systemSpreadsheetProvider();
    }

    private final SpreadsheetEngineHateosResourceHandlerContext context;
}
