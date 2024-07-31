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

import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContext;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProviderDelegator;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterSelectorEditContext;

import java.util.Objects;

/**
 * A delegating {@link SpreadsheetFormatterSelectorEditContext} that uses a {@link SpreadsheetFormatterContext} and
 * {@link SpreadsheetFormatterProvider}.
 */
final class BasicSpreadsheetParserSelectorEditContext implements SpreadsheetParserSelectorEditContext,
        SpreadsheetFormatterContextDelegator,
        SpreadsheetFormatterProviderDelegator,
        SpreadsheetParserProviderDelegator {

    static BasicSpreadsheetParserSelectorEditContext with(final SpreadsheetParserProvider spreadsheetParserProvider,
                                                          final SpreadsheetParserContext spreadsheetParserContext,
                                                          final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                          final SpreadsheetFormatterProvider spreadsheetFormatterProvider) {
        return new BasicSpreadsheetParserSelectorEditContext(
                Objects.requireNonNull(spreadsheetParserProvider, "spreadsheetParserProvider"),
                Objects.requireNonNull(spreadsheetParserContext, "spreadsheetParserContext"),
                Objects.requireNonNull(spreadsheetFormatterContext, "spreadsheetFormatterContext"),
                Objects.requireNonNull(spreadsheetFormatterProvider, "spreadsheetFormatterProvider")
        );
    }

    private BasicSpreadsheetParserSelectorEditContext(final SpreadsheetParserProvider spreadsheetParserProvider,
                                                      final SpreadsheetParserContext spreadsheetParserContext,
                                                      final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                      final SpreadsheetFormatterProvider spreadsheetFormatterProvider) {
        this.spreadsheetParserProvider = spreadsheetParserProvider;
        this.spreadsheetParserContext = spreadsheetParserContext;
        this.spreadsheetFormatterContext = spreadsheetFormatterContext;
        this.spreadsheetFormatterProvider = spreadsheetFormatterProvider;
    }

    // SpreadsheetParserProvider........................................................................................

    @Override
    public SpreadsheetParserProvider spreadsheetParserProvider() {
        return this.spreadsheetParserProvider;
    }

    private final SpreadsheetParserProvider spreadsheetParserProvider;

    // SpreadsheetParserContext.........................................................................................

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
    public String toString() {
        return this.spreadsheetFormatterContext + " " + this.spreadsheetFormatterProvider;
    }
}
