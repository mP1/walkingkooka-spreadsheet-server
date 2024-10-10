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

import walkingkooka.convert.provider.ConverterProviderDelegator;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.header.MediaType;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A {@link SpreadsheetHateosResourceHandlerContext} which delegates all methods to the given {@link SpreadsheetHateosResourceHandlerContext},
 * except for the given {@link SpreadsheetMetadata} which will have the updated {@link walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName#VIEWPORT}.
 */
final class SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetHateosResourceHandlerContext implements SpreadsheetHateosResourceHandlerContext,
        ConverterProviderDelegator,
        SpreadsheetEngineContextDelegator,
        SpreadsheetFormatterContextDelegator,
        SpreadsheetProviderDelegator,
        ProviderContextDelegator,
        JsonNodeMarshallUnmarshallContextDelegator {

    static SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetHateosResourceHandlerContext with(final SpreadsheetMetadata metadata,
                                                                                                     final SpreadsheetHateosResourceHandlerContext context) {
        return new SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetHateosResourceHandlerContext(
                metadata,
                context
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetHateosResourceHandlerContext(final SpreadsheetMetadata metadata,
                                                                                                 final SpreadsheetHateosResourceHandlerContext context) {
        this.metadata = metadata;
        this.context = context;
    }

    @Override
    public SpreadsheetMetadata spreadsheetMetadata() {
        return this.metadata;
    }

    private final SpreadsheetMetadata metadata;

    // SpreadsheetHateosResourceHandlerContext.........................................................................

    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_JSON;
    }

    // must be overridden because of clashes between various XXXDelegators

    @Override
    public <T> Optional<T> environmentValue(final EnvironmentValueName<T> name) {
        return this.context.environmentValue(name);
    }

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
    public SpreadsheetSelection resolveLabel(final SpreadsheetLabelName label) {
        return this.context.resolveLabel(label);
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

    // ProviderContext..................................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.context;
    }

    // SpreadsheetHateosResourceHandlerContext..........................................................................

    @Override
    public SpreadsheetProvider systemSpreadsheetProvider() {
        return this.context.systemSpreadsheetProvider();
    }

    private final SpreadsheetHateosResourceHandlerContext context;
}
