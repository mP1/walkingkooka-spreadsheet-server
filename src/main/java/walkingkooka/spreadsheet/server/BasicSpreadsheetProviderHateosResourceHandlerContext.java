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

import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Objects;

final class BasicSpreadsheetProviderHateosResourceHandlerContext implements SpreadsheetProviderHateosResourceHandlerContext,
    ProviderContextDelegator,
    HateosResourceHandlerContextDelegator {

    static BasicSpreadsheetProviderHateosResourceHandlerContext with(final SpreadsheetProvider spreadsheetProvider,
                                                                     final ProviderContext providerContext,
                                                                     final HateosResourceHandlerContext hateosResourceHandlerContext) {
        return new BasicSpreadsheetProviderHateosResourceHandlerContext(
            Objects.requireNonNull(spreadsheetProvider, "spreadsheetProvider"),
            Objects.requireNonNull(providerContext, "providerContext"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext")
        );
    }

    private BasicSpreadsheetProviderHateosResourceHandlerContext(final SpreadsheetProvider spreadsheetProvider,
                                                                 final ProviderContext providerContext,
                                                                 final HateosResourceHandlerContext hateosResourceHandlerContext) {
        super();

        this.spreadsheetProvider = spreadsheetProvider;
        this.providerContext = providerContext;
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
    }

    @Override
    public SpreadsheetProvider spreadsheetProvider() {
        return this.spreadsheetProvider;
    }

    private final SpreadsheetProvider spreadsheetProvider;

    // EnvironmentContext...............................................................................................

    @Override
    public <T> SpreadsheetProviderHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                                   final T value) {
        this.providerContext.setEnvironmentValue(
            name,
            value
        );
        return this;
    }
    // ProviderContextDelegator.........................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    private final ProviderContext providerContext;

    // HateosResourceHandlerContextDelegator............................................................................

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    @Override
    public SpreadsheetProviderHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        HateosResourceHandlerContext before = this.hateosResourceHandlerContext;
        HateosResourceHandlerContext after = before.setPreProcessor(processor);
        return before.equals(after) ?
            this :
            new BasicSpreadsheetProviderHateosResourceHandlerContext(
                this.spreadsheetProvider,
                this.providerContext,
                after
            );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.spreadsheetProvider + " " + this.providerContext + " " + this.hateosResourceHandlerContext;
    }
}
