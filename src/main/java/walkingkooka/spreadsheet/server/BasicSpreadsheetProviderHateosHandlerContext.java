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

import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.net.http.server.hateos.HateosHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
import java.util.Objects;

final class BasicSpreadsheetProviderHateosHandlerContext implements SpreadsheetProviderHateosHandlerContext,
    ProviderContextDelegator,
    HateosHandlerContextDelegator {

    static BasicSpreadsheetProviderHateosHandlerContext with(final SpreadsheetProvider spreadsheetProvider,
                                                             final ProviderContext providerContext,
                                                             final HateosHandlerContext hateosHandlerContext) {
        return new BasicSpreadsheetProviderHateosHandlerContext(
            Objects.requireNonNull(spreadsheetProvider, "spreadsheetProvider"),
            Objects.requireNonNull(providerContext, "providerContext"),
            Objects.requireNonNull(hateosHandlerContext, "hateosHandlerContext")
        );
    }

    private BasicSpreadsheetProviderHateosHandlerContext(final SpreadsheetProvider spreadsheetProvider,
                                                         final ProviderContext providerContext,
                                                         final HateosHandlerContext hateosHandlerContext) {
        super();

        this.spreadsheetProvider = spreadsheetProvider;
        this.providerContext = providerContext;
        this.hateosHandlerContext = hateosHandlerContext;
    }

    @Override
    public SpreadsheetProvider spreadsheetProvider() {
        return this.spreadsheetProvider;
    }

    private final SpreadsheetProvider spreadsheetProvider;

    // EnvironmentContext...............................................................................................

    @Override
    public SpreadsheetProviderHateosHandlerContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetProviderHateosHandlerContext setEnvironmentContext(final EnvironmentContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void setEnvironmentValue(final EnvironmentValueName<T> name,
                                        final T value) {
        this.providerContext.setEnvironmentValue(
            name,
            value
        );
    }

    @Override
    public Indentation indentation() {
        return this.hateosHandlerContext.indentation();
    }

    @Override
    public LineEnding lineEnding() {
        return this.hateosHandlerContext.lineEnding();
    }

    @Override
    public void setLineEnding(final LineEnding lineEnding) {
        this.providerContext.setLineEnding(lineEnding);
    }

    @Override
    public void setLocale(final Locale locale) {
        this.providerContext.setLocale(locale);
    }

    // ProviderContextDelegator.........................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    private final ProviderContext providerContext;

    // HateosHandlerContextDelegator............................................................................

    @Override
    public HateosHandlerContext hateosHandlerContext() {
        return this.hateosHandlerContext;
    }

    private final HateosHandlerContext hateosHandlerContext;

    @Override
    public SpreadsheetProviderHateosHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public SpreadsheetProviderHateosHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setPreProcessor(processor)
        );
    }

    private BasicSpreadsheetProviderHateosHandlerContext setHateosHandlerContext(final HateosHandlerContext context) {
        return this.hateosHandlerContext.equals(context) ?
            this :
            new BasicSpreadsheetProviderHateosHandlerContext(
                this.spreadsheetProvider,
                this.providerContext,
                context
            );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.spreadsheetProvider + " " + this.providerContext + " " + this.hateosHandlerContext;
    }
}
