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

import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderDelegator;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContextDelegator;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
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

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider spreadsheetFormatterProvider() {
        return this.spreadsheetFormatterProvider;
    }

    private final SpreadsheetFormatterProvider spreadsheetFormatterProvider;

    // SpreadsheetFormatterProviderSamplesContext.......................................................................

    @Override
    public SpreadsheetFormatterProviderSamplesContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setSpreadsheetFormatterProviderSamplesContext(
            this.spreadsheetFormatterProviderSamplesContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public SpreadsheetFormatterProviderSamplesContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setSpreadsheetFormatterProviderSamplesContext(
            this.spreadsheetFormatterProviderSamplesContext.setPreProcessor(processor)
        );
    }

    private SpreadsheetFormatterProviderSamplesContext setSpreadsheetFormatterProviderSamplesContext(final SpreadsheetFormatterProviderSamplesContext context) {
        return this.spreadsheetFormatterProviderSamplesContext.equals(context) ?
            this :
            with(
                this.spreadsheetFormatterProvider,
                context
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
