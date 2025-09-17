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

package walkingkooka.spreadsheet.server.parser;

import walkingkooka.InvalidCharacterException;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContext;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserProviderDelegator;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.parser.Parser;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
import java.util.Objects;

/**
 * A delegating {@link SpreadsheetParserSelectorEditContext} that uses a {@link SpreadsheetParserContext} and
 * {@link SpreadsheetParserProvider} etc.
 */
final class BasicSpreadsheetParserSelectorEditContext implements SpreadsheetParserSelectorEditContext,
    SpreadsheetFormatterContextDelegator,
    SpreadsheetFormatterProviderDelegator,
    SpreadsheetParserProviderDelegator {

    static BasicSpreadsheetParserSelectorEditContext with(final SpreadsheetParserProvider spreadsheetParserProvider,
                                                          final SpreadsheetParserContext spreadsheetParserContext,
                                                          final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                          final SpreadsheetFormatterProvider spreadsheetFormatterProvider,
                                                          final ProviderContext providerContext) {
        return new BasicSpreadsheetParserSelectorEditContext(
            Objects.requireNonNull(spreadsheetParserProvider, "spreadsheetParserProvider"),
            Objects.requireNonNull(spreadsheetParserContext, "spreadsheetParserContext"),
            Objects.requireNonNull(spreadsheetFormatterContext, "spreadsheetFormatterContext"),
            Objects.requireNonNull(spreadsheetFormatterProvider, "spreadsheetFormatterProvider"),
            Objects.requireNonNull(providerContext, "providerContext")
        );
    }

    private BasicSpreadsheetParserSelectorEditContext(final SpreadsheetParserProvider spreadsheetParserProvider,
                                                      final SpreadsheetParserContext spreadsheetParserContext,
                                                      final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                      final SpreadsheetFormatterProvider spreadsheetFormatterProvider,
                                                      final ProviderContext providerContext) {
        this.spreadsheetParserProvider = spreadsheetParserProvider;
        this.spreadsheetParserContext = spreadsheetParserContext;
        this.spreadsheetFormatterContext = spreadsheetFormatterContext;
        this.spreadsheetFormatterProvider = spreadsheetFormatterProvider;
        this.providerContext = providerContext;
    }

    @Override
    public SpreadsheetParserSelectorEditContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final SpreadsheetFormatterContext before = this.spreadsheetFormatterContext;
        final SpreadsheetFormatterContext after = before.setPreProcessor(processor);
        return before.equals(after) ?
            this :
            new BasicSpreadsheetParserSelectorEditContext(
                this.spreadsheetParserProvider,
                this.spreadsheetParserContext,
                after,
                this.spreadsheetFormatterProvider,
                this.providerContext
            );
    }

    // SpreadsheetParserProvider........................................................................................

    @Override
    public SpreadsheetParserProvider spreadsheetParserProvider() {
        return this.spreadsheetParserProvider;
    }

    private final SpreadsheetParserProvider spreadsheetParserProvider;

    // SpreadsheetParserContext.........................................................................................

    @Override
    public boolean canNumbersHaveGroupSeparator() {
        return this.spreadsheetParserContext.canNumbersHaveGroupSeparator();
    }

    @Override
    public InvalidCharacterException invalidCharacterException(final Parser<?> parser,
                                                               final TextCursor cursor) {
        return this.spreadsheetParserContext.invalidCharacterException(
            parser,
            cursor
        );
    }

    @Override
    public char valueSeparator() {
        return this.spreadsheetParserContext.valueSeparator();
    }

    private final SpreadsheetParserContext spreadsheetParserContext;

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.spreadsheetFormatterContext;
    }

    private final SpreadsheetFormatterContext spreadsheetFormatterContext;

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.spreadsheetFormatterProvider;
    }

    private final SpreadsheetFormatterProvider spreadsheetFormatterProvider;

    @Override
    public SpreadsheetParserSelectorEditContext setLocale(final Locale locale) {
        this.providerContext.setLocale(locale);
        return this;
    }

    // HasProviderContext...............................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    private final ProviderContext providerContext;

    @Override
    public String toString() {
        return this.spreadsheetFormatterContext + " " + this.spreadsheetFormatterProvider;
    }
}
