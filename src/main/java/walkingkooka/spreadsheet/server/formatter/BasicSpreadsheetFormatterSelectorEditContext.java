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

package walkingkooka.spreadsheet.server.formatter;

import walkingkooka.convert.CanConvert;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A delegating {@link SpreadsheetFormatterSelectorEditContext} that uses a {@link SpreadsheetFormatterContext} and
 * {@link SpreadsheetFormatterProvider}.
 */
final class BasicSpreadsheetFormatterSelectorEditContext implements SpreadsheetFormatterSelectorEditContext,
    SpreadsheetFormatterContextDelegator,
    SpreadsheetFormatterProviderDelegator,
    ProviderContextDelegator {

    static BasicSpreadsheetFormatterSelectorEditContext with(final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                             final SpreadsheetFormatterProvider spreadsheetFormatterProvider,
                                                             final ProviderContext providerContext) {
        return new BasicSpreadsheetFormatterSelectorEditContext(
            Objects.requireNonNull(spreadsheetFormatterContext, "spreadsheetFormatterContext"),
            Objects.requireNonNull(spreadsheetFormatterProvider, "spreadsheetFormatterProvider"),
            Objects.requireNonNull(providerContext, "providerContext")
        );
    }

    private BasicSpreadsheetFormatterSelectorEditContext(final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                         final SpreadsheetFormatterProvider spreadsheetFormatterProvider,
                                                         final ProviderContext providerContext) {
        this.spreadsheetFormatterContext = spreadsheetFormatterContext;
        this.spreadsheetFormatterProvider = spreadsheetFormatterProvider;
        this.providerContext = providerContext;
    }

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.spreadsheetFormatterContext;
    }

    @Override
    public CanConvert canConvert() {
        return this.spreadsheetFormatterContext;
    }

    private final SpreadsheetFormatterContext spreadsheetFormatterContext;

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.spreadsheetFormatterProvider;
    }

    private final SpreadsheetFormatterProvider spreadsheetFormatterProvider;

    // ProviderContext..................................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    @Override
    public LocalDateTime now() {
        return this.providerContext.now();
    }

    private final ProviderContext providerContext;

    @Override
    public String toString() {
        return this.spreadsheetFormatterContext + " " + this.spreadsheetFormatterProvider + " " + this.providerContext;
    }
}
