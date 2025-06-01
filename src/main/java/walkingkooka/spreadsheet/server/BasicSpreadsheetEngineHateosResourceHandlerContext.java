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

package walkingkooka.spreadsheet.server;

import walkingkooka.convert.CanConvert;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

final class BasicSpreadsheetEngineHateosResourceHandlerContext implements SpreadsheetEngineHateosResourceHandlerContext,
    HateosResourceHandlerContextDelegator,
    SpreadsheetEngineContextDelegator,
    SpreadsheetFormatterContextDelegator,
    SpreadsheetProviderDelegator {

    static BasicSpreadsheetEngineHateosResourceHandlerContext with(final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                                   final SpreadsheetEngineContext engineContext,
                                                                   final SpreadsheetFormatterContext formatterContext,
                                                                   final SpreadsheetProvider systemSpreadsheetProvider) {
        return new BasicSpreadsheetEngineHateosResourceHandlerContext(
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
            Objects.requireNonNull(engineContext, "engineContext"),
            Objects.requireNonNull(formatterContext, "formatterContext"),
            Objects.requireNonNull(systemSpreadsheetProvider, "systemSpreadsheetProvider")
        );
    }

    private BasicSpreadsheetEngineHateosResourceHandlerContext(final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                               final SpreadsheetEngineContext engineContext,
                                                               final SpreadsheetFormatterContext formatterContext,
                                                               final SpreadsheetProvider systemSpreadsheetProvider) {
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
        this.engineContext = engineContext;
        this.formatterContext = formatterContext;
        this.systemSpreadsheetProvider = systemSpreadsheetProvider;
    }

    // 4 methods immediately below are required due to clashes between XXXDelegators

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.hateosResourceHandlerContext.expressionNumberKind();
    }

    @Override
    public MathContext mathContext() {
        return this.formatterContext.mathContext();
    }

    @Override
    public LocalDateTime now() {
        return this.engineContext.now();
    }

    @Override
    public Optional<SpreadsheetSelection> resolveLabel(final SpreadsheetLabelName labelName) {
        return this.engineContext.resolveLabel(labelName);
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final HateosResourceHandlerContext before = this.hateosResourceHandlerContext;
        final HateosResourceHandlerContext after = before.setPreProcessor(processor);
        return before.equals(after) ?
            this :
            new BasicSpreadsheetEngineHateosResourceHandlerContext(
                after,
                this.engineContext,
                this.formatterContext.setPreProcessor(processor),
                this.systemSpreadsheetProvider
            );
    }

    // CanConvertDelegator..............................................................................................

    @Override
    public CanConvert canConvert() {
        return this.formatterContext; // engineContext will delegate to ProviderContext
    }

    // JsonNodeXXXContext...............................................................................................

    @Override
    public JsonNodeContext jsonNodeContext() {
        return this.hateosResourceHandlerContext;
    }

    @Override
    public JsonNodeMarshallContext jsonNodeMarshallContext() {
        return this.hateosResourceHandlerContext;
    }

    @Override
    public JsonNodeUnmarshallContext jsonNodeUnmarshallContext() {
        return this.hateosResourceHandlerContext;
    }

    // HateosResourceHandlerContext.....................................................................................

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    // SpreadsheetEngineContextDelegator................................................................................

    @Override
    public SpreadsheetMetadata spreadsheetMetadata() {
        return this.engineContext.spreadsheetMetadata();
    }

    @Override
    public SpreadsheetEngineContext spreadsheetEngineContext() {
        return this.engineContext;
    }

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.engineContext;
    }

    // SpreadsheetProvider..............................................................................................

    @Override
    public SpreadsheetProvider spreadsheetProvider() {
        return this.engineContext;
    }

    private final SpreadsheetEngineContext engineContext;

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.formatterContext;
    }

    private final SpreadsheetFormatterContext formatterContext;

    // SpreadsheetEngineHateosResourceHandlerContext..........................................................................

    @Override
    public SpreadsheetProvider systemSpreadsheetProvider() {
        return this.systemSpreadsheetProvider;
    }

    private final SpreadsheetProvider systemSpreadsheetProvider;
}
