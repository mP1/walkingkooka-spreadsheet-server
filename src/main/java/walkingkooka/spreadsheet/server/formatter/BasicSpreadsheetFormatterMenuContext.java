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

import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Objects;

final class BasicSpreadsheetFormatterMenuContext implements SpreadsheetFormatterMenuContext,
    SpreadsheetFormatterProviderDelegator,
    SpreadsheetFormatterProviderSamplesContextDelegator {

    static BasicSpreadsheetFormatterMenuContext with(final SpreadsheetFormatterProvider spreadsheetFormatterProvider,
                                                     final SpreadsheetFormatterProviderSamplesContext spreadsheetFormatterProviderSamplesContext) {
        return new BasicSpreadsheetFormatterMenuContext(
            Objects.requireNonNull(spreadsheetFormatterProvider, "spreadsheetFormatterProvider"),
            Objects.requireNonNull(spreadsheetFormatterProviderSamplesContext, "spreadsheetFormatterProviderSamplesContext")
        );
    }

    private BasicSpreadsheetFormatterMenuContext(final SpreadsheetFormatterProvider spreadsheetFormatterProvider,
                                                 final SpreadsheetFormatterProviderSamplesContext spreadsheetFormatterProviderSamplesContext) {
        this.spreadsheetFormatterProvider = spreadsheetFormatterProvider;
        this.spreadsheetFormatterProviderSamplesContext = spreadsheetFormatterProviderSamplesContext;
    }

    @Override
    public <T> SpreadsheetFormatterMenuContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                   final T value) {
        this.spreadsheetFormatterProviderSamplesContext.setEnvironmentValue(
            name,
            value
        );
        return this;
    }

    @Override
    public SpreadsheetFormatterMenuContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        this.spreadsheetFormatterProviderSamplesContext.removeEnvironmentValue(name);
        return this;
    }

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.spreadsheetFormatterProvider;
    }

    private final SpreadsheetFormatterProvider spreadsheetFormatterProvider;

    // SpreadsheetFormatterProviderSamplesContext.......................................................................

    @Override
    public SpreadsheetFormatterProviderSamplesContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final SpreadsheetFormatterProviderSamplesContext before = this.spreadsheetFormatterProviderSamplesContext;
        final SpreadsheetFormatterProviderSamplesContext after = before.setPreProcessor(processor);

        return before.equals(after) ?
            this :
            new BasicSpreadsheetFormatterMenuContext(
                this.spreadsheetFormatterProvider,
                after
            );
    }

    @Override
    public SpreadsheetFormatterProviderSamplesContext spreadsheetFormatterProviderSamplesContext() {
        return this.spreadsheetFormatterProviderSamplesContext;
    }

    private final SpreadsheetFormatterProviderSamplesContext spreadsheetFormatterProviderSamplesContext;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.spreadsheetFormatterProvider + " " + spreadsheetFormatterProviderSamplesContext;
    }
}
