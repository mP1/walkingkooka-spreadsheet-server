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

import walkingkooka.convert.ConverterLike;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.email.EmailAddress;
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
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
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
                                                                   final SpreadsheetEngineContext engineContext) {
        return new BasicSpreadsheetEngineHateosResourceHandlerContext(
            Objects.requireNonNull(spreadsheetEngine, "spreadsheetEngine"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
            Objects.requireNonNull(engineContext, "engineContext")
        );
    }

    private BasicSpreadsheetEngineHateosResourceHandlerContext(final SpreadsheetEngine spreadsheetEngine,
                                                               final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                               final SpreadsheetEngineContext engineContext) {
        this.spreadsheetEngine = spreadsheetEngine;
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
        this.engineContext = engineContext;
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

    // ConvertLikeDelegator.............................................................................................

    @Override
    public ConverterLike converterLike() {
        return this.engineContext;
    }

    // JsonNodeMarshallUnmarshallContext................................................................................

    @Override
    public JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext() {
        return this.hateosResourceHandlerContext;
    }

    @Override
    public BasicSpreadsheetEngineHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public BasicSpreadsheetEngineHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setPreProcessor(processor)
        );
    }

    private BasicSpreadsheetEngineHateosResourceHandlerContext setHateosResourceHandlerContext(final HateosResourceHandlerContext context) {
        return this.hateosResourceHandlerContext.equals(context) ?
            this :
            new BasicSpreadsheetEngineHateosResourceHandlerContext(
                this.spreadsheetEngine,
                context,
                this.engineContext
            );
    }

    // HateosResourceHandlerContext.....................................................................................

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    // SpreadsheetEngineContextDelegator................................................................................

    @Override
    public LineEnding lineEnding() {
        return this.engineContext.lineEnding();
    }

    @Override
    public void setLineEnding(final LineEnding lineEnding) {
        this.engineContext.setLineEnding(lineEnding);
    }
    
    @Override
    public Locale locale() {
        return this.engineContext.locale();
    }

    @Override
    public void setLocale(final Locale locale) {
        this.engineContext.setLocale(locale);
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
                clone
            );
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext setEnvironmentContext(final EnvironmentContext environmentContext) {
        final SpreadsheetEngineContext before = this.engineContext;
        final SpreadsheetEngineContext after = before.setEnvironmentContext(environmentContext);

        return before == after ?
            this :
            new BasicSpreadsheetEngineHateosResourceHandlerContext(
                this.spreadsheetEngine,
                this.hateosResourceHandlerContext,
                after
            );
    }

    @Override
    public <T> void setEnvironmentValue(final EnvironmentValueName<T> name,
                                        final T value) {
        this.engineContext.setEnvironmentValue(
            name,
            value
        );
    }

    @Override
    public void removeEnvironmentValue(final EnvironmentValueName<?> name) {
        this.engineContext.removeEnvironmentValue(name);
    }

    @Override
    public void setUser(final Optional<EmailAddress> user) {
        this.engineContext.setUser(user);
    }

    private final SpreadsheetEngineContext engineContext;
}
