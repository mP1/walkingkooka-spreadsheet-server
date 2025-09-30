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
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
import java.util.Objects;

/**
 * A delegating {@link SpreadsheetFormatterSelectorEditContext} that uses a {@link SpreadsheetFormatterContext} and
 * {@link SpreadsheetFormatterProvider}.
 */
final class BasicSpreadsheetFormatterSelectorEditContext implements SpreadsheetFormatterSelectorEditContext,
    SpreadsheetFormatterContextDelegator,
    SpreadsheetFormatterProviderDelegator {

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

    // LocaleContext....................................................................................................

    @Override
    public Locale locale() {
        return this.localeContext()
            .locale();
    }

    @Override
    public SpreadsheetFormatterSelectorEditContext setLocale(final Locale locale) {
        this.localeContext()
            .setLocale(locale);
        return this;
    }

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterSelectorEditContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setSpreadsheetFormatterContext(
            this.spreadsheetFormatterContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public SpreadsheetFormatterSelectorEditContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setSpreadsheetFormatterContext(
            this.spreadsheetFormatterContext.setPreProcessor(processor)
        );
    }

    private SpreadsheetFormatterSelectorEditContext setSpreadsheetFormatterContext(final SpreadsheetFormatterContext context) {
        return this.spreadsheetFormatterContext.equals(context) ?
            this :
            with(
                context,
                this.spreadsheetFormatterProvider,
                this.providerContext
            );
    }

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

    // HasProviderContext...............................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    private final ProviderContext providerContext;

    @Override
    public String toString() {
        return this.spreadsheetFormatterContext + " " + this.spreadsheetFormatterProvider + " " + this.providerContext;
    }
}
