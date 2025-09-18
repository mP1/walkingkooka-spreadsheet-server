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
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContextDelegator;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

final class BasicSpreadsheetEngineHateosResourceHandlerContext implements SpreadsheetEngineHateosResourceHandlerContext,
    HateosResourceHandlerContextDelegator,
    SpreadsheetEngineContextDelegator,
    SpreadsheetProviderDelegator,
    JsonNodeMarshallContextDelegator {

    static BasicSpreadsheetEngineHateosResourceHandlerContext with(final SpreadsheetEngine spreadsheetEngine,
                                                                   final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                                   final SpreadsheetEngineContext engineContext,
                                                                   final SpreadsheetProvider systemSpreadsheetProvider) {
        return new BasicSpreadsheetEngineHateosResourceHandlerContext(
            Objects.requireNonNull(spreadsheetEngine, "spreadsheetEngine"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
            Objects.requireNonNull(engineContext, "engineContext"),
            Objects.requireNonNull(systemSpreadsheetProvider, "systemSpreadsheetProvider")
        );
    }

    private BasicSpreadsheetEngineHateosResourceHandlerContext(final SpreadsheetEngine spreadsheetEngine,
                                                               final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                               final SpreadsheetEngineContext engineContext,
                                                               final SpreadsheetProvider systemSpreadsheetProvider) {
        this.spreadsheetEngine = spreadsheetEngine;
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
        this.engineContext = engineContext;
        this.systemSpreadsheetProvider = systemSpreadsheetProvider;
    }

    @Override
    public SpreadsheetEngine spreadsheetEngine() {
        return this.spreadsheetEngine;
    }

    private final SpreadsheetEngine spreadsheetEngine;

    // 4 methods immediately below are required due to clashes between XXXDelegators

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.engineContext.spreadsheetMetadata()
            .expressionNumberKind();
    }

    @Override
    public MathContext mathContext() {
        return this.spreadsheetMetadata()
            .mathContext();
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
    public BasicSpreadsheetEngineHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final HateosResourceHandlerContext before = this.hateosResourceHandlerContext;
        final HateosResourceHandlerContext after = before.setPreProcessor(processor);
        return before.equals(after) ?
            this :
            new BasicSpreadsheetEngineHateosResourceHandlerContext(
                this.spreadsheetEngine,
                after,
                this.engineContext,
                this.systemSpreadsheetProvider
            );
    }

    // CanConvertDelegator..............................................................................................

    @Override
    public CanConvert canConvert() {
        return this.engineContext;
    }

    // JsonNodeMarshallUnmarshallContext................................................................................

    @Override
    public JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext() {
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
    public Locale locale() {
        return this.engineContext.locale();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext setLocale(final Locale locale) {
        this.engineContext.setLocale(locale);
        return this;
    }

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

    // EnvironmentContext...............................................................................................

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext cloneEnvironment() {
        final SpreadsheetEngineContext engineContext = this.engineContext;
        final SpreadsheetEngineContext clone = engineContext.cloneEnvironment();

        // Recreate only if different cloned EnvironmentContext, cloned environment should be equals
        return engineContext == clone ?
            this :
            new BasicSpreadsheetEngineHateosResourceHandlerContext(
                this.spreadsheetEngine,
                this.hateosResourceHandlerContext,
                clone,
                this.systemSpreadsheetProvider
            );
    }

    @Override
    public <T> SpreadsheetEngineHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                                 final T value) {
        this.engineContext.setEnvironmentValue(
            name,
            value
        );
        return this;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        this.engineContext.removeEnvironmentValue(name);
        return this;
    }

    private final SpreadsheetEngineContext engineContext;

    // SpreadsheetEngineHateosResourceHandlerContext....................................................................

    @Override
    public SpreadsheetProvider systemSpreadsheetProvider() {
        return this.systemSpreadsheetProvider;
    }

    private final SpreadsheetProvider systemSpreadsheetProvider;
}
