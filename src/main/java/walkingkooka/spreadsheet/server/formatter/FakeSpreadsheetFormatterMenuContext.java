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

import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.format.FakeSpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContext;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorToken;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.List;
import java.util.Optional;

public class FakeSpreadsheetFormatterMenuContext extends FakeSpreadsheetFormatterContext
    implements SpreadsheetFormatterMenuContext {

    @Override
    public SpreadsheetFormatterProviderSamplesContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatterProviderSamplesContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterSelector selector,
                                                     final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatter spreadsheetFormatter(final SpreadsheetFormatterName name,
                                                     final List<?> values,
                                                     final ProviderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetFormatterSelectorToken> spreadsheetFormatterNextToken(final SpreadsheetFormatterSelector selector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SpreadsheetFormatterSample> spreadsheetFormatterSamples(final SpreadsheetFormatterSelector selector,
                                                                        final boolean includeSamples,
                                                                        final SpreadsheetFormatterProviderSamplesContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatterInfoSet spreadsheetFormatterInfos() {
        throw new UnsupportedOperationException();
    }

    // HasProviderContext...............................................................................................

    @Override
    public ProviderContext providerContext() {
        throw new UnsupportedOperationException();
    }
}
