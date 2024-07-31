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

import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContextDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTextComponent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A delegating {@link SpreadsheetFormatterSelectorEditContext} that uses a {@link SpreadsheetFormatterContext} and
 * {@link SpreadsheetFormatterProvider}.
 */
final class BasicSpreadsheetFormatterSelectorEditContext implements SpreadsheetFormatterSelectorEditContext,
        SpreadsheetFormatterContextDelegator {

    static BasicSpreadsheetFormatterSelectorEditContext with(final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                             final SpreadsheetFormatterProvider spreadsheetFormatterProvider) {
        return new BasicSpreadsheetFormatterSelectorEditContext(
                Objects.requireNonNull(spreadsheetFormatterContext, "spreadsheetFormatterContext"),
                Objects.requireNonNull(spreadsheetFormatterProvider, "spreadsheetFormatterProvider")
        );
    }

    private BasicSpreadsheetFormatterSelectorEditContext(final SpreadsheetFormatterContext spreadsheetFormatterContext,
                                                         final SpreadsheetFormatterProvider spreadsheetFormatterProvider) {
        this.spreadsheetFormatterContext = spreadsheetFormatterContext;
        this.spreadsheetFormatterProvider = spreadsheetFormatterProvider;
    }

    // SpreadsheetFormatterContext......................................................................................

    @Override
    public SpreadsheetFormatterContext spreadsheetFormatterContext() {
        return this.spreadsheetFormatterContext;
    }

    private final SpreadsheetFormatterContext spreadsheetFormatterContext;

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector selector) {
        return spreadsheetFormatterProvider.spreadsheetFormatter(selector);
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterName name,
                                                     final List<?> values) {
        return spreadsheetFormatterProvider.spreadsheetFormatter(name, values);
    }

    @Override
    public Optional<SpreadsheetFormatterSelectorTextComponent> spreadsheetFormatterNextTextComponent(final SpreadsheetFormatterSelector selector) {
        return spreadsheetFormatterProvider.spreadsheetFormatterNextTextComponent(selector);
    }

    @Override
    public List<SpreadsheetFormatterSample> spreadsheetFormatterSamples(final SpreadsheetFormatterName name,
                                                                        final SpreadsheetFormatterContext context) {
        return spreadsheetFormatterProvider.spreadsheetFormatterSamples(
                name,
                context
        );
    }

    @Override
    public Set<SpreadsheetFormatterInfo> spreadsheetFormatterInfos() {
        return spreadsheetFormatterProvider.spreadsheetFormatterInfos();
    }

    private final SpreadsheetFormatterProvider spreadsheetFormatterProvider;

    @Override
    public String toString() {
        return this.spreadsheetFormatterContext + " " + this.spreadsheetFormatterProvider;
    }
}
